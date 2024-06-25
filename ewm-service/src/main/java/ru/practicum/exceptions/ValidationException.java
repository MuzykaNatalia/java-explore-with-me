package ru.practicum.exceptions;

import java.util.List;

public class ValidationException extends RuntimeException {
    private final List<String> errorMessages;

    public ValidationException(final String message, final List<String> errorMessages) {
        super(message);
        this.errorMessages = errorMessages;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
