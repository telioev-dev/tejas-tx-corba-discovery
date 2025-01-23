package com.teliolabs.corba.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {

    private DateTimeUtils() {}

    public static String getDeltaTimestamp(int minusDays) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss'.0'");
        String timestamp = LocalDateTime.now().minusDays(minusDays).format(formatter);
        return timestamp;
    }
}
