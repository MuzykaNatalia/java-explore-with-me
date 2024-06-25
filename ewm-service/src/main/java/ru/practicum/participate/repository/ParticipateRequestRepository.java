package ru.practicum.participate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.participate.model.ParticipateRequest;

import java.util.List;
import java.util.Optional;

public interface ParticipateRequestRepository extends JpaRepository<ParticipateRequest, Long> {
    Optional<List<ParticipateRequest>> findAllByRequester(Long userId);

    Optional<List<ParticipateRequest>> findAllByEvent(Long eventId);
}
