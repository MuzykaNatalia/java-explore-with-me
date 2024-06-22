package ru.practicum.participate.model;

import lombok.*;
import ru.practicum.event.status.Status;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "participate_request")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipateRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;
    private LocalDateTime created;
    @Column(name = "event_id")
    private Long event;
    @Column(name = "requester_id")
    private Long requester;
    @Enumerated(EnumType.STRING)
    private Status status;
}
