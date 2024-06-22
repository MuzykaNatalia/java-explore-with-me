package ru.practicum.event.service;

import ru.practicum.event.sort.SortEvent;
import ru.practicum.event.dto.*;
import ru.practicum.event.state.EventState;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd, Boolean onlyAvailable, SortEvent sort,
                                  Integer from, Integer size, HttpServletRequest request);

    EventFullDto getOneEvent(Long id, HttpServletRequest request);

    List<EventFullDto> getEventsForAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                         Integer from, Integer size);

    EventFullDto updateEventByAdmin(UpdateEventAdminRequest updateEventAdminRequest, Long eventId);

    List<EventShortDto> getOwnerEvents(Long userId, Integer from, Integer size);

    EventFullDto createOwnerEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getOwnerOneEvent(Long userId, Long eventId);

    EventFullDto updateOwnerEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);
}
