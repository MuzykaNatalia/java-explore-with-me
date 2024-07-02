package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.exception.EndTimeBeforeStartTimeException;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class StatsServiceImplTest {
    private final StatsService statsService;
    protected LocalDateTime timestamp;
    protected EndpointHitDto endpointHitDtoOne;
    protected EndpointHitDto endpointHitDtoTwo;
    protected EndpointHitDto endpointHitDtoThree;
    protected EndpointHitDto endpointHitDtoFour;
    protected EndpointHitDto endpointHitDtoFive;
    protected EndpointHitDto endpointHitDtoSix;
    private EndpointHitDto endpointHitDtoSeven;
    private LocalDateTime start;
    private LocalDateTime end;
    private List<String> uris;

    @BeforeEach
    public void setUp() {
        timestamp = LocalDateTime.of(2024,2,1, 10, 1, 8);
        endpointHitDtoOne = new EndpointHitDto("ewm1", "/events/1", "121.0.0.2", timestamp);
        endpointHitDtoTwo = new EndpointHitDto("ewm2", "/events/2", "121.0.0.3", timestamp.plusDays(1));
        endpointHitDtoThree = new EndpointHitDto("ewm2", "/events/2", "121.0.0.3", timestamp.plusDays(2));
        endpointHitDtoFour = new EndpointHitDto("ewm3", "/events/3", "121.0.0.5", timestamp.plusDays(3));
        endpointHitDtoFive = new EndpointHitDto("ewm3", "/events/3", "121.0.0.5", timestamp.plusDays(4));
        endpointHitDtoSix = new EndpointHitDto("ewm3", "/events/3", "121.0.0.6", timestamp.plusDays(5));
        endpointHitDtoSeven = new EndpointHitDto("ewm7", "/events/7", "121.0.0.9", timestamp.plusDays(6));
        start = LocalDateTime.of(2024,1,1, 0, 0, 0);
        end = LocalDateTime.of(2024,3,1, 0, 0, 0);
        uris = List.of("/events/2", "/events/3");

        statsService.createHit(endpointHitDtoOne);
        statsService.createHit(endpointHitDtoTwo);
        statsService.createHit(endpointHitDtoThree);
        statsService.createHit(endpointHitDtoFour);
        statsService.createHit(endpointHitDtoFive);
        statsService.createHit(endpointHitDtoSix);
    }

    @DisplayName("Должен сохранить информацию о том, что к эндпоинту был запрос")
    @Test
    public void shouldCreateHit() {
        EndpointHitDto result = statsService.createHit(endpointHitDtoSeven);

        assertThat(endpointHitDtoSeven, is(equalTo(result)));
    }

    @DisplayName("Должен получить статистику по посещениям с указанием количества уникальных ip")
    @Test
    public void shouldGetStatsAndUniqueIp() {
        List<ViewStatsDto> viewStatsDtoList = List.of(
                new ViewStatsDto("ewm3", "/events/3", 2L),
                new ViewStatsDto("ewm1", "/events/1", 1L),
                new ViewStatsDto("ewm2", "/events/2", 1L));

        List<ViewStatsDto> result = statsService.getStats(start, end, null, true);

        assertThat(viewStatsDtoList, is(equalTo(result)));
    }

    @DisplayName("Должен получить статистику по посещениям")
    @Test
    public void shouldGetStats() {
        List<ViewStatsDto> viewStatsDtoList = List.of(
                new ViewStatsDto("ewm3", "/events/3", 3L),
                new ViewStatsDto("ewm2", "/events/2", 2L),
                new ViewStatsDto("ewm1", "/events/1", 1L));

        List<ViewStatsDto> result = statsService.getStats(start, end, null, false);

        assertThat(viewStatsDtoList, is(equalTo(result)));
    }

    @DisplayName("Должен получить статистику по посещениям с указанием количества уникальных ip по конкретным uris")
    @Test
    public void shouldGetStatsByUrisAndUniqueIp() {
        List<ViewStatsDto> viewStatsDtoList = List.of(
                new ViewStatsDto("ewm3", "/events/3", 2L),
                new ViewStatsDto("ewm2", "/events/2", 1L));

        List<ViewStatsDto> result = statsService.getStats(start, end, uris, true);

        assertThat(viewStatsDtoList, is(equalTo(result)));
    }

    @DisplayName("Должен получить статистику по посещениям по конкретным uris")
    @Test
    public void shouldGetStatsByUris() {
        List<ViewStatsDto> viewStatsDtoList = List.of(
                new ViewStatsDto("ewm3", "/events/3", 3L),
                new ViewStatsDto("ewm2", "/events/2", 2L));

        List<ViewStatsDto> result = statsService.getStats(start, end, uris, false);

        assertThat(viewStatsDtoList, is(equalTo(result)));
    }

    @DisplayName("Должен выдать исключение EndTimeBeforeStartTimeException, если время end начинается раньше start")
    @Test
    public void shouldNotGetStats() {
        EndTimeBeforeStartTimeException exception = assertThrows(
                EndTimeBeforeStartTimeException.class,
                () -> statsService.getStats(end, start, null, false)
        );
        assertEquals("End time cannot be before than start time", exception.getMessage());
    }
}