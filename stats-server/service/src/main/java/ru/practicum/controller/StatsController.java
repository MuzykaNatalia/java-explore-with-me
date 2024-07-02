package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.service.StatsService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.Constant.PATTERN_DATE;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class StatsController {
    private final StatsService service;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitDto createHit(@Valid @RequestBody EndpointHitDto endpointHitDto) {
        log.info("POST /hit: request to save information that there was a request to the endpoint={}", endpointHitDto);
        return service.createHit(endpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(@RequestParam @DateTimeFormat(pattern = PATTERN_DATE) @NotNull
                                           LocalDateTime start,
                                       @RequestParam @DateTimeFormat(pattern = PATTERN_DATE) @NotNull
                                           LocalDateTime end,
                                       @RequestParam(required = false) List<String> uris,
                                       @RequestParam(defaultValue = "false", required = false) Boolean unique) {
        log.info("GET /stats: request for statistics on visits: start={}, end={}, uris={}, unique={}",
                start, end, uris, unique);
        return service.getStats(start, end, uris, unique);
    }
}
