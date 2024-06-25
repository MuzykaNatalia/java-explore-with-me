package ru.practicum.event.service;

import io.micrometer.core.lang.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.event.sort.SortEvent;
import ru.practicum.event.dto.*;
import ru.practicum.exceptions.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.category.model.Category;
import ru.practicum.event.model.Event;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.location.model.Location;
import ru.practicum.location.repository.LocationRepository;
import ru.practicum.user.model.User;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.event.state.EventState;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.Constant.NAME_SERVICE_APP;
import static ru.practicum.event.sort.SortEvent.EVENT_DATE;
import static ru.practicum.event.state.AdminStateAction.PUBLISH_EVENT;
import static ru.practicum.event.state.AdminStateAction.REJECT_EVENT;
import static ru.practicum.event.state.EventState.*;
import static ru.practicum.user.state.UserStateAction.CANCEL_REVIEW;
import static ru.practicum.user.state.UserStateAction.SEND_TO_REVIEW;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final StatsClient statsClient;
    private final EventMapper eventMapper;

    @Transactional
    @Override
    public EventFullDto createOwnerEvent(Long userId, NewEventDto newEventDto) {
        getErrorIfTimeBeforeStartsIsLessTwoHours(newEventDto.getEventDate());
        User initiator = getUser(userId);
        Category category = getCategory(newEventDto.getCategory());
        Location location = locationRepository.save(newEventDto.getLocation());
        Event event = eventMapper.toEvent(newEventDto, category, initiator, location);

        Event createdEvent = eventRepository.save(event);
        log.info("Created event id={} owner id={}", createdEvent.getId(), userId);
        return eventMapper.toEventFullDto(createdEvent);
    }

    @Transactional
    @Override
    public EventFullDto updateOwnerEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event oldEvent = getExceptionIfThisNotOwnerOfEvent(eventId, userId);
        getExceptionIfStateEventPublished(oldEvent.getEventState());
        getErrorIfTimeBeforeStartsIsLessTwoHours(request.getEventDate());
        getErrorIfTimeBeforeStartsIsLessTwoHours(oldEvent.getEventDate());
        Category category = request.getCategory() != null
                ? getCategory(request.getCategory()) : oldEvent.getCategory();

        if (CANCEL_REVIEW.equals(request.getStateAction())) {
            oldEvent = eventMapper.toEvent(oldEvent, request, category);
            oldEvent.setEventState(CANCELED);
            log.info("Cancel event={} id={} owner id={}", oldEvent, eventId, userId);
            return eventMapper.toEventFullDto(eventRepository.save(oldEvent));
        } else if (SEND_TO_REVIEW.equals(request.getStateAction())) {
            oldEvent = eventMapper.toEvent(oldEvent, request, category);
            oldEvent.setEventState(PENDING);
            log.info("Update event={} id={} owner id={}", oldEvent, eventId, userId);
        }

        return eventMapper.toEventFullDto(eventRepository.save(oldEvent));
    }

    @Transactional
    @Override
    public EventFullDto updateEventByAdmin(UpdateEventAdminRequest request, Long eventId) {
        Event oldEvent = getEvent(eventId);
        Category category = request.getCategory() != null
                ? getCategory(request.getCategory()) : oldEvent.getCategory();
        Location location = request.getLocation() != null
                ? locationRepository.save(request.getLocation()) : oldEvent.getLocation();
        request.setLocation(location);
        getErrorIfTimeBeforeStartsIsLessOneHours(request.getEventDate());
        getErrorIfTimeBeforeStartsIsLessOneHours(oldEvent.getEventDate());

        if (PUBLISH_EVENT.equals(request.getStateAction())) {
            if (oldEvent.getEventState().equals(PENDING)) {
                oldEvent.setPublishedOn(LocalDateTime.now());
                oldEvent = eventMapper.toEvent(oldEvent, request, category);
                oldEvent.setEventState(PUBLISHED);
                log.info("Administrator published event={} id={} owner id={}",
                        oldEvent, eventId, oldEvent.getInitiator().getId());
            } /*else {
                    log.warn("Event id={} is not PENDING", eventId);
                    throw new ConflictException("Event is not PENDING", Collections.singletonList("An event can " +
                            "only be published if it is in a publish PENDING state"));
                }*/
        } else if (REJECT_EVENT.equals(request.getStateAction())) {
            if (!oldEvent.getEventState().equals(PUBLISHED)) {
                oldEvent = eventMapper.toEvent(oldEvent, request, category);
                oldEvent.setEventState(CANCELED);
                log.info("Administrator canceled event={} id={} owner id={}",
                        oldEvent, eventId, oldEvent.getInitiator().getId());
            } /*else {
                    log.warn("Event id={} is not PENDING or CANCELED", eventId);
                    throw new ConflictException("Cannot canceled the event because it's not in the " +
                            "right state: PUBLISHED", Collections.singletonList("The event must be in a state " +
                            "PENDING or CANCELED"));
                }*/
        }
        return eventMapper.toEventFullDto(eventRepository.save(oldEvent));
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getOwnerOneEvent(Long userId, Long eventId) {
        Event event = getExceptionIfThisNotOwnerOfEvent(eventId, userId);
        log.info("Event id={} received by owner id={}", eventId, userId);
        return eventMapper.toEventFullDto(event);
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getOneEvent(Long eventId, HttpServletRequest request) {
        Event event = getEvent(eventId);
        if (event.getEventState().equals(PUBLISHED)) {
            event = setViews(List.of(event)).get(0);
        } else {
            log.warn("Event id={} is not PUBLISHED", eventId);
            throw new NotFoundException("Event not found", Collections.singletonList("Incorrect id"));
        }
        sendInfoAboutViewInStats(List.of(eventId), request);
        log.info("Event id={} received user ip={}", event, request.getRemoteAddr());
        return eventMapper.toEventFullDto(event);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getOwnerEvents(Long userId, Integer from, Integer size) {
        Pageable pageable = getPageableSortByAsc(from, size);
        List<Event> eventList = eventRepository.findByInitiatorId(userId, pageable).orElse(new ArrayList<>());
        log.info("Events issued to the owner id={}", userId);
        return eventMapper.toEventShortDtoList(eventList);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventFullDto> getEventsForAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                Integer from, Integer size) {
        checkDateTime(rangeStart, rangeEnd);
        boolean isTime = isNotNullTime(rangeStart, rangeEnd);
        Pageable pageable = getPageableSortByAsc(from, size);

        if (isNotNullUsersAndStatesAndCategories(users, states, categories)) {
            return findEventsByUsersStatesCategoriesForAdmin(
                    users, states, categories, rangeStart, rangeEnd, pageable, isTime);
        } else if (isNotNullUsersAndStates(users, states)) {
            return findEventsByUsersStatesForAdmin(users, states, rangeStart, rangeEnd, pageable, isTime);
        } else if (isNotNullStatesAndCategories(states, categories)) {
            return findEventsByStatesCategoriesForAdmin(states, categories, rangeStart, rangeEnd, pageable, isTime);
        } else if (isNotNullUsersAndCategories(users, categories)) {
            return findEventsByUsersCategoriesForAdmin(users, categories, rangeStart, rangeEnd, pageable, isTime);
        } else if (users != null && !users.isEmpty()) {
            return findEventsByUsersForAdmin(users, rangeStart, rangeEnd, pageable, isTime);
        } else if (states != null && !states.isEmpty()) {
            return findEventsByStatesForAdmin(states, rangeStart, rangeEnd, pageable, isTime);
        } else if (categories != null && !categories.isEmpty()) {
            return findEventsByCategoriesForAdmin(categories, rangeStart, rangeEnd, pageable, isTime);
        } else if (isTime) {
            return eventMapper.toEventFullDtoList(eventRepository.findByDateStartAndEnd(rangeStart, rangeEnd, pageable)
                    .orElse(new ArrayList<>()));
        }
        return eventMapper.toEventFullDtoList(eventRepository.findAll(pageable).getContent());
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd, Boolean onlyAvailable, SortEvent sort,
                                         Integer from, Integer size, HttpServletRequest request) {
        checkDateTime(rangeStart, rangeEnd);
        boolean isTime = isNotNullTime(rangeStart, rangeEnd);
        Pageable pageable = sort.equals(EVENT_DATE) ?
                getPageableSortDesc(from, size, "event_date")
                : getPageableSortDesc(from, size, "views");

        List<Event> events = new ArrayList<>();
        if (isNotNullTextAndCategoriesAndPaid(text, categories, paid)) {
            events = findEventsByTextCategoriesPaidAvailableForAll(
                    text, categories, paid, rangeStart, rangeEnd, pageable, isTime);
        } else if (isNotNullTextAndCategories(text, categories)) {
            events = findEventsByTextCategoriesAvailableForAll(
                    text, categories, rangeStart, rangeEnd, pageable, isTime);
        } else if (isNotNullAndCategoriesAndPaid(categories, paid)) {
            events = findEventsByCategoriesPaidAvailableForAll(
                    categories, paid, rangeStart, rangeEnd, pageable, isTime);
        } else if (isNotNullTextAndPaid(text, paid)) {
            events = findEventsByTextPaidAvailableForAll(
                    text, paid, rangeStart, rangeEnd, pageable, isTime);
        } else if (text != null && !text.isEmpty()) {
            events = findEventsByTextAvailableForAll(text, rangeStart, rangeEnd, pageable, isTime);
        } else if (categories != null && !categories.isEmpty()) {
            events = findEventsByCategoriesAvailableForAll(
                    categories, rangeStart, rangeEnd, pageable, isTime);
        } else if (paid != null) {
            events = findEventsByPaidAvailableForAll(paid, rangeStart, rangeEnd, pageable, isTime);
        }
        sendInfoAboutViewInStats(events.stream().map(Event::getId).collect(Collectors.toList()), request);

        //events = setViews(events);
        return eventMapper.toEventShortDtoList(events);
    }

    private List<EventFullDto> findEventsByUsersStatesCategoriesForAdmin(List<Long> users, List<EventState> states,
                                                                        List<Long> categories, LocalDateTime rangeStart,
                                                                        LocalDateTime rangeEnd, Pageable pageable,
                                                                        boolean isTime) {
        List<Event> eventList = isTime ?
                eventRepository.findByUserIdAndStatesAndCategoriesAndDateStartAndEnd(users, states,
                        categories, rangeStart, rangeEnd, pageable).orElse(new ArrayList<>())
                : eventRepository.findByInitiatorIdInAndEventStateInAndCategoryIdIn(users, states, categories,
                pageable).orElse(new ArrayList<>());
        return eventMapper.toEventFullDtoList(eventList);
    }

    private List<EventFullDto> findEventsByUsersStatesForAdmin(List<Long> users, List<EventState> states,
                                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                               Pageable pageable, boolean isTime) {
        List<Event> eventList = isTime ?
                eventRepository.findByUserIdAndStatesAndDateStartAndEnd(users, states, rangeStart, rangeEnd,
                        pageable).orElse(new ArrayList<>())
                : eventRepository.findByInitiatorIdInAndEventStateIn(users, states, pageable)
                .orElse(new ArrayList<>());
        return eventMapper.toEventFullDtoList(eventList);
    }

    private List<EventFullDto> findEventsByStatesCategoriesForAdmin(List<EventState> states, List<Long> categories,
                                                                    LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                                    Pageable pageable, boolean isTime) {
        List<Event> eventList = isTime ?
                eventRepository.findByStatesAndCategoriesAndDateStartAndEnd(states, categories, rangeStart,
                        rangeEnd, pageable).orElse(new ArrayList<>())
                : eventRepository.findByEventStateInAndCategoryIdIn(states, categories, pageable)
                .orElse(new ArrayList<>());
        return eventMapper.toEventFullDtoList(eventList);
    }

    private List<EventFullDto> findEventsByUsersCategoriesForAdmin(List<Long> users, List<Long> categories,
                                                                   LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                                   Pageable pageable, boolean isTime) {
        List<Event> eventList = isTime ?
                eventRepository.findByUserIdAndCategoriesAndDateStartAndEnd(users, categories, rangeStart,
                        rangeEnd, pageable).orElse(new ArrayList<>())
                : eventRepository.findByInitiatorIdInAndCategoryIdIn(users, categories, pageable)
                .orElse(new ArrayList<>());
        return eventMapper.toEventFullDtoList(eventList);
    }

    private List<EventFullDto> findEventsByUsersForAdmin(List<Long> users, LocalDateTime rangeStart,
                                                         LocalDateTime rangeEnd, Pageable pageable, boolean isTime) {
        List<Event> eventList = isTime ?
                eventRepository.findByUserIdAndDateStartAndEnd(users, rangeStart, rangeEnd, pageable)
                        .orElse(new ArrayList<>())
                : eventRepository.findAllByInitiatorIdIn(users, pageable).orElse(new ArrayList<>());
        return eventMapper.toEventFullDtoList(eventList);
    }

    private List<EventFullDto> findEventsByStatesForAdmin(List<EventState> states, LocalDateTime rangeStart,
                                                          LocalDateTime rangeEnd, Pageable pageable, boolean isTime) {
        List<Event> eventList = isTime ?
                eventRepository.findByStatesAndDateStartAndEnd(states, rangeStart, rangeEnd, pageable)
                        .orElse(new ArrayList<>())
                : eventRepository.findAllByEventStateIn(states, pageable).orElse(new ArrayList<>());
        return eventMapper.toEventFullDtoList(eventList);
    }

    private List<EventFullDto> findEventsByCategoriesForAdmin(List<Long> categories, LocalDateTime rangeStart,
                                                              LocalDateTime rangeEnd, Pageable pageable, boolean isTime) {
        List<Event> eventList = isTime ?
                eventRepository.findByCategoriesAndDateStartAndEnd(categories, rangeStart, rangeEnd, pageable)
                        .orElse(new ArrayList<>())
                : eventRepository.findAllByCategoryIdIn(categories, pageable).orElse(new ArrayList<>());
        return eventMapper.toEventFullDtoList(eventList);
    }

    private List<Event> findEventsByTextCategoriesPaidAvailableForAll(String text, List<Long> categories,
                                                                      Boolean paid, LocalDateTime rangeStart,
                                                                      LocalDateTime rangeEnd,
                                                                      Pageable pageable, boolean isTime) {
        return isTime ?
                eventRepository.findPublishedByTextAndCategoriesAndTimeAndPaid(text, text, categories, paid, rangeStart,
                        rangeEnd, pageable).orElse(new ArrayList<>())
                : eventRepository.findPublishedByTextAndCategoriesAndPaid(text, text, categories, paid,
                        LocalDateTime.now(), pageable).orElse(new ArrayList<>());
    }

    private List<Event> findEventsByTextCategoriesAvailableForAll(String text, List<Long> categories,
                                                                  LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                                  Pageable pageable, boolean isTime) {
        return isTime ?
                eventRepository.findPublishedByTextAndCategoriesAndTime(text, text, categories, rangeStart, rangeEnd,
                        pageable).orElse(new ArrayList<>())
                : eventRepository.findPublishedByTextAndCategories(text, text, categories, LocalDateTime.now(),
                        pageable).orElse(new ArrayList<>());
    }

    private List<Event> findEventsByCategoriesPaidAvailableForAll(List<Long> categories, Boolean paid,
                                                                  LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                                  Pageable pageable, boolean isTime) {
        return isTime ?
                eventRepository.findPublishedByCategoriesAndPaid(categories, paid, rangeStart, rangeEnd, pageable)
                        .orElse(new ArrayList<>())
                : eventRepository.findPublishedByCategoriesAndPaid(categories, paid, LocalDateTime.now(), pageable)
                        .orElse(new ArrayList<>());
    }

    private List<Event> findEventsByTextPaidAvailableForAll(String text, Boolean paid, LocalDateTime rangeStart,
                                                            LocalDateTime rangeEnd, Pageable pageable, boolean isTime) {
        return isTime ?
                eventRepository.findPublishedByTextAndTimeAndPaid(text, text, paid, rangeStart, rangeEnd, pageable)
                        .orElse(new ArrayList<>())
                : eventRepository.findPublishedByTextAndPaid(text, text, paid, LocalDateTime.now(), pageable)
                        .orElse(new ArrayList<>());
    }

    private List<Event> findEventsByTextAvailableForAll(String text, LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                        Pageable pageable, boolean isTime) {
        return isTime ?
                eventRepository.findPublishedByText(text, text, rangeStart, rangeEnd, pageable)
                        .orElse(new ArrayList<>())
                : eventRepository.findPublishedByText(text, text, LocalDateTime.now(), pageable)
                        .orElse(new ArrayList<>());
    }

    private List<Event> findEventsByCategoriesAvailableForAll(List<Long> categories, LocalDateTime rangeStart,
                                                              LocalDateTime rangeEnd, Pageable pageable, boolean isTime) {
        return isTime ?
                eventRepository.findPublishedByCategoriesAndTime(categories, rangeStart, rangeEnd, pageable)
                        .orElse(new ArrayList<>())
                : eventRepository.findPublishedByCategories(categories, LocalDateTime.now(), pageable)
                        .orElse(new ArrayList<>());
    }

    private List<Event> findEventsByPaidAvailableForAll(Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                        Pageable pageable, boolean isTime) {
        return isTime ?
                eventRepository.findPublishedByPaid(paid, rangeStart, rangeEnd, pageable).orElse(new ArrayList<>())
                : eventRepository.findPublishedByPaid(paid, LocalDateTime.now(), pageable).orElse(new ArrayList<>());
    }

    private boolean isNotNullTime(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        return rangeStart != null && rangeEnd != null;
    }

    private boolean isNotNullUsersAndStatesAndCategories(List<Long> users, List<EventState> states,
                                                         List<Long> categories) {
        return users != null && states != null && categories != null
                && !users.isEmpty() && !states.isEmpty() && !categories.isEmpty();
    }

    private boolean isNotNullUsersAndStates(List<Long> users, List<EventState> states) {
        return users != null && states != null && !users.isEmpty() && !states.isEmpty();
    }

    private boolean isNotNullStatesAndCategories(List<EventState> states, List<Long> categories) {
        return states != null && categories != null && !states.isEmpty() && !categories.isEmpty();
    }

    private boolean isNotNullUsersAndCategories(List<Long> users, List<Long> categories) {
        return users != null && categories != null && !users.isEmpty() && !categories.isEmpty();
    }

    private boolean isNotNullTextAndCategoriesAndPaid(String text, List<Long> categories, Boolean paid) {
        return text != null && categories != null && paid != null && !text.isEmpty() && !categories.isEmpty();
    }

    private boolean isNotNullTextAndCategories(String text, List<Long> categories) {
        return text != null && categories != null && !text.isEmpty() && !categories.isEmpty();
    }

    private boolean isNotNullAndCategoriesAndPaid(List<Long> categories, Boolean paid) {
        return categories != null && paid != null && !categories.isEmpty();
    }

    private boolean isNotNullTextAndPaid(String text, Boolean paid) {
        return text != null && paid != null && !text.isEmpty();
    }

    private void checkDateTime(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (isNotNullTime(rangeStart, rangeEnd)) {
            if (rangeEnd.isBefore(rangeStart)) {
                throw new ValidationException("The end time cannot be earlier than the start time",
                        Collections.singletonList("Incorrect end time has been transmitted"));
            }
        }
    }

    private void sendInfoAboutViewInStats(List<Long> eventsIds, HttpServletRequest request) {
        for (Long id : eventsIds) {
            statsClient.createHit(new EndpointHitDto(NAME_SERVICE_APP, "/events/" + id,
                    request.getRemoteAddr(), LocalDateTime.now()));
        }
    }

    private Pageable getPageableSortByAsc(Integer from, Integer size) {
        return PageRequest.of(from / size, size);
    }

    private Pageable getPageableSortDesc(Integer from, Integer size, String property) {
        return PageRequest.of(from / size, size, Sort.by(Sort.Order.desc(property)));
    }

    private Category getCategory(Long catId) {
        return categoryRepository.findById(catId).orElseThrow(() ->
                new NotFoundException("Category with id=" + catId + " was not found",
                        Collections.singletonList("Category id does not exist")));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("User with id=" + userId + " was not found",
                        Collections.singletonList("User id does not exist")));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Event with id=" + eventId + " was not found",
                        Collections.singletonList("Event id does not exist")));
    }

    private Event getExceptionIfThisNotOwnerOfEvent(Long eventId, Long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() ->
                new NotFoundException("Event with id=" + eventId + " was not found",
                        Collections.singletonList("Event id does not exist")));
    }

    private void getExceptionIfStateEventPublished(EventState eventState) {
        if (eventState.equals(PUBLISHED)) {
            throw new ValidationException("Event must not be published",
                    Collections.singletonList("Only pending or canceled events can be changed"));
        }
    }

    private void getErrorIfTimeBeforeStartsIsLessTwoHours(LocalDateTime verifiableTime) {
        if (verifiableTime != null) {
            if (verifiableTime.isBefore(LocalDateTime.now().minusHours(2))) {
                throw new ValidationException("Field: eventDate. Error: must contain a date that has not yet occurred. " +
                        "Value: " + verifiableTime, Collections.singletonList("The date and time on which the event is " +
                        "scheduled cannot be earlier than two hours from the current moment"));
            }
        }
    }

    private void getErrorIfTimeBeforeStartsIsLessOneHours(LocalDateTime verifiableTime) {
        if (verifiableTime != null) {
            if (verifiableTime.isBefore(LocalDateTime.now().minusHours(1))) {
                throw new ValidationException("Field: eventDate. Error: must contain a date that has not yet occurred. " +
                        "Value: " + verifiableTime, Collections.singletonList("The date and time on which the event is " +
                        "scheduled cannot be earlier than one hours from the current moment"));
            }
        }
    }

    private List<Event> setViews(List<Event> events) {

        return events.stream()
                .peek(event -> {
                    try {
                        ResponseEntity<Object> response = statsClient.getStats(event.getCreatedOn(),
                                LocalDateTime.now(), new String[]{"/event/" + event.getId()}, true);
                        List<ViewStatsDto> viewStatsDto = (List<ViewStatsDto>) response.getBody();
                        event.setViews(viewStatsDto != null && !viewStatsDto.isEmpty()
                                ? viewStatsDto.get(0).getHits() + 1 : 1L);
                    } catch (Exception e) {
                        log.warn("Error getting statistics for an event id={}", event.getId());
                        throw new RuntimeException("Error getting statistics for an event id=" + event.getId());
                    }
                })
                .collect(Collectors.toList());
    }
}
