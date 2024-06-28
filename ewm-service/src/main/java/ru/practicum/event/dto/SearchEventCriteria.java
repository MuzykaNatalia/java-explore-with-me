package ru.practicum.event.dto;

import lombok.*;
import ru.practicum.event.sort.SortEvent;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class SearchEventCriteria {
    private String text;
    private List<Long> categories;
    private Boolean paid;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Boolean onlyAvailable;
    private SortEvent sort;
    private Integer from;
    private Integer size;
}
