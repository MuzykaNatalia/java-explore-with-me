package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.event.repository.CustomSearchEventRepository;
import ru.practicum.event.sort.SortEvent;
import ru.practicum.event.dto.*;
import ru.practicum.exception.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.category.model.Category;
import ru.practicum.event.model.Event;
import ru.practicum.exception.ValidationException;
import ru.practicum.location.model.Location;
import ru.practicum.location.repository.LocationRepository;
import ru.practicum.user.model.User;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.event.state.EventState;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.constant.Constant.NAME_SERVICE_APP;
import static ru.practicum.event.sort.SortEvent.VIEWS;
import static ru.practicum.event.state.AdminStateAction.*;
import static ru.practicum.event.state.EventState.*;
import static ru.practicum.event.state.UserStateAction.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final CustomSearchEventRepository customSearchEventRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final StatsClient statsClient;
    private final EventMapper eventMapper;

    @Transactional
    @Override
    public EventFullDto createOwnerEvent(Long userId, NewEventDto newEventDto) {
        getErrorIfTimeBeforeStartsIsLessThen(newEventDto.getEventDate(), 2);

        User initiator = getUser(userId);
        Category category = getCategory(newEventDto.getCategory());
        Location location = checkAndSaveLocation(newEventDto.getLocation());
        Event event = eventMapper.toEventForCreate(newEventDto, category, initiator, location);

        Event createdEvent = eventRepository.save(event);
        log.info("Created event id={} owner id={} : {}", createdEvent.getId(), userId, createdEvent);
        return eventMapper.toEventFullDto(createdEvent);
    }

    @Transactional
    @Override
    public EventFullDto updateOwnerEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event oldEvent = getExceptionIfThisNotOwnerOfEvent(eventId, userId);

        getExceptionIfStateEventPublished(oldEvent.getEventState());
        getErrorIfTimeBeforeStartsIsLessThen(request.getEventDate(), 2);
        getErrorIfTimeBeforeStartsIsLessThen(oldEvent.getEventDate(), 2);

        Location location = checkAndSaveLocation(request.getLocation());
        Category category = request.getCategory() != null
                ? getCategory(request.getCategory()) : oldEvent.getCategory();

        if (CANCEL_REVIEW.equals(request.getStateAction())) {
            oldEvent = eventMapper.toEventUpdate(oldEvent, request, category, location);
            oldEvent.setEventState(CANCELED);
            log.info("Cancel event id={} owner id={} : {}", eventId, userId, oldEvent);
            return eventMapper.toEventFullDto(eventRepository.save(oldEvent));
        } else if (SEND_TO_REVIEW.equals(request.getStateAction())) {
            oldEvent = eventMapper.toEventUpdate(oldEvent, request, category, location);
            oldEvent.setEventState(PENDING);
            log.info("Update event id={} owner id={} : {}", eventId, userId, oldEvent);
        }

        return eventMapper.toEventFullDto(eventRepository.save(oldEvent));
    }

    @Transactional
    @Override
    public EventFullDto updateEventByAdmin(UpdateEventAdminRequest request, Long eventId) {
        Event event = getEvent(eventId);
        Category category = request.getCategory() != null
                ? getCategory(request.getCategory()) : event.getCategory();
        Location location = checkAndSaveLocation(request.getLocation());
        request.setLocation(location);

        getErrorIfTimeBeforeStartsIsLessThen(request.getEventDate(), 1);
        getErrorIfTimeBeforeStartsIsLessThen(event.getEventDate(), 1);

        if (PUBLISH_EVENT.equals(request.getStateAction())) {
            if (event.getEventState().equals(PENDING)) {
                event = eventMapper.toEventUpdate(event, request, category, location);
                event.setPublishedOn(LocalDateTime.now());
                event.setEventState(PUBLISHED);
                log.info("Administrator published event id={} owner id={} : {}",
                        eventId, event.getInitiator().getId(), event);
            } else {
                    getExceptionIfEventNotPending(eventId);
                }
        } else if (REJECT_EVENT.equals(request.getStateAction())) {
            if (!event.getEventState().equals(PUBLISHED)) {
                event = eventMapper.toEventUpdate(event, request, category, location);
                event.setEventState(CANCELED);
                log.info("Administrator canceled event id={} owner id={} : {}",
                        eventId, event.getInitiator().getId(), event);
            } else {
                getExceptionIfEventPublished(eventId);
                }
        }
        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getOwnerOneEvent(Long userId, Long eventId) {
        getUser(userId);
        Event event = getExceptionIfThisNotOwnerOfEvent(eventId, userId);
        log.info("Event id={} received by owner id={} : {}", eventId, userId, event);
        return eventMapper.toEventFullDto(event);
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getOneEvent(Long eventId, HttpServletRequest request) {
        Event event = getEvent(eventId);

        if (event.getEventState().equals(PUBLISHED)) {
            sendInfoAboutViewInStats(List.of(eventId), request);
            setViewsForOneEvents(event);
        } else {
            getExceptionIfEventNotPublished(eventId);
        }

        log.info("Event id={} received user ip={} : {}", event, request.getRemoteAddr(), event);
        return eventMapper.toEventFullDto(event);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getOwnerEvents(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> eventList = eventRepository.findAllByInitiatorId(userId, pageable).orElse(new ArrayList<>());
        log.info("Events issued to the owner id={} : {}", userId, eventList);
        return eventMapper.toEventShortDtoList(eventList);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventFullDto> getEventsForAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                Integer from, Integer size) {
        checkDateTime(rangeStart, rangeEnd);
        SearchEventCriteriaAdmin newSearchEventCriteriaAdmin = SearchEventCriteriaAdmin.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();

        List<Event> events = customSearchEventRepository.getEventsByCriteriaByAdmin(newSearchEventCriteriaAdmin);

        log.info("Information on events for the administrator has been received, size={}", events.size());
        return eventMapper.toEventFullDtoList(events);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd, Boolean onlyAvailable, SortEvent sort,
                                         Integer from, Integer size, HttpServletRequest request) {
        checkDateTime(rangeStart, rangeEnd);
        SearchEventCriteria newSearchEventCriteria = SearchEventCriteria.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .from(from)
                .size(size)
                .build();

        List<Event> events = customSearchEventRepository.getEventsByCriteriaByAll(newSearchEventCriteria);
        sendInfoAboutViewInStats(events.stream().map(Event::getId).collect(Collectors.toList()), request);

        events = setViewsForListEvents(events);

        if (VIEWS.equals(sort)) {
            events = events.stream().sorted(Comparator.comparing(Event::getViews)).collect(Collectors.toList());
        }

        log.info("Information on events size={} for the user ip={} has been received",
                events.size(), request.getRemoteAddr());
        return eventMapper.toEventShortDtoList(events);
    }

    private void getErrorIfTimeBeforeStartsIsLessThen(LocalDateTime verifiableTime, Integer plusHours) {
        if (verifiableTime != null && verifiableTime.isBefore(LocalDateTime.now().plusHours(plusHours))) {
            throw new ValidationException("Field: eventDate. Error: must contain a date that has not yet occurred. " +
                    "Value: " + verifiableTime, Collections.singletonList("The date and time on which the event is " +
                    "scheduled cannot be earlier than " + plusHours + " two hours from the current moment"));
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("User with id=" + userId + " was not found",
                        Collections.singletonList("User id does not exist")));
    }

    private Category getCategory(Long catId) {
        return categoryRepository.findById(catId).orElseThrow(() ->
                new NotFoundException("Category with id=" + catId + " was not found",
                        Collections.singletonList("Category id does not exist")));
    }

    private Location checkAndSaveLocation(Location newLocation) {
        if (newLocation == null) {
            return null;
        }

        Location location = locationRepository.findByLatAndLon(newLocation.getLat(), newLocation.getLon())
                .orElse(null);
        if (location == null) {
            return locationRepository.save(newLocation);
        }
        return location;
    }

    private Event getExceptionIfThisNotOwnerOfEvent(Long eventId, Long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() ->
                new NotFoundException("Event with id=" + eventId + " was not found",
                        Collections.singletonList("Event id does not exist")));
    }

    private void getExceptionIfStateEventPublished(EventState eventState) {
        if (eventState.equals(PUBLISHED)) {
            throw new ConflictException("Event must not be published",
                    Collections.singletonList("Only pending or canceled events can be changed"));
        }
    }

    private void getExceptionIfEventNotPending(Long eventId) {
        log.warn("Event id={} is not PENDING", eventId);
        throw new ConflictException("Event is not PENDING", Collections.singletonList("An event can " +
                "only be published if it is in a publish PENDING state"));
    }

    private void getExceptionIfEventPublished(Long eventId) {
        log.warn("Event id={} is not PENDING or CANCELED", eventId);
        throw new ConflictException("Cannot canceled the event because it's not in the right state: " +
                "PUBLISHED", Collections.singletonList("The event must be in a state PENDING or CANCELED"));
    }

    private void sendInfoAboutViewInStats(List<Long> eventsIds, HttpServletRequest request) {
        for (Long id : eventsIds) {
            statsClient.createHit(new EndpointHitDto(NAME_SERVICE_APP, "/events/" + id,
                    request.getRemoteAddr(), LocalDateTime.now()));
        }
    }

    private void setViewsForOneEvents(Event event) {
        List<String> uri = List.of("/events/" + event.getId());
        ViewStatsDto viewStatsDto = statsClient.getStats(event.getCreatedOn(), LocalDateTime.now(), uri, true)
                .get(0);
        event.setViews(viewStatsDto.getHits());
    }

    private void getExceptionIfEventNotPublished(Long eventId) {
        log.warn("Event id={} is not PUBLISHED", eventId);
        throw new NotFoundException("Event not found", Collections.singletonList("Incorrect id"));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Event with id=" + eventId + " was not found",
                        Collections.singletonList("Event id does not exist")));
    }

    private void checkDateTime(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (isNotNullTime(rangeStart, rangeEnd) && rangeEnd.isBefore(rangeStart)) {
            throw new ValidationException("The end time cannot be earlier than the start time",
                    Collections.singletonList("Incorrect end time has been transmitted"));
        }
    }

    private boolean isNotNullTime(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        return rangeStart != null && rangeEnd != null;
    }

    private List<Event> setViewsForListEvents(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return events;
        }

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        LocalDateTime minCreatedOn = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        List<ViewStatsDto> viewStatsDto = statsClient.getStats(minCreatedOn, LocalDateTime.now(), uris, true);

        Map<Long, Long> eventsViews = new HashMap<>();
        viewStatsDto.forEach(v -> eventsViews.put(Long.valueOf(v.getUri().replace("/events/", "")),
                v.getHits() != null ? v.getHits() : 0));

        events = events.stream()
                .map(event -> {
                    Long views = eventsViews.getOrDefault(event.getId(), 0L);
                    event.setViews(views);
                    return event;
                })
                .collect(Collectors.toList());

        return events;
    }
}
