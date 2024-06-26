package ru.practicum.exceptions.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.Constant.FORMATTER;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ApiError {
    public HttpStatus status;
    public String reason;
    public String message;
    public List<String> errors;
    public String timestamp = FORMATTER.format(LocalDateTime.now());
}
