package ru.practicum.participation.dto;

import lombok.*;
import ru.practicum.participation.status.Status;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class ParticipationRequestDto {
    private Long id;
    private String created;
    private Long event;
    private Long requester;
    private Status status;
}
