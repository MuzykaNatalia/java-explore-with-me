package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentFullDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.event.state.EventState.PUBLISHED;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Transactional
    @Override
    public CommentDto createComment(Long eventId, Long userId, NewCommentDto newCommentDto) {
        Event event = getEvent(eventId);
        User user = getUser(userId);
        getExceptionIfReplyNotEventInitiator(event, userId, newCommentDto);

        Comment parentComment = getParentComment(eventId, newCommentDto);
        Comment createComment = commentMapper.toCommentCreate(newCommentDto, event, user, parentComment);
        createComment = commentRepository.save(createComment);

        log.info("Comment has been created={}", createComment);
        return commentMapper.toCommentDto(createComment);
    }

    @Transactional
    @Override
    public CommentDto updateComment(Long commentId, Long userId, UpdateCommentDto updateCommentDto) {
        Comment oldComment = getCommentByIdAndAuthor(commentId, userId);
        Comment updateComment = commentMapper.toCommentUpdate(updateCommentDto, oldComment);
        updateComment = commentRepository.save(updateComment);

        log.info("Comment updated={}", updateComment);
        return commentMapper.toCommentDto(updateComment);
    }

    @Transactional
    @Override
    public void deleteComment(Long commentId, Long userId) {
        Comment oldComment = getCommentByIdAndAuthor(commentId, userId);
        commentRepository.delete(oldComment);
        log.info("Comment with id={} deleted", commentId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CommentDto> getAllCommentsUser(Long userId, Integer from, Integer size) {
        Pageable pageable = getPageable(from, size);
        List<Comment> allCommentsUser = commentRepository.findAllByAuthorId(userId, pageable)
                .orElse(new ArrayList<>());

        log.info("Received all comments author id={}, size={}", userId, allCommentsUser.size());
        return commentMapper.toCommentDtoList(allCommentsUser);
    }

    @Transactional(readOnly = true)
    @Override
    public CommentDto getCommentUser(Long commentId, Long userId) {
        Comment comment = getCommentByIdAndAuthor(commentId, userId);

        log.info("Received comment id={} author id={} : {}", commentId, userId, comment);
        return commentMapper.toCommentDto(comment);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CommentDto> getAllCommentsByEvent(Long eventId, Integer from, Integer size) {
        Pageable pageable = getPageable(from, size);

        List<Comment> allCommentByEvent = commentRepository
                .findAllByEventIdAndParentComment(eventId, null, pageable)
                .orElse(new ArrayList<>());

        List<Comment> commentsWithReplies = setReplies(allCommentByEvent);

        log.info("Received all comments event id={}, size={}", eventId, commentsWithReplies.size());
        return commentMapper.toCommentDtoList(commentsWithReplies);
    }

    @Transactional
    @Override
    public void deleteCommentByAdmin(Long commentId) {
        getComment(commentId);
        commentRepository.deleteById(commentId);
        log.info("Comment with id={} deleted", commentId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CommentFullDto> getAllCommentsUserForAdmin(Long userId, Integer from, Integer size) {
        Pageable pageable = getPageable(from, size);
        List<Comment> allCommentsUser = commentRepository.findAllByAuthorId(userId, pageable)
                .orElse(new ArrayList<>());

        log.info("Received all comments author id={} for admin", userId);
        return commentMapper.toCommentFullDtoList(allCommentsUser);
    }

    @Transactional(readOnly = true)
    @Override
    public CommentFullDto getCommentForAdmin(Long commentId) {
        Comment comment = getComment(commentId);
        log.info("Received comment id={} for admin : {}", commentId, comment);
        return commentMapper.toCommentFullDto(comment);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CommentFullDto> getAllCommentsByTextForAdmin(String text, Integer from, Integer size) {
        Pageable pageable = getPageable(from, size);
        List<Comment> allCommentsByText = commentRepository.findAllByText(text, pageable).orElse(new ArrayList<>());
        log.info("Received comments for admin by text={}, size={}", text, allCommentsByText.size());
        return commentMapper.toCommentFullDtoList(allCommentsByText);
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findByIdAndEventState(eventId, PUBLISHED).orElseThrow(() ->
                new NotFoundException("Event with id=" + eventId + " was not found",
                        Collections.singletonList("Event id does not exist")));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("User with id=" + userId + " was not found",
                        Collections.singletonList("User id does not exist")));
    }

    private void getExceptionIfReplyNotEventInitiator(Event event, Long userId, NewCommentDto newCommentDto) {
        if (newCommentDto.getParentComment() != null && !event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Only the event organizer can respond to comments",
                    Collections.singletonList("The user is not the event organizer"));
        }
    }

    private Comment getCommentByIdAndAuthor(Long commentId, Long userId) {
        return commentRepository.findByIdAndAuthorId(commentId, userId).orElseThrow(() ->
                new NotFoundException("Comment with id=" + commentId + " was not found",
                        Collections.singletonList("Comment id does not exist")));
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException("Comment with id=" + commentId + " was not found",
                        Collections.singletonList("Comment id does not exist")));
    }

    private Comment getParentComment(Long eventId, NewCommentDto newCommentDto) {
        return newCommentDto.getParentComment() == null ? null
                : commentRepository.findByIdAndEventId(newCommentDto.getParentComment(), eventId)
                .orElseThrow(() -> new NotFoundException("Parent comment by event id=" + eventId + " was not found",
                        Collections.singletonList("Comment is not found")));
    }

    private List<Comment> setReplies(List<Comment> comments) {
        List<Long> commentsIds = comments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());

        List<Comment> replies = commentRepository.findAllByParentCommentIdIn(commentsIds).orElse(new ArrayList<>());
        Map<Long, Comment> repliesMap = getCommentsByIdInMap(replies);

        return comments.stream()
                .map(comment -> {
                    if (repliesMap.containsKey(comment.getId())) {
                        Comment reply = repliesMap.get(comment.getId());
                        comment.setReply(reply);
                    }
                    return comment;
                }).collect(Collectors.toList());
    }

    private Map<Long, Comment> getCommentsByIdInMap(List<Comment> replies) {
        Map<Long, Comment> repliesMap = new HashMap<>();
        replies.forEach(comment -> repliesMap.put(comment.getParentComment().getId(), comment));
        return repliesMap;
    }

    private Pageable getPageable(Integer from, Integer size) {
        return PageRequest.of(from / size, size);
    }
}
