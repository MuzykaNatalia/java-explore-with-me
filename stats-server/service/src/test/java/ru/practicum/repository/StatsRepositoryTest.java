package ru.practicum.repository;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class StatsRepositoryTest {
    @Autowired
    private StatsRepository statsRepository;
    protected LocalDateTime timestamp;
    protected EndpointHit endpointHitOne;
    protected EndpointHit endpointHitTwo;
    protected EndpointHit endpointHitThree;
    protected EndpointHit endpointHitFour;
    protected EndpointHit endpointHitFive;
    protected EndpointHit endpointHitSix;
    private LocalDateTime start;
    private LocalDateTime end;
    private List<String> uris;

    @BeforeEach
    public void setUp() {
        timestamp = LocalDateTime.of(2024,2,1, 10, 1, 8);
        endpointHitOne = new EndpointHit(null, "ewm1", "/events/1", "121.0.0.2", timestamp);
        endpointHitTwo = new EndpointHit(null,"ewm2", "/events/2", "121.0.0.3", timestamp.plusDays(1));
        endpointHitThree = new EndpointHit(null,"ewm2", "/events/2", "121.0.0.3", timestamp.plusDays(2));
        endpointHitFour = new EndpointHit(null,"ewm3", "/events/3", "121.0.0.5", timestamp.plusDays(3));
        endpointHitFive = new EndpointHit(null,"ewm3", "/events/3", "121.0.0.5", timestamp.plusDays(4));
        endpointHitSix = new EndpointHit(null,"ewm3", "/events/3", "121.0.0.6", timestamp.plusDays(5));
        start = LocalDateTime.of(2024,1,1, 0, 0, 0);
        end = LocalDateTime.of(2024,3,1, 0, 0, 0);
        uris = List.of("/events/2", "/events/3");

        statsRepository.save(endpointHitOne);
        statsRepository.save(endpointHitTwo);
        statsRepository.save(endpointHitThree);
        statsRepository.save(endpointHitFour);
        statsRepository.save(endpointHitFive);
        statsRepository.save(endpointHitSix);
    }

    @DisplayName("Должен получить статистику в определенном промежутке времени по посещениям по конкретным uris")
    @Test
    public void findAllByDateBetweenAndUri() {
        List<ViewStats> viewStatsList = List.of(
                new ViewStats("ewm3", "/events/3", 3L),
                new ViewStats("ewm2", "/events/2", 2L));

        List<ViewStats> result = statsRepository.findAllByDateBetweenAndUri(start, end, uris);

        assertNotNull(result);
        assertThat(result, Matchers.is(equalTo(viewStatsList)));
    }

    @DisplayName("Должен получить статистику в определенном промежутке времени " +
            "по посещениям с указанием количества уникальных ip по конкретным uris")
    @Test
    public void findAllByDateBetweenAndUriAndUniqueIp() {
        List<ViewStats> viewStatsList = List.of(
                new ViewStats("ewm3", "/events/3", 2L),
                new ViewStats("ewm2", "/events/2", 1L));

        List<ViewStats> result = statsRepository.findAllByDateBetweenAndUriAndUniqueIp(start, end, uris);

        assertNotNull(result);
        assertThat(result, Matchers.is(equalTo(viewStatsList)));
    }

    @DisplayName("Должен получить статистику в определенном промежутке времени по посещениям")
    @Test
    public void findAllByDateBetweenStartAndEnd() {
        List<ViewStats> viewStatsList = List.of(
                new ViewStats("ewm3", "/events/3", 3L),
                new ViewStats("ewm2", "/events/2", 2L),
                new ViewStats("ewm1", "/events/1", 1L));

        List<ViewStats> result = statsRepository.findAllByDateBetweenStartAndEnd(start, end);

        assertNotNull(result);
        assertThat(result, Matchers.is(equalTo(viewStatsList)));
    }

    @DisplayName("Должен получить статистику в определенном промежутке времени " +
            "по посещениям с указанием количества уникальных ip")
    @Test
    public void findAllByDateBetweenAndUniqueIp() {
        List<ViewStats> viewStatsList = List.of(
                new ViewStats("ewm3", "/events/3", 2L),
                new ViewStats("ewm1", "/events/1", 1L),
                new ViewStats("ewm2", "/events/2", 1L));

        List<ViewStats> result = statsRepository.findAllByDateBetweenAndUniqueIp(start, end);

        assertNotNull(result);
        assertThat(result, Matchers.is(equalTo(viewStatsList)));
    }
}