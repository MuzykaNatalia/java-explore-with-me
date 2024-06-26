package ru.practicum.exceptions;

import java.util.List;

public class EndTimeBeforeStartTimeException extends RuntimeException {
    private final List<String> errorMessages;

    public EndTimeBeforeStartTimeException(final String message, final List<String> errorMessages) {
        super(message);
        this.errorMessages = errorMessages;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
