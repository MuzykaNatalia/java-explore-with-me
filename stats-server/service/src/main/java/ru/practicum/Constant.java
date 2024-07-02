package ru.practicum;

import java.time.format.DateTimeFormatter;

public class Constant {
    public static final String PATTERN_DATE = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final String REASON_BAD_REQUEST = "Incorrectly made request.";
}
