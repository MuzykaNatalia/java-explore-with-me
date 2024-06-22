package ru.practicum.event.dto;

import lombok.*;
import ru.practicum.location.model.Location;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.event.state.EventState;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class EventFullDto {
    private Long id;
    private String annotation; // Краткое описание события
    private CategoryDto category;
    private Integer confirmedRequests; // Количество одобренных заявок на участие в данном событии
    private LocalDateTime createdOn; //Дата и время создания события
    private String description;
    private LocalDateTime eventDate; //Дата и время на которые намечено событие
    private UserShortDto initiator;
    private Location location;
    private Boolean paid; //Нужно ли оплачивать участие
    private Integer participantLimit; //Ограничение на количество участников.
    private LocalDateTime publishedOn; // Дата и время публикации события
    private Boolean requestModeration; // Нужна ли пре-модерация заявок на участие
    private EventState eventState; // Список состояний жизненного цикла события
    private String title;
    private Long views; // Количество просмотрев события
}
