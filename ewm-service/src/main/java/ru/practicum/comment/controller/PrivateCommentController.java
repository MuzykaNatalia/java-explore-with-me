package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.service.CommentService;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.dto.NewCommentDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PrivateCommentController {
    private final CommentService commentService;

    @PostMapping("/event/{eventId}/user/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable @NotNull @Positive Long eventId,
                                    @PathVariable @NotNull @Positive Long userId,
                                    @Valid @RequestBody NewCommentDto newCommentDto) {
        log.info("POST /comment/event/{eventId}/user/{userId}: request to create a comment for event id={} by user id={}",
                eventId, userId);
        return commentService.createComment(eventId, userId, newCommentDto);
    }

    @PatchMapping("/{commentId}/user/{userId}")
    public CommentDto updateComment(@PathVariable @NotNull @Positive Long commentId,
                                    @PathVariable @NotNull @Positive Long userId,
                                    @Valid @RequestBody UpdateCommentDto updateCommentDto) {
        log.info("PATCH /comment/{commentId}/user/{userId}: request to update a comment id={} by user id={}",
                commentId, userId);
        return commentService.updateComment(commentId, userId, updateCommentDto);
    }

    @DeleteMapping("/{commentId}/user/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @NotNull @Positive Long commentId,
                              @PathVariable @NotNull @Positive Long userId) {
        log.info("DELETE /comment/{commentId}/user/{userId}: request to delete a comment id={} by user id={}",
                commentId, userId);
        commentService.deleteComment(commentId, userId);
    }

    @GetMapping("/user/{userId}")
    public List<CommentDto> getAllCommentsUser(@PathVariable @NotNull @Positive Long userId,
                                               @RequestParam(defaultValue = "0", required = false) Integer from,
                                               @RequestParam(defaultValue = "10", required = false) Integer size) {
        log.info("GET /comment/user/{userId}: request to get all comments by user id={}, from={}, size={}",
                userId, from, size);
        return commentService.getAllCommentsUser(userId, from, size);
    }

    @GetMapping("/{commentId}/user/{userId}")
    public CommentDto getComment(@PathVariable @NotNull @Positive Long commentId,
                                 @PathVariable @NotNull @Positive Long userId) {
        log.info("GET /comment/{commentId}/user/{userId}: request to get one comment id={} by user id={}",
                commentId, userId);
        return commentService.getCommentUser(commentId, userId);
    }
}
