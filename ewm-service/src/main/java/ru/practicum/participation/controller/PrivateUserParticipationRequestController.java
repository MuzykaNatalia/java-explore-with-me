package ru.practicum.participation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.participation.dto.ParticipationRequestDto;
import ru.practicum.participation.service.ParticipationRequestService;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PrivateUserParticipationRequestController {
    private final ParticipationRequestService participationRequestService;

    @GetMapping
    public List<ParticipationRequestDto> getInfoOnRequestsForUserInOtherEvents(@PathVariable @NotNull
                                                                               @Min(1L) Long userId) {
        log.info("GET /users/{userId}/requests: request get info on requests for user by id={} " +
                "in other events", userId);
        return participationRequestService.getInfoOnRequestsForUserInOtherEvents(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequestToParticipateInEvent(@PathVariable @NotNull @Min(1L) Long userId,
                                                                     @RequestParam(required = false) Long eventId) {
        log.info("POST /users/{userId}/requests: request create request to participate in " +
                "event id={} for user id={} ", eventId, userId);
        return participationRequestService.createRequestToParticipateInEvent(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequestToParticipateInEvent(@PathVariable @NotNull @Min(1L) Long userId,
                                                                     @PathVariable @NotNull @Min(1L) Long requestId) {
        log.info("PATCH /users/{userId}/requests/{requestId}/cancel: request cancel request id={} to participate " +
                "in event id={}", requestId, userId);
        return participationRequestService.cancelRequestToParticipateInEvent(userId, requestId);
    }
}
