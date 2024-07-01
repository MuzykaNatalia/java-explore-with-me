package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.practicum.exception.model.ApiError;

import static ru.practicum.Constant.REASON_BAD_REQUEST;

@RestControllerAdvice
@Slf4j
public class ErrorHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiError> handleValidationException(final EndTimeBeforeStartTimeException e) {
        ApiError apiError = new ApiError()
                .setStatus(HttpStatus.BAD_REQUEST)
                .setReason(REASON_BAD_REQUEST)
                .setMessage(e.getMessage())
                .setErrors(e.getErrorMessages());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }
}
