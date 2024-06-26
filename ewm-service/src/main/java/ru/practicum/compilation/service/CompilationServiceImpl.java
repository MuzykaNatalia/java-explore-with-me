package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exceptions.NotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Transactional
    @Override
    public CompilationDto createCompilations(NewCompilationDto newCompilationDto) {
        List<Event> events = new ArrayList<>();
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            events = eventRepository.findAllById(newCompilationDto.getEvents());
        }

        Compilation created = compilationRepository.save(compilationMapper.toCompilation(newCompilationDto,
                events.isEmpty() ? null : events));
        log.info("A new compilation of events has been created={}", created);
        return compilationMapper.toCompilationDto(created);
    }

    @Transactional
    @Override
    public CompilationDto updateCompilations(UpdateCompilationRequest updateCompilationRequest, Long compId) {
        Compilation compilation = getCompilation(compId);
        List<Event> events = new ArrayList<>();
        if (updateCompilationRequest.getEvents() != null) {
            events = eventRepository.findAllById(updateCompilationRequest.getEvents());
        }
        compilation = compilationMapper.toCompilation(compilation, updateCompilationRequest, events);
        Compilation updated = compilationRepository.save(compilation);
        log.info("Compilation of events id={} updated={}", compId, updated);
        return compilationMapper.toCompilationDto(updated);
    }

    @Transactional
    @Override
    public void deleteCompilations(Long compId) {
        Compilation compilation = getCompilation(compId);
        compilationRepository.delete(compilation);
        log.info("Compilation of events id={} deleted", compId);
    }

    @Transactional(readOnly = true)
    @Override
    public CompilationDto getOneCompilationsEvents(Long compId) {
        Compilation compilation = getCompilation(compId);
        log.info("Received a selection of events by id={}", compId);
        return compilationMapper.toCompilationDto(compilation);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CompilationDto> getCompilationsEvents(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findAllByPinned(pinned, pageable)
                    .orElse(new ArrayList<>());
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }
        log.info("Received a selection of events={}", compilations);
        return compilationMapper.toCompilationDtoList(compilations);
    }

    private Compilation getCompilation(Long compId) {
        return compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Compilation not found", Collections.singletonList("Invalid id")));
    }
}
