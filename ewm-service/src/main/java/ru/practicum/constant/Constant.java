package ru.practicum.constant;

import java.time.format.DateTimeFormatter;

public class Constant {
    public static final String PATTERN_DATE = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final String REASON_NOT_FOUND = "The required object was not found.";
    public static final String REASON_CONFLICT = "Integrity constraint has been violated.";
    public static final String REASON_BAD_REQUEST = "Incorrectly made request.";
    public static final String NAME_SERVICE_APP = "ewm-main-service";
}
