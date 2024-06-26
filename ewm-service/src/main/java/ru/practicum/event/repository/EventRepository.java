package ru.practicum.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.event.model.Event;
import ru.practicum.event.state.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    boolean existsByCategory_Id(Long categoryId);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    Optional<List<Event>> findByInitiatorId(Long userId, Pageable pageable);

    Optional<List<Event>> findAllByCategoryIdIn(List<Long> categories, Pageable pageable);

    Optional<List<Event>> findAllByInitiatorIdIn(List<Long> users, Pageable pageable);

    Optional<List<Event>> findAllByEventStateIn(List<EventState> states, Pageable pageable);

    Optional<List<Event>> findByInitiatorIdInAndEventStateInAndCategoryIdIn(List<Long> users, List<EventState> states,
                                                                            List<Long> categories, Pageable pageable);

    Optional<List<Event>> findByInitiatorIdInAndEventStateIn(List<Long> users, List<EventState> states,
                                                             Pageable pageable);

    Optional<List<Event>> findByEventStateInAndCategoryIdIn(List<EventState> states, List<Long> categories,
                                                            Pageable pageable);

    Optional<List<Event>> findByInitiatorIdInAndCategoryIdIn(List<Long> users, List<Long> categories, Pageable pageable);

    List<Event> findAllByEventState(EventState state, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.eventDate > :rangeStart AND e.eventDate < :rangeEnd")
    Optional<List<Event>> findByDateStartAndEnd(LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.initiator.id IN :users " +
            "AND (e.eventDate > :rangeStart AND e.eventDate < :rangeEnd)")
    Optional<List<Event>> findByUserIdAndDateStartAndEnd(List<Long> users, LocalDateTime rangeStart,
                                                          LocalDateTime rangeEnd, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.eventState IN :states " +
            "AND (e.eventDate > :rangeStart AND e.eventDate < :rangeEnd)")
    Optional<List<Event>> findByStatesAndDateStartAndEnd(List<EventState> states, LocalDateTime rangeStart,
                                                         LocalDateTime rangeEnd, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.category.id IN :categories " +
            "AND (e.eventDate > :rangeStart AND e.eventDate < :rangeEnd)")
    Optional<List<Event>> findByCategoriesAndDateStartAndEnd(List<Long> categories, LocalDateTime rangeStart,
                                                             LocalDateTime rangeEnd, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.initiator.id IN :users AND e.eventState IN :states " +
            "AND e.category.id IN :categories AND (e.eventDate > :rangeStart AND e.eventDate < :rangeEnd)")
    Optional<List<Event>> findByUserIdAndStatesAndCategoriesAndDateStartAndEnd(List<Long> users, List<EventState> states,
                                                                               List<Long> categories,
                                                                               LocalDateTime rangeStart,
                                                                               LocalDateTime rangeEnd,
                                                                               Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.initiator.id IN :users AND e.eventState IN :states " +
            "AND (e.eventDate > :rangeStart AND e.eventDate < :rangeEnd)")
    Optional<List<Event>> findByUserIdAndStatesAndDateStartAndEnd(List<Long> users, List<EventState> states,
                                                                  LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                                  Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.eventState IN :states AND e.category.id IN :categories " +
            "AND (e.eventDate > :rangeStart AND e.eventDate < :rangeEnd)")
    Optional<List<Event>> findByStatesAndCategoriesAndDateStartAndEnd(List<EventState> states, List<Long> categories,
                                                                      LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                                      Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.initiator.id IN :users AND e.category.id IN :categories " +
            "AND (e.eventDate > :rangeStart AND e.eventDate < :rangeEnd)")
    Optional<List<Event>> findByUserIdAndCategoriesAndDateStartAndEnd(List<Long> users, List<Long> categories,
                                                                      LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                                      Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.eventState = 'PUBLISHED' " +
            "AND (LOWER(e.annotation) LIKE CONCAT('%',LOWER(:text),'%') " +
            "OR LOWER(e.description) LIKE CONCAT('%',LOWER(:textDuplicate),'%')) " +
            "AND e.category.id IN :categories AND e.paid = :paid " +
            "AND (e.eventDate > :rangeStart AND e.eventDate < :rangeEnd) " +
            "AND (e.participantLimit = 0 OR e.participantLimit > e.confirmedRequests)")
    Optional<List<Event>> findPublishedByTextAndCategoriesAndTimeAndPaid(String text, String textDuplicate,
                                                                         List<Long> categories, Boolean paid,
                                                                         LocalDateTime rangeStart,
                                                                         LocalDateTime rangeEnd, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.eventState = 'PUBLISHED' " +
            "AND (LOWER(e.annotation) LIKE CONCAT('%',LOWER(:text),'%') " +
            "OR LOWER(e.description) LIKE CONCAT('%',LOWER(:textDuplicate),'%')) " +
            "AND e.category.id IN :categories AND e.paid = :paid " +
            "AND e.eventDate >= :current " +
            "AND (e.participantLimit = 0 OR e.participantLimit > e.confirmedRequests)")
    Optional<List<Event>> findPublishedByTextAndCategoriesAndPaid(String text, String textDuplicate,
                                                                  List<Long> categories, Boolean paid,
                                                                  LocalDateTime current, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.eventState = 'PUBLISHED' " +
            "AND (LOWER(e.annotation) LIKE CONCAT('%',LOWER(:text),'%') " +
            "OR LOWER(e.description) LIKE CONCAT('%',LOWER(:textDuplicate),'%')) " +
            "AND e.category.id IN :categories " +
            "AND (e.eventDate > :rangeStart AND e.eventDate < :rangeEnd) " +
            "AND (e.participantLimit = 0 OR e.participantLimit > e.confirmedRequests)")
    Optional<List<Event>> findPublishedByTextAndCategoriesAndTime(String text, String textDuplicate,
                                                                  List<Long> categories, LocalDateTime rangeStart,
                                                                  LocalDateTime rangeEnd, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.eventState = 'PUBLISHED' " +
            "AND (LOWER(e.annotation) LIKE CONCAT('%',LOWER(:text),'%') " +
            "OR LOWER(e.description) LIKE CONCAT('%',LOWER(:textDuplicate),'%')) " +
            "AND e.category.id IN :categories " +
            "AND e.eventDate > :current " +
            "AND (e.participantLimit = 0 OR e.participantLimit > e.confirmedRequests)")
    Optional<List<Event>> findPublishedByTextAndCategories(String text, String textDuplicate, List<Long> categories,
                                                           LocalDateTime current, Pageable pageable);


    @Query("SELECT e FROM Event e WHERE e.eventState = 'PUBLISHED' " +
            "AND e.category.id IN :categories AND e.paid = :paid " +
            "AND (e.eventDate > :rangeStart AND e.eventDate < :rangeEnd) " +
            "AND (e.participantLimit = 0 OR e.participantLimit > e.confirmedRequests)")
    Optional<List<Event>> findPublishedByCategoriesAndPaid(List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                                           LocalDateTime rangeEnd, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.eventState = 'PUBLISHED' " +
            "AND e.category.id IN :categories AND e.paid = :paid " +
            "AND e.eventDate > :current " +
            "AND (e.participantLimit = 0 OR e.participantLimit > e.confirmedRequests)")
    Optional<List<Event>> findPublishedByCategoriesAndPaid(List<Long> categories, Boolean paid,
                                                           LocalDateTime current, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.eventState = 'PUBLISHED' " +
            "AND (LOWER(e.annotation) LIKE CONCAT('%',LOWER(:text),'%') " +
            "OR LOWER(e.description) LIKE CONCAT('%',LOWER(:textDuplicate),'%')) " +
            "AND e.paid = :paid AND (e.eventDate > :rangeStart AND e.eventDate < :rangeEnd) " +
            "AND (e.participantLimit = 0 OR e.participantLimit > e.confirmedRequests)")
    Optional<List<Event>> findPublishedByTextAndTimeAndPaid(String text, String textDuplicate,
                                                            Boolean paid, LocalDateTime rangeStart,
                                                            LocalDateTime rangeEnd, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.eventState = 'PUBLISHED' " +
            "AND (LOWER(e.annotation) LIKE CONCAT('%',LOWER(:text),'%') " +
            "OR LOWER(e.description) LIKE CONCAT('%',LOWER(:textDuplicate),'%')) " +
            "AND e.paid = :paid AND e.eventDate > :current " +
            "AND (e.participantLimit = 0 OR e.participantLimit > e.confirmedRequests)")
    Optional<List<Event>> findPublishedByTextAndPaid(String text, String textDuplicate, Boolean paid,
                                                     LocalDateTime current, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.eventState = 'PUBLISHED' " +
            "AND (LOWER(e.annotation) LIKE CONCAT('%',LOWER(:text),'%') " +
            "OR LOWER(e.description) LIKE CONCAT('%',LOWER(:textDuplicate),'%')) " +
            "AND (e.eventDate > :rangeStart AND e.eventDate < :rangeEnd) " +
            "AND (e.participantLimit = 0 OR e.participantLimit > e.confirmedRequests)")
    Optional<List<Event>> findPublishedByText(String text, String textDuplicate, LocalDateTime rangeStart,
                                              LocalDateTime rangeEnd, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.eventState = 'PUBLISHED' " +
            "AND (LOWER(e.annotation) LIKE CONCAT('%',LOWER(:text),'%') " +
            "OR LOWER(e.description) LIKE CONCAT('%',LOWER(:textDuplicate),'%')) " +
            "AND e.eventDate > :current " +
            "AND (e.participantLimit = 0 OR e.participantLimit > e.confirmedRequests)")
    Optional<List<Event>> findPublishedByText(String text, String textDuplicate,
                                              LocalDateTime current, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.eventState = 'PUBLISHED' " +
            "AND e.category.id IN :categories " +
            "AND (e.eventDate > :rangeStart AND e.eventDate < :rangeEnd) " +
            "AND (e.participantLimit = 0 OR e.participantLimit > e.confirmedRequests)")
    Optional<List<Event>> findPublishedByCategoriesAndTime(List<Long> categories, LocalDateTime rangeStart,
                                                           LocalDateTime rangeEnd, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.eventState = 'PUBLISHED' " +
            "AND e.category.id IN :categories " +
            "AND e.eventDate > :current " +
            "AND (e.participantLimit = 0 OR e.participantLimit > e.confirmedRequests)")
    Optional<List<Event>> findPublishedByCategories(List<Long> categories, LocalDateTime current, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.paid = :paid " +
            "AND (e.eventDate > :rangeStart AND e.eventDate < :rangeEnd) " +
            "AND (e.participantLimit = 0 OR e.participantLimit > e.confirmedRequests)")
    Optional<List<Event>> findPublishedByPaid(Boolean paid, LocalDateTime rangeStart,
                                              LocalDateTime rangeEnd, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.paid = :paid " +
            "AND e.eventDate > :current " +
            "AND (e.participantLimit = 0 OR e.participantLimit > e.confirmedRequests)")
    Optional<List<Event>> findPublishedByPaid(Boolean paid, LocalDateTime current, Pageable pageable);
}
