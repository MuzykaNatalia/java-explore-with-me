package ru.practicum.participation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.participation.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    Optional<List<ParticipationRequest>> findAllByRequester(Long userId);

    Optional<List<ParticipationRequest>> findAllByEvent(Long eventId);

    Optional<ParticipationRequest> findByRequesterAndEvent(Long userId, Long eventId);
}
