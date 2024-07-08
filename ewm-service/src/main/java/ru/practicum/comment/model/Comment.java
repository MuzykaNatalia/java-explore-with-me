package ru.practicum.comment.model;

import lombok.*;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comment")
@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;
    private String text;
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
    private LocalDateTime created;
    private LocalDateTime updated;
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Comment parentComment;
    @ToString.Exclude
    @Transient
    public Comment reply;
}
