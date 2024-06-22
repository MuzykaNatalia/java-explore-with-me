package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatsClient;
import ru.practicum.event.service.EventService;
import ru.practicum.event.sort.SortEvent;
import ru.practicum.event.dto.*;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.category.model.Category;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.event.state.EventState;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static ru.practicum.Constant.NAME_SERVICE_APP;
import static ru.practicum.event.state.EventState.PENDING;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final StatsClient statsClient;
    private final EventMapper eventMapper;
    // TODO информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики
    //это публичный эндпоинт, соответственно в выдаче должны быть только опубликованные события
    //текстовый поиск (по аннотации и подробному описанию) должен быть без учета регистра букв
    //если в запросе не указан диапазон дат [rangeStart-rangeEnd], то нужно выгружать события, которые произойдут
    // позже текущей даты и времени
    //информация о каждом событии должна включать в себя количество просмотров и количество уже одобренных
    // заявок на участие
    //информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики
    //В случае, если по заданным фильтрам не найдено ни одного события, возвращает пустой список
    @Override// 400
    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd, Boolean onlyAvailable, SortEvent sort,
                                         Integer from, Integer size, HttpServletRequest request) {
        return null;
    }
    //событие должно быть опубликовано
    //информация о событии должна включать в себя количество просмотров и количество подтвержденных запросов
    // TODO информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики
    @Override
    public EventFullDto getOneEvent(Long id, HttpServletRequest request) {
        Event event = eventRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Event with id=" + id + " was not found",
                                Collections.singletonList("Event id does not exist")));
        return null;
    }

    //Эндпоинт возвращает полную информацию обо всех событиях подходящих под переданные условия
    //В случае, если по заданным фильтрам не найдено ни одного события, возвращает пустой список
    @Override// 400
    public List<EventFullDto> getEventsForAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                Integer from, Integer size) {
        return null;
    }
    //Редактирование данных любого события администратором. Валидация данных не требуется. Обратите внимание:
    //дата начала изменяемого события должна быть не ранее чем за час от даты публикации. (Ожидается код ошибки 409)
    //событие можно публиковать, только если оно в состоянии ожидания публикации (Ожидается код ошибки 409)
    //событие можно отклонить, только если оно еще не опубликовано (Ожидается код ошибки 409)
    @Override// 404, 409
    public EventFullDto updateEventByAdmin(UpdateEventAdminRequest updateEventAdminRequest, Long eventId) {
        return null;
    }
    //В случае, если по заданным фильтрам не найдено ни одного события, возвращает пустой список
    @Override// 400
    public List<EventShortDto> getOwnerEvents(Long userId, Integer from, Integer size) {
        return null;
    }

    @Override
    public EventFullDto createOwnerEvent(Long userId, NewEventDto newEventDto) {
        getExceptionIfTimeIsTwoHoursThanPresent(newEventDto.getEventDate());
        User user = getUser(userId);
        Category category = getCategory(newEventDto.getCategory());

        Event event = eventMapper.toEvent(newEventDto);
        event.setCategory(category);
        event.setConfirmedRequests(0);
        event.setCreatedOn(LocalDateTime.now());
        event.setInitiator(user);
        event.setEventState(PENDING);

        Event createdEvent = eventRepository.save(event);
        createdEvent.setViews(0L);
        log.info("Created event id={} owner id={}", createdEvent.getId(), userId);
        return eventMapper.toEventFullDto(createdEvent);
    }

    //В случае, если события с заданным id не найдено, возвращает статус код 404
    @Override // 400, 404
    public EventFullDto getOwnerOneEvent(Long userId, Long eventId) {
        Event event = getExceptionIfThisNotOwnerOfEvent(eventId, userId);
        // TODO достать статискику
        return null;
    }
    //изменить можно только отмененные события или события в состоянии ожидания модерации (Ожидается код ошибки 409)
    //дата и время на которые намечено событие не может быть раньше,
    // чем через два часа от текущего момента (Ожидается код ошибки 409)
    @Override// 400, 404, 409
    public EventFullDto updateOwnerEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        return null;
    }


    private void sendInfoAboutViewInStats(Long eventId, HttpServletRequest request) {
        statsClient.createHit(new EndpointHitDto(NAME_SERVICE_APP, "/events/" + eventId,
                request.getRemoteAddr(), LocalDateTime.now()));
    }

    private Object getInfoAboutViewInStats() {
        return null;
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

    private Event getExceptionIfThisNotOwnerOfEvent(Long eventId, Long userId) {
        return eventRepository.findByIdAndUser_id(eventId, userId).orElseThrow(() ->
                new NotFoundException("Event with id=" + eventId + " was not found",
                        Collections.singletonList("Event id does not exist")));
    }

    private Pageable getPageable(Integer from, Integer size, Sort sort) {
        return PageRequest.of(from / size, size, sort);
    }

    private void getExceptionIfTimeIsTwoHoursThanPresent(LocalDateTime verifiableTime) {
        if (verifiableTime.isBefore(LocalDateTime.now().minusHours(2))) {
            throw new ConflictException("Field: eventDate. Error: must contain a date that has not yet occurred. " +
                    "Value: " + verifiableTime, Collections.singletonList("The date and time on which the event is " +
                    "scheduled cannot be earlier than two hours from the current moment"));
        }
    }
}
