package ru.practicum.controller.publics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.service.CompilationService;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PublicCompilationController {
    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getCompilationsEvents(@RequestParam(required = false) Boolean pinned,
                                                      @RequestParam(defaultValue = "0", required = false) Integer from,
                                                      @RequestParam(defaultValue = "10", required = false) Integer size) {
        log.info("GET /compilations: request get compilations pinned={} events, from={}, size={}", pinned, from, size);
        return compilationService.getCompilationsEvents(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getOneCompilationsEvents(@PathVariable @NotNull @Min(1L) Long compId) {
        log.info("GET /compilations/{compId}: request get one compilations id={} events", compId);
        return compilationService.getOneCompilationsEvents(compId);
    }
}
