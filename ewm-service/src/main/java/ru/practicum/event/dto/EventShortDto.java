package ru.practicum.event.dto;

import lombok.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class EventShortDto { // Краткая информация о событии
    private Long id;
    private String annotation; // Краткое описание события
    private CategoryDto category;
    private Integer confirmedRequests; // Количество одобренных заявок на участие в данном событии
    private LocalDateTime eventDate; //Дата и время на которые намечено событие
    private UserShortDto initiator;
    private Boolean paid; //Нужно ли оплачивать участие
    private String title;
    private Long views; // Количество просмотрев события
}
