package net.idonow.common.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

public abstract class TimestampUtils {

    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final DateTimeFormatter OFFSET_DATE_TIME = new DateTimeFormatterBuilder()
            .append(ISO_LOCAL_DATE_TIME)
            .appendOffset("+HH:MM", "+00:00")
            .toFormatter();

    public static String getTimestampZeroOffset() {
        return ZonedDateTime.now(UTC_ZONE).format(OFFSET_DATE_TIME);
    }
}
