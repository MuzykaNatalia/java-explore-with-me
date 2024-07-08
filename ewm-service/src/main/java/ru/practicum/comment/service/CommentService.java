package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentFullDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;

import java.util.List;

public interface CommentService {
    CommentDto createComment(Long eventId, Long userId, NewCommentDto newCommentDto);

    CommentDto updateComment(Long commentId, Long userId, UpdateCommentDto updateCommentDto);

    void deleteComment(Long commentId, Long userId);

    List<CommentDto> getAllCommentsUser(Long userId, Integer from, Integer size);

    CommentDto getCommentUser(Long commentId, Long userId);

    List<CommentDto> getAllCommentsByEvent(Long eventId, Integer from, Integer size);

    void deleteCommentByAdmin(Long commentId);

    List<CommentFullDto> getAllCommentsUserForAdmin(Long userId, Integer from, Integer size);

    CommentFullDto getCommentForAdmin(Long commentId);

    List<CommentFullDto> getAllCommentsByTextForAdmin(String text, Integer from, Integer size);
}
