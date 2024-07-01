package ru.practicum.compilation.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CompilationMapper {
    private final EventMapper eventMapper;

    public Compilation toCompilation(NewCompilationDto newCompilationDto, List<Event> events) {
        return Compilation.builder()
                .events(events)
                .pinned(newCompilationDto.getPinned() != null ? newCompilationDto.getPinned() : false)
                .title(newCompilationDto.getTitle())
                .build();
    }

    public CompilationDto toCompilationDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .events(compilation.getEvents() != null
                        ? eventMapper.toEventShortDtoList(compilation.getEvents()) : new ArrayList<>())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }

    public List<CompilationDto> toCompilationDtoList(List<Compilation> compilationList) {
        return compilationList.stream().map(this::toCompilationDto).collect(Collectors.toList());
    }

    public Compilation toCompilation(Compilation oldCompilation, UpdateCompilationRequest updateCompilationRequest,
                                     List<Event> events) {
        return Compilation.builder()
                .id(oldCompilation.getId())
                .events(updateCompilationRequest.getEvents() == null ? oldCompilation.getEvents() : events)
                .pinned(updateCompilationRequest.getPinned() == null
                        ? oldCompilation.getPinned() : updateCompilationRequest.getPinned())
                .title(updateCompilationRequest.getTitle() == null
                        ? oldCompilation.getTitle() : updateCompilationRequest.getTitle())
                .build();
    }
}
