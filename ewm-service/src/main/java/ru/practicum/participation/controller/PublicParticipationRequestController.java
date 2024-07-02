package ru.practicum.participation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.participation.dto.EventRequestStatusUpdateRequest;
import ru.practicum.participation.dto.EventRequestStatusUpdateResult;
import ru.practicum.participation.dto.ParticipationRequestDto;
import ru.practicum.participation.service.ParticipationRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PublicParticipationRequestController {
    private final ParticipationRequestService participationRequestService;

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsForOwnerEvent(@PathVariable @NotNull @Min(1L) Long userId,
                                                                 @PathVariable @NotNull @Min(1L) Long eventId) {
        log.info("GET /users/{userId}/events/{eventId}/requests: list request get request for owner id={} event id={}",
                userId, eventId);
        return participationRequestService.getRequestsForOwnerEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatusParticipateOwnerEvent(
            @PathVariable @NotNull @Min(1L) Long userId,
            @PathVariable @NotNull @Min(1L) Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest eventRequestStatus) {
        log.info("PATCH /users/{userId}/events/{eventId}/requests: request update request status participate " +
                "owner id={} event id={}, new update request={}", userId, eventId, eventRequestStatus);
        return participationRequestService.updateRequestStatusParticipateOwnerEvent(userId, eventId, eventRequestStatus);
    }
}
