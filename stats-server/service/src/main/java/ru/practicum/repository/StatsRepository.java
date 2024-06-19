package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {
    @Query("SELECT new ru.practicum.model.ViewStats(h.app, h.uri, COUNT(h.ip)) FROM EndpointHit AS h " +
            "WHERE (h.timestamp BETWEEN :start AND :end) AND h.uri IN (:uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.ip) DESC")
    List<ViewStats> findAllByDateBetweenAndUri(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT new ru.practicum.model.ViewStats(h.app, h.uri, COUNT(DISTINCT h.ip)) FROM EndpointHit AS h " +
            "WHERE (h.timestamp BETWEEN :start AND :end) AND h.uri IN (:uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<ViewStats> findAllByDateBetweenAndUriAndUniqueIp(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT new ru.practicum.model.ViewStats(h.app, h.uri, COUNT(h.ip)) FROM EndpointHit AS h " +
            "WHERE (h.timestamp BETWEEN :start AND :end) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.ip) DESC")
    List<ViewStats> findAllByDateBetweenStartAndEnd(LocalDateTime start, LocalDateTime end);

    @Query("SELECT new ru.practicum.model.ViewStats(h.app, h.uri, COUNT(DISTINCT h.ip)) FROM EndpointHit AS h " +
            "WHERE (h.timestamp BETWEEN :start AND :end) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<ViewStats> findAllByDateBetweenAndUniqueIp(LocalDateTime start, LocalDateTime end);
}
