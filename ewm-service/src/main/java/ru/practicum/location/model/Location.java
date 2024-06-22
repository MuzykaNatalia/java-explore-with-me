package ru.practicum.location.model;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class Location {
    private Long id;
    @NotNull
    @Positive
    private Float lat;
    @NotNull
    @Positive
    private Float lon;
}
