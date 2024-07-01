package ru.practicum.compilation.service;

import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto createCompilations(NewCompilationDto newCompilationDto);

    CompilationDto updateCompilations(UpdateCompilationRequest updateCompilationRequest, Long compId);

    void deleteCompilations(Long compId);

    CompilationDto getOneCompilationsEvents(Long compId);

    List<CompilationDto> getCompilationsEvents(Boolean pinned, Integer from, Integer size);
}
