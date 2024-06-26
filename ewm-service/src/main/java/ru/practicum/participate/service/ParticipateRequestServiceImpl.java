package ru.practicum.participate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.state.EventState;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.participate.dto.EventRequestStatusUpdateRequest;
import ru.practicum.participate.dto.EventRequestStatusUpdateResult;
import ru.practicum.participate.dto.ParticipationRequestDto;
import ru.practicum.participate.mapper.ParticipationRequestMapper;
import ru.practicum.participate.model.ParticipateRequest;
import ru.practicum.participate.repository.ParticipateRequestRepository;
import ru.practicum.status.Status;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ru.practicum.event.state.EventState.PUBLISHED;
import static ru.practicum.status.Status.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipateRequestServiceImpl implements ParticipateRequestService {
    private final ParticipateRequestRepository participateRequestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestMapper participationRequestMapper;

    @Transactional
    @Override
    public ParticipationRequestDto createRequestToParticipateInEvent(Long userId, Long eventId) {
        validationEventId(eventId);
        User requester = getUser(userId);
        Event event = getEvent(eventId);

        getExceptionIfEventNotPublished(event.getEventState());
        getExceptionIfExceededRequestLimit(event.getConfirmedRequests(), event.getParticipantLimit());
        getExceptionIfInitiatorEqualsRequester(event.getInitiator().getId(), requester.getId());

        ParticipateRequest participateRequest = getNewParticipateRequest(
                userId, eventId, event.getParticipantLimit(), event.getRequestModeration());
        ParticipateRequest createdParticipateRequest = participateRequestRepository.save(participateRequest);

        if (createdParticipateRequest.getStatus().equals(CONFIRMED)) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        log.info("Created request to participate in event id={}, requester id={} : {}",
                eventId, userId, createdParticipateRequest);
        return participationRequestMapper.toParticipationRequestDto(createdParticipateRequest);
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateRequestStatusParticipateOwnerEvent(
            Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatus) {
        Event event = getEvent(eventId);
        getExceptionIfEventIsNotThisUser(event.getInitiator(), userId);
        getExceptionIfExceededRequestLimit(event.getConfirmedRequests(), event.getParticipantLimit());

        boolean isApplicationConfirmationRequired = (event.getParticipantLimit() != 0)
                || event.getRequestModeration().equals(true);

        if (!isApplicationConfirmationRequired) {
            log.info("Confirmation of applications is not required");
            return new EventRequestStatusUpdateResult(new ArrayList<>(), new ArrayList<>());
        }

        List<ParticipateRequest> participateRequests = participateRequestRepository
                .findAllById(eventRequestStatus.getRequestIds());
        EventRequestStatusUpdateResult updated = setRequestStatusAndSave(event, eventId, participateRequests,
                eventRequestStatus);

        log.info("The status of the request to participate in the event id={} has been updated : {}",
                eventId, updated);
        return updated;
    }

    @Transactional
    @Override
    public ParticipationRequestDto cancelRequestToParticipateInEvent(Long userId, Long requestId) {
        ParticipateRequest participateRequest = getParticipateRequest(requestId);
        getExceptionIfRequestIsNotThisUser(userId, participateRequest.getRequester());

        if (participateRequest.getStatus().equals(CONFIRMED)) {
            Event event = getEvent(participateRequest.getEvent());
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
            eventRepository.save(event);
        }

        participateRequest.setStatus(CANCELED);
        ParticipateRequest cancelRequest = participateRequestRepository.save(participateRequest);

        log.info("The request id={} to participate in the event has been canceled : {}", requestId, cancelRequest);
        return participationRequestMapper.toParticipationRequestDto(cancelRequest);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> getRequestForOwnerEvent(Long userId, Long eventId) {
        Event event = getEvent(eventId);
        getExceptionIfEventIsNotThisUser(event.getInitiator(), userId);
        List<ParticipateRequest> participateRequests = participateRequestRepository
                .findAllByEvent(eventId).orElse(new ArrayList<>());

        log.info("Requests to participate in the event id={} have been received by the event initiator id={}",
                eventId, userId);
        return participationRequestMapper.toParticipationRequestDtoList(participateRequests);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> getInfoOnRequestsForUserInOtherEvents(Long userId) {
        List<ParticipateRequest> participateRequests = participateRequestRepository
                .findAllByRequester(userId).orElse(new ArrayList<>());

        log.info("Information about user id={} requests in other events was received : {}", userId, participateRequests);
        return participationRequestMapper.toParticipationRequestDtoList(participateRequests);
    }

    private EventRequestStatusUpdateResult setRequestStatusAndSave(Event event, Long eventId,
                                                                   List<ParticipateRequest> participateRequests,
                                                                   EventRequestStatusUpdateRequest eventRequestStatus) {
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
        List<ParticipateRequest> updatedRequests = new ArrayList<>();

        for (ParticipateRequest request : participateRequests) {
            getExceptionIfStatusRequestNotPending(request.getStatus());
            boolean isPotentialParticipant = request.getEvent().equals(eventId)
                    && eventRequestStatus.getStatus().equals(CONFIRMED)
                    && (event.getParticipantLimit() == 0 || event.getConfirmedRequests() < event.getParticipantLimit());
            if (isPotentialParticipant) {
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                request.setStatus(CONFIRMED);
                updatedRequests.add(request);
                confirmedRequests.add(participationRequestMapper.toParticipationRequestDto(request));
                continue;
            }

            request.setStatus(REJECTED);
            updatedRequests.add(request);
            rejectedRequests.add(participationRequestMapper.toParticipationRequestDto(request));
        }
        eventRepository.save(event);
        participateRequestRepository.saveAll(updatedRequests);
        return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }

    private void getExceptionIfEventIsNotThisUser(User initiator, Long userId) {
        if (!initiator.getId().equals(userId)) {
            throw new ConflictException("The user is not the initiator of the event",
                    Collections.singletonList("Incorrect event id or user id"));
        }
    }

    private void getExceptionIfRequestIsNotThisUser(Long userId, Long requesterId) {
        if (!userId.equals(requesterId)) {
            throw new ConflictException("Request is not this user",
                    Collections.singletonList("Incorrect request or user id"));
        }
    }

    private ParticipateRequest getNewParticipateRequest(Long userId, Long eventId,
                                                        Integer participantLimit, Boolean requestModeration) {
        return new ParticipateRequest(null, LocalDateTime.now(), eventId, userId,
                participantLimit == 0 ? CONFIRMED :
                requestModeration.equals(true) ? PENDING : CONFIRMED);
    }

    private void getExceptionIfExceededRequestLimit(Integer confirmedRequests, Integer participantLimit) {
        if (confirmedRequests.equals(participantLimit) && participantLimit != 0) {
            throw new ConflictException("The event has reached the limit of requests for participation",
                    Collections.singletonList("Participant limit exceeded"));
        }
    }

    private void getExceptionIfEventNotPublished(EventState eventState) {
        if (!eventState.equals(PUBLISHED)) {
            throw new ConflictException("You cannot participate in an unpublished event",
                    Collections.singletonList("Event is not PUBLISHED"));
        }
    }

    private void getExceptionIfStatusRequestNotPending(Status state) {
        if (!state.equals(PENDING)) {
            throw new ConflictException("Status can be changed only for pending applications",
                    Collections.singletonList("Status request is not PENDING"));
        }
    }

    private void getExceptionIfInitiatorEqualsRequester(Long initiatorId, Long requesterId) {
        if (initiatorId.equals(requesterId)) {
            throw new ConflictException("The event initiator cannot add a request to participate in his event",
                    Collections.singletonList("Initiator equals requester"));
        }
    }

    private void validationEventId(Long eventId) {
        if (eventId == null || eventId < 1) {
            throw new ValidationException("Incorrect data",
                    List.of("Event id must be passed", "Event id must be non-negative"));
        }
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

    private ParticipateRequest getParticipateRequest(Long requestId) {
        return participateRequestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("Participate request with id=" + requestId + " was not found",
                        Collections.singletonList("Participate request id does not exist")));
    }
}
