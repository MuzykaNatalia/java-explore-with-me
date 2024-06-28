package ru.practicum.controller.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.service.CompilationService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminCompilationController {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilations(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        log.info("POST /admin/compilations: request create compilation={}", newCompilationDto);
        return compilationService.createCompilations(newCompilationDto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilations(@PathVariable @NotNull @Min(1L) Long compId) {
        log.info("DELETE /admin/compilations/{compId}: request delete compilations by id={}", compId);
        compilationService.deleteCompilations(compId);
    }

    @PatchMapping("/{compId}")
    public CompilationDto updateCompilations(@Valid @RequestBody UpdateCompilationRequest updateCompilationRequest,
                                            @PathVariable @NotNull @Min(1L) Long compId) {
        log.info("PATCH /admin/compilations/{compId}: request update compilations={} by id={}",
                updateCompilationRequest, compId);
        return compilationService.updateCompilations(updateCompilationRequest, compId);
    }
}
