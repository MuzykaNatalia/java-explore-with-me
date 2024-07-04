package ru.practicum.comment.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentFullDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.constant.Constant.PATTERN_DATE;

@Component
public class CommentMapper {
    public Comment toCommentCreate(NewCommentDto newComment, Event event, User user, Comment parentComment) {
        return Comment.builder()
                .text(newComment.getText())
                .author(user)
                .event(event)
                .created(LocalDateTime.now())
                .parentComment(parentComment)
                .build();
    }

    public Comment toCommentUpdate(UpdateCommentDto updateCommentDto, Comment oldComment) {
        return Comment.builder()
                .id(oldComment.getId())
                .text(updateCommentDto.getText())
                .author(oldComment.getAuthor())
                .event(oldComment.getEvent())
                .created(oldComment.getCreated())
                .updated(LocalDateTime.now())
                .parentComment(oldComment.getParentComment())
                .build();
    }

    public CommentFullDto toCommentFullDto(Comment comment) {
        return CommentFullDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(comment.getAuthor())
                .event(comment.getEvent())
                .created(comment.getCreated().format(DateTimeFormatter.ofPattern(PATTERN_DATE)))
                .updated(comment.getUpdated() != null ?
                        comment.getUpdated().format(DateTimeFormatter.ofPattern(PATTERN_DATE)) : null)
                .parentComment(comment.getParentComment())
                .build();
    }

    public List<CommentFullDto> toCommentFullDtoList(List<Comment> comment) {
        return comment.stream().map(this::toCommentFullDto).collect(Collectors.toList());
    }

    public CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .userName(comment.getAuthor().getName())
                .created(comment.getCreated().format(DateTimeFormatter.ofPattern(PATTERN_DATE)))
                .updated(comment.getUpdated() != null ?
                        comment.getUpdated().format(DateTimeFormatter.ofPattern(PATTERN_DATE)) : null)
                .replies(comment.getReplies() != null ? toCommentDtoList(comment.getReplies()) : null)
                .build();
    }

    public List<CommentDto> toCommentDtoList(List<Comment> comment) {
        return comment.stream().map(this::toCommentDto).collect(Collectors.toList());
    }
}
