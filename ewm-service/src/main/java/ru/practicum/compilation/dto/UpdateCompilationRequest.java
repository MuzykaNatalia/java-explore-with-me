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
@EqualsAndHashCode // Изменение информации о подборке событий.
public class UpdateCompilationRequest { // Если поле в запросе не указано (равно null) - значит изменение этих данных не треубется.
    @NotNull
    private Set<Long> events; // Список id событий подборки для полной замены текущего списка
    @NotNull
    private Boolean pinned = false; // Закреплена ли подборка на главной странице сайта
    @NotBlank
    @Length(min = 1, max = 50)
    private String title;
}
