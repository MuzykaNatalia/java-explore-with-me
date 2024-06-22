package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {
    //для работы с подборками
    // получение подборок событий
    // В случае, если по заданным фильтрам не найдено ни одной подборки, возвращает пустой список
    @Override// 400, 409 //подборка может не содержать событий
    public CompilationDto createCompilations(NewCompilationDto newCompilationDto) {
        return null;
    }

    @Override// 404
    public CompilationDto updateCompilations(UpdateCompilationRequest updateCompilationRequest, Long compId) {
        return null;
    }

    @Override//404
    public void deleteCompilations(Long compId) {

    }

    // В случае, если подборки с заданным id не найдено, возвращает статус код 404
    @Override // 400, 404
    public CompilationDto getOneCompilationsEvents(Long compId) {
        return null;
    }

    @Override// 400
    public List<CompilationDto> getCompilationsEvents(Boolean pinned, Integer from, Integer size) {
        return null;
    }
}
