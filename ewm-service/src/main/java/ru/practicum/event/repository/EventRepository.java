package ru.practicum.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.event.model.Event;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    boolean existsByCategory_Id(Long categoryId);

    Optional<Event> findByIdAndUser_id(Long eventId, Long userId);
}
