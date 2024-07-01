package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CompilationServiceImplTest {
    private final CompilationService compilationService;

    @DisplayName("Должен создать подборку без событий")
    @Test
    public void shouldCreateCompilations() {
        NewCompilationDto newCompilationDto = new NewCompilationDto(null, false, "Open Day");

        CompilationDto compilationDto = compilationService.createCompilations(newCompilationDto);
        CompilationDto test = new CompilationDto(compilationDto.getId(), new ArrayList<>(), false, "Open Day");

        assertThat(compilationDto, is(equalTo(test)));
    }

    @DisplayName("Должен обновить подборку без событий")
    @Test
    public void shouldUpdateCompilations() {
        NewCompilationDto newCompilationDto = new NewCompilationDto(null, false, "Open Day");

        CompilationDto compilationDto = compilationService.createCompilations(newCompilationDto);
        CompilationDto test = new CompilationDto(compilationDto.getId(), new ArrayList<>(), false, "Open Day");

        assertThat(compilationDto, is(equalTo(test)));

        UpdateCompilationRequest updateCompilationRequest = new UpdateCompilationRequest(null,
                true, "Open night");

        CompilationDto compilationDtoTwo = compilationService.updateCompilations(updateCompilationRequest, test.getId());
        CompilationDto testTwo = new CompilationDto(compilationDto.getId(), new ArrayList<>(), true, "Open night");

        assertThat(compilationDtoTwo, is(equalTo(testTwo)));
    }

    @DisplayName("Должен удалить подборку без событий")
    @Test
    public void shouldDeleteCompilations() {
        NewCompilationDto newCompilationDto = new NewCompilationDto(null, false, "Open Day");

        CompilationDto compilationDto = compilationService.createCompilations(newCompilationDto);
        CompilationDto test = new CompilationDto(compilationDto.getId(), new ArrayList<>(), false, "Open Day");

        assertThat(compilationDto, is(equalTo(test)));

        compilationService.deleteCompilations(compilationDto.getId());
        List<CompilationDto> result = compilationService.getCompilationsEvents(false, 0, 10);

        assertThat(result, hasSize(0));
    }

    @DisplayName("Должен выдать одну подборку без событий")
    @Test
    public void shouldGetOneCompilationsEvents() {
        NewCompilationDto newCompilationDto = new NewCompilationDto(null, false, "Open Day");

        CompilationDto compilationDto = compilationService.createCompilations(newCompilationDto);
        CompilationDto result = compilationService.getOneCompilationsEvents(compilationDto.getId());

        assertThat(result, is(equalTo(compilationDto)));
    }

    @DisplayName("Должен выдать подборки без событий")
    @Test
    public void shouldGetCompilationsEvents() {
        NewCompilationDto newCompilationDto = new NewCompilationDto(null, false, "Open Day");

        CompilationDto compilationDto = compilationService.createCompilations(newCompilationDto);
        List<CompilationDto> result = compilationService.getCompilationsEvents(false, 0, 10);

        assertThat(result, is(equalTo(List.of(compilationDto))));
    }
}