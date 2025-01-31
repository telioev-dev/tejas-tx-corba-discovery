package com.teliolabs.corba.utils;

import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Log4j2
public final class DateTimeUtils {

    private DateTimeUtils() {
    }

    public static String getDeltaTimestamp(int minusDays) {
        log.info("minusDays: {}", minusDays);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss'.0'");
        String timestamp = LocalDateTime.now().minusDays(minusDays).format(formatter);
        log.info("Using Delta Timestamp: {}", timestamp);
        return timestamp;
    }
}
