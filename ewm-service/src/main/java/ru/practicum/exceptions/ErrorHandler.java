package ru.practicum.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.practicum.exceptions.model.ApiError;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.Constant.*;

@RestControllerAdvice
@Slf4j
public class ErrorHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiError> handleNotFoundException(final NotFoundException e) {
        ApiError apiError = new ApiError()
                .setStatus(HttpStatus.NOT_FOUND)
                .setReason(REASON_NOT_FOUND)
                .setMessage(e.getMessage())
                .setErrors(e.getErrorMessages());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiError> handleConflictException(final ConflictException e) {
        ApiError apiError = new ApiError()
                .setStatus(HttpStatus.CONFLICT)
                .setReason(REASON_CONFLICT)
                .setMessage(e.getMessage())
                .setErrors(e.getErrorMessages());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiError> handleDataIntegrityViolationException(final DataIntegrityViolationException e) {
        String errorMessage = e.getMostSpecificCause().getMessage();
        log.warn("The category has not been created due to data integrity violation: {}", errorMessage);
        ApiError apiError = new ApiError()
                .setStatus(HttpStatus.CONFLICT)
                .setReason(REASON_CONFLICT)
                .setMessage(e.getMessage())
                .setErrors(Collections.singletonList(errorMessage));
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

     @Override
     public ResponseEntity<Object> handleMethodArgumentNotValid(@NotNull MethodArgumentNotValidException ex,
                                                                @NotNull HttpHeaders headers,
                                                                @NotNull HttpStatus status, WebRequest request) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.toList());

        ApiError apiError = new ApiError()
                .setStatus(HttpStatus.BAD_REQUEST)
                .setReason(REASON_BAD_REQUEST)
                .setMessage(ex.getMessage())
                .setErrors(errors);

        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiError> handleValidationException(final ValidationException e) {
        ApiError apiError = new ApiError()
                .setStatus(HttpStatus.BAD_REQUEST)
                .setReason(REASON_BAD_REQUEST)
                .setMessage(e.getMessage())
                .setErrors(e.getErrorMessages());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiError> handleConstraintViolationException(ConstraintViolationException e) {
        List<String> errors = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessageTemplate)
                .collect(Collectors.toList());

        ApiError apiError = new ApiError()
                .setStatus(HttpStatus.BAD_REQUEST)
                .setReason(REASON_BAD_REQUEST)
                .setMessage(e.getMessage())
                .setErrors(errors);

        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }
}
