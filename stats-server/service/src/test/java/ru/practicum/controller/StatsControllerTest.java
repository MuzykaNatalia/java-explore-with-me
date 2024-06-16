package ru.practicum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.service.StatsService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static ru.practicum.Constant.FORMATTER;

@WebMvcTest(StatsController.class)
public class StatsControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private StatsService statsService;
    @Autowired
    private ObjectMapper mapper;

    @DisplayName("Должен сохранить информацию о том, что к эндпоинту был запрос")
    @Test
    @SneakyThrows
    public void shouldCreateHit() {
        EndpointHitDto endpointHitDto = new EndpointHitDto("ewm-main-service", "/events/1", "121.0.0.1",
                LocalDateTime.of(2024,6,16, 10, 51, 2));

        when(statsService.createHit(any())).thenReturn(endpointHitDto);

        mvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(endpointHitDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.app").value(endpointHitDto.getApp()))
                .andExpect(jsonPath("$.uri").value(endpointHitDto.getUri()))
                .andExpect(jsonPath("$.ip").value(endpointHitDto.getIp()))
                .andExpect(jsonPath("$.timestamp").value(endpointHitDto.getTimestamp().format(FORMATTER)));

        verify(statsService).createHit(any());
    }

    @DisplayName("Должен получить статистику по посещениям с указанием количества уникальных ip по конкретным uris")
    @Test
    @SneakyThrows
    public void shouldGetStats() {
        List<ViewStatsDto> viewStatsDto = List.of(new ViewStatsDto("ewm-main-service", "/events/1", 1L));
        String start = "2024-05-16 10:51:02";
        String end = "2024-06-16 10:51:02";
        String uris = "/events/1";
        Boolean unique = true;

        when(statsService.getStats(any(), any(), anyList(), anyBoolean())).thenReturn(viewStatsDto);

        mvc.perform(get("/stats?start=" + start + "&end=" + end + "&uris=" + uris + "&unique=" + unique,
                        start, end, uris, unique)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].app").value(viewStatsDto.get(0).getApp()))
                .andExpect(jsonPath("$[0].uri").value(viewStatsDto.get(0).getUri()))
                .andExpect(jsonPath("$[0].hits").value(viewStatsDto.get(0).getHits()));

        verify(statsService).getStats(any(), any(), anyList(), anyBoolean());
    }
}