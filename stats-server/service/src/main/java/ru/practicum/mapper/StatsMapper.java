package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StatsMapper {
    EndpointHitDto toEndpointHitDto(EndpointHit endpointHit);

    EndpointHit toEndpointHit(EndpointHitDto endpointHitDto);

    List<ViewStatsDto> toViewStatsDtoList(List<ViewStats> viewStats);

    ViewStatsDto toViewStats(ViewStats viewStats);
}
