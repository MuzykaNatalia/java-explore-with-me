package ru.practicum.participate.service;

import ru.practicum.participate.dto.EventRequestStatusUpdateRequest;
import ru.practicum.participate.dto.EventRequestStatusUpdateResult;
import ru.practicum.participate.dto.ParticipationRequestDto;

import java.util.List;

public interface ParticipateRequestService {
    ParticipationRequestDto createRequestToParticipateInEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatusParticipateOwnerEvent(
            Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatus);

    ParticipationRequestDto cancelRequestToParticipateInEvent(Long userId, Long requestId);

    List<ParticipationRequestDto> getRequestsForOwnerEvent(Long userId, Long eventId);

    List<ParticipationRequestDto> getInfoOnRequestsForUserInOtherEvents(Long userId);
}
