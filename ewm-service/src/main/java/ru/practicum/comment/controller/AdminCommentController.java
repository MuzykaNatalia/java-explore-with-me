package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.service.CommentService;
import ru.practicum.comment.dto.CommentFullDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/admin/comment")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminCommentController {
    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByAdmin(@PathVariable @NotNull @Positive Long commentId) {
        log.info("DELETE /admin/comment/{commentId}: request to delete comment id={} by administrator", commentId);
        commentService.deleteCommentByAdmin(commentId);
    }

    @GetMapping("/user/{userId}")
    public List<CommentFullDto> getAllCommentsUserForAdmin(@PathVariable @NotNull @Positive Long userId,
                                                           @RequestParam(defaultValue = "0", required = false)
                                                           Integer from,
                                                           @RequestParam(defaultValue = "10", required = false)
                                                           Integer size) {
        log.info("GET /admin/comment/user/{userId}: request to receive all comments from user id={} by administrator," +
                        " from={}, size={}", userId, from, size);
        return commentService.getAllCommentsUserForAdmin(userId, from, size);
    }

    @GetMapping("/{commentId}/user/{userId}")
    public CommentFullDto getCommentForAdmin(@PathVariable @NotNull @Positive Long commentId) {
        log.info("GET /admin/comment/{commentId}/user/{userId}: request to receive one comment id={} from the " +
                        "administrator", commentId);
        return commentService.getCommentForAdmin(commentId);
    }

    @GetMapping
    public List<CommentFullDto> getAllCommentsByTextForAdmin(@RequestParam @NotBlank String text,
                                                         @RequestParam(defaultValue = "0", required = false)
                                                             Integer from,
                                                         @RequestParam(defaultValue = "10", required = false)
                                                             Integer size) {
        log.info("GET /admin/comment: request to receive all comments by text={} by administrator, from={}, size={}",
                text, from, size);
        return commentService.getAllCommentsByTextForAdmin(text, from, size);
    }
}
