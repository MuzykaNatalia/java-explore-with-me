package ru.practicum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.core.io.ClassPathResource;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class EndpointHitDtoTest {
    private JacksonTester<EndpointHitDto> json;
    private Validator validator;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private LocalDateTime timestamp = LocalDateTime.of(2024,1,1, 0, 0, 0);

    @BeforeEach
    public void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        JacksonTester.initFields(this, mapper);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @DisplayName("Тест на корректную сериализацию объекта EndpointHitDto")
    @Test
    @SneakyThrows
    public void shouldSerialize() {

        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .app("ewm3")
                .uri("/events/3")
                .ip("121.0.0.6")
                .timestamp(timestamp)
                .build();

        JsonContent<EndpointHitDto> endpointHitDtoJson = this.json.write(endpointHitDto);

        assertThat(endpointHitDtoJson).hasJsonPathValue("$.app");
        assertThat(endpointHitDtoJson).extractingJsonPathStringValue("$.app").isEqualTo("ewm3");

        assertThat(endpointHitDtoJson).hasJsonPathValue("$.uri");
        assertThat(endpointHitDtoJson).extractingJsonPathStringValue("$.uri").isEqualTo("/events/3");

        assertThat(endpointHitDtoJson).hasJsonPathValue("$.ip");
        assertThat(endpointHitDtoJson).extractingJsonPathStringValue("$.ip").isEqualTo("121.0.0.6");

        assertThat(endpointHitDtoJson).hasJsonPathValue("$.timestamp");
        assertThat(endpointHitDtoJson).extractingJsonPathStringValue("$.timestamp")
                .isEqualTo(timestamp.format(FORMATTER));
    }

    @DisplayName("Тест на корректную десериализацию объекта EndpointHitDto")
    @Test
    @SneakyThrows
    public void shouldDeserialize() {
        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .app("ewm3")
                .uri("/events/3")
                .ip("121.0.0.6")
                .timestamp(timestamp)
                .build();

        var resource = new ClassPathResource("endpointHitDto.json");
        String content = Files.readString(resource.getFile().toPath());

        assertThat(this.json.parse(content)).isEqualTo(endpointHitDto);
    }

    @DisplayName("Проверка корректной валидации объекта EndpointHitDto при создании")
    @Test
    public void shouldValidation() {
        EndpointHitDto endpointHitDtoOne = EndpointHitDto.builder()
                .app("")
                .uri("/events/3")
                .ip("121.0.0.6")
                .timestamp(timestamp)
                .build();

        EndpointHitDto endpointHitDtoTwo = EndpointHitDto.builder()
                .app("ewm3")
                .uri("")
                .ip("121.0.0.6")
                .timestamp(timestamp)
                .build();

        EndpointHitDto endpointHitDtoThree = EndpointHitDto.builder()
                .app("ewm3")
                .uri("/events/3")
                .ip("")
                .timestamp(timestamp)
                .build();

        EndpointHitDto endpointHitDtoFour = EndpointHitDto.builder()
                .app("ewm3")
                .uri("/events/3")
                .ip("121.0.0.6")
                .timestamp(null)
                .build();

        Set<ConstraintViolation<EndpointHitDto>> violations = validator.validate(endpointHitDtoOne);
        Set<ConstraintViolation<EndpointHitDto>> violationsTwo = validator.validate(endpointHitDtoTwo);
        Set<ConstraintViolation<EndpointHitDto>> violationsThree = validator.validate(endpointHitDtoThree);
        Set<ConstraintViolation<EndpointHitDto>> violationsFour = validator.validate(endpointHitDtoFour);

        assertThat(violations).isNotEmpty();
        assertThat(violationsTwo).isNotEmpty();
        assertThat(violationsThree).isNotEmpty();
        assertThat(violationsFour).isNotEmpty();
    }
}