package ru.practicum.location.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "location")
@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long id;
    private Float lat;
    private Float lon;
}
