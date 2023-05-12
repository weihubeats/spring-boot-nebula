package com.nebula.base.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * @author : wh
 * @date : 2023/5/12 10:30
 * @description:
 */
public class TimeUtil {

    public static LocalDateTime toLocalDateTime(long unixTime, Integer zoneOffset) {
        Instant instant = Instant.ofEpochMilli(unixTime);
        return LocalDateTime.ofEpochSecond(instant.getEpochSecond(), instant.getNano(), ZoneOffset.ofHours(zoneOffset));
    }

    public static LocalDateTime toLocalDateTime(long unixTime) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(unixTime), ZoneId.systemDefault());
    }

    public static LocalDateTime toLocalDateTime(Long unixTime) {
        if (DataUtils.isEmpty(unixTime)) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(unixTime), ZoneId.systemDefault());
    }
}
