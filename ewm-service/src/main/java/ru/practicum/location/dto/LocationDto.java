package ru.practicum.location.dto;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class LocationDto {
    @NotNull
    private Float lat;
    @NotNull
    private Float lon;
}
