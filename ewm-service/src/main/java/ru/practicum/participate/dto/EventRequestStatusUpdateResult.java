package ru.practicum.participate.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class EventRequestStatusUpdateResult { // Результат подтверждения/отклонения заявок на участие в событии
    private List<ParticipationRequestDto> confirmedRequests; // Заявка на участие в событии подтвержденные запросы
    private List<ParticipationRequestDto> rejectedRequests; // Заявка на участие в событии отклоненные запросы
}
