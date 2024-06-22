package ru.practicum.participate.dto;

import lombok.*;
import ru.practicum.event.status.Status;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class ParticipationRequestDto { // Заявка на участие в событии
    private Long id; // Идентификатор заявки
    private LocalDateTime created; // Дата и время создания заявки
    private Long event; // Идентификатор события
    private Long requester; // Идентификатор пользователя, отправившего заявку
    private Status status; // Статус заявки
}
