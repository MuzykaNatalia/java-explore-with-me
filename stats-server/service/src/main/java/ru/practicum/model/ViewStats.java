package ru.practicum.model;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
public class ViewStats {
    private String app;
    private String uri;
    private Long hits;
}
