package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.service.CommentService;
import ru.practicum.comment.dto.CommentDto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PublicCommentController {
    private final CommentService commentService;

    @GetMapping("/event/{eventId}")
    public List<CommentDto> getAllCommentsByEvent(@PathVariable @NotNull @Positive Long eventId,
                                                  @RequestParam(defaultValue = "0", required = false)
                                                      Integer from,
                                                  @RequestParam(defaultValue = "10", required = false)
                                                      Integer size) {
        log.info("GET /comment/event/{eventId}: request to receive all comments on the event id={}, from={}, size={}",
                eventId, from, size);
        return commentService.getAllCommentsByEvent(eventId, from, size);
    }
}
