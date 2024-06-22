package ru.practicum.exceptions;

import java.util.List;

public class ConflictException extends RuntimeException {
    private final List<String> errorMessages;
    public ConflictException(final String message, final List<String> errorMessages) {
        super(message);
        this.errorMessages = errorMessages;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
