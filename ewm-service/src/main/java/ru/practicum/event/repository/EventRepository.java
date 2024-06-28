package ru.practicum.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import ru.practicum.event.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    boolean existsByCategory_Id(Long categoryId);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    Optional<List<Event>> findAllByInitiatorId(Long userId, Pageable pageable);
}
