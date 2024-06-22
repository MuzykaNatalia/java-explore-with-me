package ru.practicum.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;

@Mapper(componentModel = "spring")
public interface CompilationMapper {
    @Mapping(target = "pinned", source = "pinned", defaultValue = "false")
    Compilation toCompilation(NewCompilationDto newCompilationDto);
}
