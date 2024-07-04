package ru.practicum.comment.dto;

import lombok.*;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class CommentFullDto {
    private Long id;
    private String text;
    private User author;
    private Event event;
    private String created;
    private String updated;
    private Comment parentComment;
}
