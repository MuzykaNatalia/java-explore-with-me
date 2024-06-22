package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import ru.practicum.location.model.Location;
import ru.practicum.event.state.AdminStateAction;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode // Данные для изменения информации о событии.
public class UpdateEventAdminRequest { // Если поле в запросе не указано (равно null) - значит изменение этих данных не треубется.
    @NotBlank
    @Length(min = 20, max = 2000)
    private String annotation; // Новое краткое описание события
    @NotNull
    @Min(1L)
    private Long category; // Новая категория
    @NotBlank
    @Length(min = 20, max = 7000)
    private String description; // Новое описание события
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate; // Новые дата и время на которые намечено событие.
    @NotNull
    private Location location; // Широта и долгота места проведения события
    private Boolean paid = false; // Новое значение флага о платности мероприятия
    @Min(0)
    private Integer participantLimit = 0; // Новый лимит пользователей
    private Boolean requestModeration = true; // Нужна ли пре-модерация заявок на участие.
    private AdminStateAction adminStateAction;
    @NotBlank
    @Length(min = 3, max = 120)
    private String title; // Заголовок события
}
