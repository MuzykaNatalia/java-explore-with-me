package ru.practicum.compilation.model;

import lombok.*;
import ru.practicum.event.model.Event;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "compilation")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Compilation {
    @ToString.Exclude
    @ManyToMany
    @JoinColumn(name = "event_id")
    @Column(name = "event_id")
    private Set<Event> events;
    @Column(name = "compilation_id")
    private Long compilationId;
    private Boolean pinned;
    private String title;
}
