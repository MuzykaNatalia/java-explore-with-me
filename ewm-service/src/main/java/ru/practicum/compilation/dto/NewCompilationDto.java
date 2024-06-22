package ru.practicum.compilation.dto;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class NewCompilationDto { // Подборка событий
    @NotNull
    private Set<Long> events; // Список идентификаторов событий входящих в подборку
    @NotNull
    private Boolean pinned; // Закреплена ли подборка на главной странице сайта
    @NotBlank
    @Length(min = 1, max = 50)
    private String title; // Заголовок подборки

}
