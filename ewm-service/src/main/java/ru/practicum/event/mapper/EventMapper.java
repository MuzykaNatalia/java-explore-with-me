package ru.practicum.event.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;
import ru.practicum.location.mapper.LocationMapper;
import ru.practicum.location.model.Location;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.Constant.PATTERN_DATE;
import static ru.practicum.event.state.EventState.PENDING;

@Component
@RequiredArgsConstructor
public class EventMapper {
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final LocationMapper locationMapper;

    public Event toEventForCreate(NewEventDto newEventDto, Category category, User initiator, Location location) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .category(category)
                .confirmedRequests(0)
                .createdOn(LocalDateTime.now())
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .initiator(initiator)
                .location(location)
                .paid(newEventDto.getPaid() != null ? newEventDto.getPaid() : false)
                .participantLimit(newEventDto.getParticipantLimit() != null ? newEventDto.getParticipantLimit() : 0)
                .requestModeration(
                        newEventDto.getRequestModeration() != null ? newEventDto.getRequestModeration() : true)
                .eventState(PENDING)
                .title(newEventDto.getTitle())
                .views(0L)
                .build();
    }

    public Event toEventUpdate(Event oldEvent, UpdateEventUserRequest request, Category categoryNew, Location location) {
        return Event.builder()
                .id(oldEvent.getId())
                .annotation(request.getAnnotation() != null ? request.getAnnotation() : oldEvent.getAnnotation())
                .category(request.getCategory() != null ? categoryNew : oldEvent.getCategory())
                .confirmedRequests(oldEvent.getConfirmedRequests())
                .createdOn(oldEvent.getCreatedOn())
                .description(request.getDescription() != null ? request.getDescription() : oldEvent.getDescription())
                .eventDate(request.getEventDate() != null ? request.getEventDate() : oldEvent.getEventDate())
                .initiator(oldEvent.getInitiator())
                .location(location != null ? location : oldEvent.getLocation())
                .paid(request.getPaid() != null ? request.getPaid() : oldEvent.getPaid())
                .participantLimit(request.getParticipantLimit() != null
                        ? request.getParticipantLimit() : oldEvent.getParticipantLimit())
                .publishedOn(oldEvent.getPublishedOn())
                .requestModeration(request.getRequestModeration() != null
                        ? request.getRequestModeration() : oldEvent.getRequestModeration())
                .eventState(oldEvent.getEventState())
                .title(request.getTitle() != null ? request.getTitle() : oldEvent.getTitle())
                .views(oldEvent.getViews())
                .build();
    }

    public Event toEventUpdate(Event oldEvent, UpdateEventAdminRequest request, Category categoryNew, Location location) {
        return Event.builder()
                .id(oldEvent.getId())
                .annotation(request.getAnnotation() != null ? request.getAnnotation() : oldEvent.getAnnotation())
                .category(request.getCategory() != null ? categoryNew : oldEvent.getCategory())
                .confirmedRequests(oldEvent.getConfirmedRequests())
                .createdOn(oldEvent.getCreatedOn())
                .description(request.getDescription() != null ? request.getDescription() : oldEvent.getDescription())
                .eventDate(request.getEventDate() != null ? request.getEventDate() : oldEvent.getEventDate())
                .initiator(oldEvent.getInitiator())
                .location(location != null ? location : oldEvent.getLocation())
                .paid(request.getPaid() != null ? request.getPaid() : oldEvent.getPaid())
                .participantLimit(request.getParticipantLimit() != null
                        ? request.getParticipantLimit() : oldEvent.getParticipantLimit())
                .publishedOn(oldEvent.getPublishedOn())
                .requestModeration(request.getRequestModeration() != null
                        ? request.getRequestModeration() : oldEvent.getRequestModeration())
                .eventState(oldEvent.getEventState())
                .title(request.getTitle() != null ? request.getTitle() : oldEvent.getTitle())
                .views(oldEvent.getViews())
                .build();
    }

    public EventShortDto toEventShortDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .eventDate(event.getEventDate().format(DateTimeFormatter.ofPattern(PATTERN_DATE)))
                .initiator(userMapper.toUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    public List<EventShortDto> toEventShortDtoList(List<Event> eventList) {
        return eventList.stream().map(this::toEventShortDto).collect(Collectors.toList());
    }

    public EventFullDto toEventFullDto(Event event) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn().format(DateTimeFormatter.ofPattern(PATTERN_DATE)))
                .description(event.getDescription())
                .eventDate(event.getEventDate().format(DateTimeFormatter.ofPattern(PATTERN_DATE)))
                .initiator(userMapper.toUserShortDto(event.getInitiator()))
                .location(locationMapper.toLocationDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn() != null ?
                        event.getPublishedOn().format(DateTimeFormatter.ofPattern(PATTERN_DATE)) : null)
                .requestModeration(event.getRequestModeration())
                .state(event.getEventState())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    public List<EventFullDto> toEventFullDtoList(List<Event> event) {
        return event.stream().map(this::toEventFullDto).collect(Collectors.toList());
    }
}
