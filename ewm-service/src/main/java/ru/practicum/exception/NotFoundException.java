package ru.practicum.exception;

import java.util.List;

public class NotFoundException extends RuntimeException {
    private final List<String> errorMessages;

    public NotFoundException(final String message, final List<String> errorMessages) {
        super(message);
        this.errorMessages = errorMessages;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
