package ru.practicum.participation.service;

import ru.practicum.participation.dto.EventRequestStatusUpdateRequest;
import ru.practicum.participation.dto.EventRequestStatusUpdateResult;
import ru.practicum.participation.dto.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {
    ParticipationRequestDto createRequestToParticipateInEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatusParticipateOwnerEvent(
            Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatus);

    ParticipationRequestDto cancelRequestToParticipateInEvent(Long userId, Long requestId);

    List<ParticipationRequestDto> getRequestsForOwnerEvent(Long userId, Long eventId);

    List<ParticipationRequestDto> getInfoOnRequestsForUserInOtherEvents(Long userId);
}
