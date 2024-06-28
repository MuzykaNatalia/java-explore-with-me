package ru.practicum.controller.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.sort.SortEvent;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.Constant.PATTERN_DATE;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PublicEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEvents(@RequestParam(required = false) String text,
                                         @RequestParam(required = false) List<Long> categories,
                                         @RequestParam(required = false) Boolean paid,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = PATTERN_DATE)
                                             LocalDateTime rangeStart,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = PATTERN_DATE)
                                             LocalDateTime rangeEnd,
                                         @RequestParam(defaultValue = "false", required = false) Boolean onlyAvailable,
                                         @RequestParam(defaultValue = "VIEWS") String sort,
                                         @RequestParam(defaultValue = "0", required = false) Integer from,
                                         @RequestParam(defaultValue = "10", required = false) Integer size,
                                         HttpServletRequest request) {
        log.info("GET /events: request get events by text={}, by categories ids={}, by paid={}, by range date time " +
                "start={} and end={}, by only available={}, by sort={}, from={}, size={}", text, categories, paid,
                rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        return eventService.getEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable,
                SortEvent.valueOf(sort), from, size, request);
    }

    @GetMapping("/{id}")
    public EventFullDto getOneEvent(@PathVariable @NotNull @Min(1L) Long id, HttpServletRequest request) {
        log.info("GET /events/{id}: request get one event id={}", id);
        return eventService.getOneEvent(id, request);
    }
}
