package ru.practicum.participate.dto;

import lombok.*;
import ru.practicum.event.status.Status;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class EventRequestStatusUpdateRequest { // Изменение статуса запроса на участие в событии текущего пользователя
    @NotNull
    private List<Long> requestIds; // Идентификаторы запросов на участие в событии текущего пользователя
    @NotNull
    private Status status; // Новый статус запроса на участие в событии текущего пользователя
}
