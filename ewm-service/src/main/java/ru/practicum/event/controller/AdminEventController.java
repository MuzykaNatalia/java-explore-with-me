package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.service.EventService;
import ru.practicum.event.state.EventState;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.constant.Constant.PATTERN_DATE;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getEventsForAdmin(@RequestParam(required = false) List<Long> users,
                                                @RequestParam(required = false) List<EventState> states,
                                                @RequestParam(required = false) List<Long> categories,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = PATTERN_DATE)
                                                    LocalDateTime rangeStart,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = PATTERN_DATE)
                                                    LocalDateTime rangeEnd,
                                                @RequestParam(defaultValue = "0", required = false) Integer from,
                                                @RequestParam(defaultValue = "10", required = false) Integer size) {
        log.info("GET /admin/events: request events by users ids={}, by states={}, by categories ids={}, " +
                        "range date time by start={} and end={}, from={}, size={}",
                users, states, categories, rangeStart, rangeEnd, from, size);
        return eventService.getEventsForAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByAdmin(@Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest,
                                           @PathVariable @NotNull @Min(1L) Long eventId) {
        log.info("PATCH /admin/events/{eventId}: request update event={} by id={}", updateEventAdminRequest, eventId);
        return eventService.updateEventByAdmin(updateEventAdminRequest, eventId);
    }
}
