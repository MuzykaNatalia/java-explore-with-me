package ru.practicum.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;

@Mapper(componentModel = "spring")
public interface EventMapper {
    @Mapping(target = "paid", source = "paid", defaultValue = "false")
    @Mapping(target = "participantLimit", source = "participantLimit", defaultValue = "0")
    @Mapping(target = "requestModeration", source = "requestModeration", defaultValue = "true")
    Event toEvent(NewEventDto newEventDto);

    EventFullDto toEventFullDto(Event event);
}
