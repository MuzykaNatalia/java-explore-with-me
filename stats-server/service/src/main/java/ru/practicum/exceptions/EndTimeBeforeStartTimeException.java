package ru.practicum.exceptions;

public class EndTimeBeforeStartTimeException extends RuntimeException {
    public EndTimeBeforeStartTimeException(final String message) {
        super(message);
    }
}
