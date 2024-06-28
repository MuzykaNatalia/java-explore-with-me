package ru.practicum.controller.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.participate.dto.EventRequestStatusUpdateRequest;
import ru.practicum.participate.dto.EventRequestStatusUpdateResult;
import ru.practicum.participate.dto.ParticipationRequestDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.service.EventService;
import ru.practicum.participate.service.ParticipateRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PrivateUserEventController {
    private final EventService eventService;
    private final ParticipateRequestService participateRequestService;

    @GetMapping
    public List<EventShortDto> getOwnerEvents(@PathVariable @Min(1L) Long userId,
                                              @RequestParam(required = false, defaultValue = "0") Integer from,
                                              @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("GET /users/{userId}/events: request get owner id={} events, from={}, size={}", userId, from, size);
        return eventService.getOwnerEvents(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createOwnerEvent(@PathVariable @NotNull @Min(1L) Long userId,
                                         @Valid @RequestBody NewEventDto newEventDto) {
        log.info("POST /users/{userId}/events: request create owner id={} event={}", userId, newEventDto);
        return eventService.createOwnerEvent(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getOwnerOneEvent(@PathVariable @NotNull @Min(1L) Long userId,
                                         @PathVariable @NotNull @Min(1L) Long eventId) {
        log.info("GET /users/{userId}/events/{eventId}: request get owner id={} one event id={}", userId, eventId);
        return eventService.getOwnerOneEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateOwnerEvent(@PathVariable @NotNull @Min(1L) Long userId,
                                         @PathVariable @NotNull @Min(1L) Long eventId,
                                         @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        log.info("PATCH /users/{userId}/events/{eventId}: request update owner id={} event id={}, " +
                "new update event={}", userId, eventId, updateEventUserRequest);
        return eventService.updateOwnerEvent(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestForOwnerEvent(@PathVariable @NotNull @Min(1L) Long userId,
                                                           @PathVariable @NotNull @Min(1L) Long eventId) {
        log.info("GET /users/{userId}/events/{eventId}/requests: list request get request for owner id={} event id={}",
                userId, eventId);
        return participateRequestService.getRequestForOwnerEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatusParticipateOwnerEvent(
                                            @PathVariable @NotNull @Min(1L) Long userId,
                                            @PathVariable @NotNull @Min(1L) Long eventId,
                                            @Valid @RequestBody EventRequestStatusUpdateRequest eventRequestStatus) {
        log.info("PATCH /users/{userId}/events/{eventId}/requests: request update request status participate " +
                "owner id={} event id={}, new update request={}", userId, eventId, eventRequestStatus);
        return participateRequestService.updateRequestStatusParticipateOwnerEvent(userId, eventId, eventRequestStatus);
    }
}
