package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.exception.EndTimeBeforeStartTimeException;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;
    private final StatsMapper statsMapper;

    @Transactional
    @Override
    public EndpointHitDto createHit(EndpointHitDto endpointHitDto) {
        EndpointHit endpointHitSave = statsRepository.save(statsMapper.toEndpointHit(endpointHitDto));
        log.info("POST /hit: create hit={}", endpointHitSave);
        return statsMapper.toEndpointHitDto(endpointHitSave);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        checkTime(start, end);
        List<ViewStats> viewStats;

        if ((uris == null || uris.isEmpty()) || uris.get(0).equals("/events")) {
            viewStats = Boolean.TRUE.equals(unique)
                    ? statsRepository.findAllByDateBetweenAndUniqueIp(start, end)
                    : statsRepository.findAllByDateBetweenStartAndEnd(start, end);
            return statsMapper.toViewStatsDtoList(viewStats);
        }

        viewStats = Boolean.TRUE.equals(unique)
                ? statsRepository.findAllByDateBetweenAndUriAndUniqueIp(start, end, uris)
                : statsRepository.findAllByDateBetweenAndUri(start, end, uris);

        log.info("GET /stats: visit statistics received");
        return statsMapper.toViewStatsDtoList(viewStats);
    }

    private void checkTime(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            log.warn("GET /stats: End time cannot be before or equals than start time: start={}, end={}", start, end);
            throw new EndTimeBeforeStartTimeException("End time cannot be before than start time",
                    Collections.singletonList("Incorrect data"));
        }
    }
}
