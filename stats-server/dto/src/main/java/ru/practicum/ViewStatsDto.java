package ru.practicum;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class ViewStatsDto {
    private String app;
    private String uri;
    private Long hits;
}
