package ru.practicum.participate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.participate.model.ParticipateRequest;

public interface ParticipateRequestRepository extends JpaRepository<ParticipateRequest, Long> {
}
