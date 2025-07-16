/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package com.nebula.base.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @author : wh
 * @date : 2023/5/12 10:30
 * @description:
 */
public class TimeUtil {
    
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    
    public static final String HH_MM_SS = "HH:mm:ss";
    
    public static final String YYYYMMDD = "yyyyMMdd";
    
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);
    
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(YYYY_MM_DD);
    
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(HH_MM_SS);
    
    public static final DateTimeFormatter COMPACT_DATE_FORMATTER = DateTimeFormatter.ofPattern(YYYYMMDD);
    
    public static final DateTimeFormatter COMPACT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(YYYYMMDDHHMMSS);
    
    private TimeUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    /**
     * Converts Unix time (milliseconds since epoch) to {@link LocalDateTime} using a specific ZoneOffset.
     *
     * @param epochMilli The Unix time in milliseconds.
     * @param zoneOffset The zone offset (e.g., hours difference from UTC). Cannot be null.
     * @return The corresponding {@link LocalDateTime}.
     * @throws NullPointerException if zoneOffset is null.
     */
    public static LocalDateTime toLocalDateTime(long epochMilli, ZoneOffset zoneOffset) {
        if (zoneOffset == null) {
            throw new NullPointerException("ZoneOffset cannot be null.");
        }
        Instant instant = Instant.ofEpochMilli(epochMilli);
        return LocalDateTime.ofInstant(instant, zoneOffset);
    }
    
    /**
     * Converts Unix time (milliseconds since epoch) to {@link LocalDateTime} using a specific ZoneId.
     * This is generally preferred over using ZoneOffset as ZoneId handles daylight saving rules.
     *
     * @param epochMilli The Unix time in milliseconds.
     * @param zoneId The time zone ID (e.g., "Europe/Paris", "America/New_York"). Cannot be null.
     * @return The corresponding {@link LocalDateTime}.
     * @throws NullPointerException if zoneId is null.
     */
    public static LocalDateTime toLocalDateTime(long epochMilli, ZoneId zoneId) {
        if (zoneId == null) {
            throw new NullPointerException("ZoneId cannot be null.");
        }
        Instant instant = Instant.ofEpochMilli(epochMilli);
        return LocalDateTime.ofInstant(instant, zoneId);
    }
    
    /**
     * Converts Unix time (milliseconds since epoch) to {@link LocalDateTime} using the system default time zone.
     * Note: Usage of system default time zone can lead to inconsistencies if the application
     * is run in different environments with different default time zones.
     * Consider using {@link #toLocalDateTime(long, ZoneId)} for better control.
     *
     * @param epochMilli The Unix time in milliseconds.
     * @return The corresponding {@link LocalDateTime} in the system default time zone.
     */
    public static LocalDateTime toLocalDateTime(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
    }
    
    /**
     * Converts Unix time (milliseconds since epoch, as a {@link Long} object) to {@link LocalDateTime}
     * using the system default time zone.
     * Returns null if the input epochMilli is null.
     * Note: Usage of system default time zone can lead to inconsistencies.
     *
     * @param epochMilli The Unix time in milliseconds, or null.
     * @return The corresponding {@link LocalDateTime} in the system default time zone, or null if input is null.
     */
    public static LocalDateTime toLocalDateTime(Long epochMilli) {
        if (epochMilli == null /* || DataUtils.isEmpty(epochMilli) */) { // Assuming DataUtils.isEmpty handles null
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
    }
    
    /**
     * Converts Unix time (milliseconds since epoch, as a {@link Long} object) to {@link LocalDateTime}
     * using a specific ZoneId.
     * Returns null if the input epochMilli is null.
     *
     * @param epochMilli The Unix time in milliseconds, or null.
     * @param zoneId The time zone ID. Cannot be null if epochMilli is not null.
     * @return The corresponding {@link LocalDateTime}, or null if epochMilli is null.
     * @throws NullPointerException if epochMilli is not null and zoneId is null.
     */
    public static LocalDateTime toLocalDateTime(Long epochMilli, ZoneId zoneId) {
        if (epochMilli == null) {
            return null;
        }
        if (zoneId == null) {
            throw new NullPointerException("ZoneId cannot be null when epochMilli is provided.");
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), zoneId);
    }
    
    /**
     * Converts a {@link LocalDateTime} to Unix time (milliseconds since epoch) using the system default time zone.
     * Note: System default time zone can vary. Consider {@link #toEpochMilli(LocalDateTime, ZoneId)}.
     *
     * @param localDateTime The {@link LocalDateTime} to convert. Cannot be null.
     * @return The Unix time in milliseconds.
     * @throws NullPointerException if localDateTime is null.
     */
    public static long toEpochMilli(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            throw new NullPointerException("LocalDateTime cannot be null.");
        }
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    
    /**
     * Converts a {@link LocalDateTime} to Unix time (milliseconds since epoch) using a specific {@link ZoneId}.
     *
     * @param localDateTime The {@link LocalDateTime} to convert. Cannot be null.
     * @param zoneId The {@link ZoneId} to use for conversion. Cannot be null.
     * @return The Unix time in milliseconds.
     * @throws NullPointerException if localDateTime or zoneId is null.
     */
    public static long toEpochMilli(LocalDateTime localDateTime, ZoneId zoneId) {
        if (localDateTime == null) {
            throw new NullPointerException("LocalDateTime cannot be null.");
        }
        if (zoneId == null) {
            throw new NullPointerException("ZoneId cannot be null.");
        }
        return localDateTime.atZone(zoneId).toInstant().toEpochMilli();
    }
    
    /**
     * Converts a {@link LocalDateTime} to Unix time (milliseconds since epoch) using a specific {@link ZoneOffset}.
     *
     * @param localDateTime The {@link LocalDateTime} to convert. Cannot be null.
     * @param zoneOffset The {@link ZoneOffset} to use for conversion. Cannot be null.
     * @return The Unix time in milliseconds.
     * @throws NullPointerException if localDateTime or zoneOffset is null.
     */
    public static long toEpochMilli(LocalDateTime localDateTime, ZoneOffset zoneOffset) {
        if (localDateTime == null) {
            throw new NullPointerException("LocalDateTime cannot be null.");
        }
        if (zoneOffset == null) {
            throw new NullPointerException("ZoneOffset cannot be null.");
        }
        return localDateTime.toInstant(zoneOffset).toEpochMilli();
    }
    
    /**
     * Gets the current {@link LocalDateTime} in the system default time zone.
     *
     * @return The current {@link LocalDateTime}.
     */
    public static LocalDateTime getCurrentLocalDateTime() {
        return LocalDateTime.now(ZoneId.systemDefault());
    }
    
    /**
     * Gets the current {@link LocalDateTime} in the specified {@link ZoneId}.
     *
     * @param zoneId The time zone ID. Cannot be null.
     * @return The current {@link LocalDateTime} in the specified zone.
     * @throws NullPointerException if zoneId is null.
     */
    public static LocalDateTime getCurrentLocalDateTime(ZoneId zoneId) {
        if (zoneId == null) {
            throw new NullPointerException("ZoneId cannot be null.");
        }
        return LocalDateTime.now(zoneId);
    }
    
    /**
     * Gets the current {@link LocalDate} in the system default time zone.
     *
     * @return The current {@link LocalDate}.
     */
    public static LocalDate getCurrentLocalDate() {
        return LocalDate.now(ZoneId.systemDefault());
    }
    
    /**
     * Gets the current {@link LocalDate} in the specified {@link ZoneId}.
     *
     * @param zoneId The time zone ID. Cannot be null.
     * @return The current {@link LocalDate} in the specified zone.
     * @throws NullPointerException if zoneId is null.
     */
    public static LocalDate getCurrentLocalDate(ZoneId zoneId) {
        if (zoneId == null) {
            throw new NullPointerException("ZoneId cannot be null.");
        }
        return LocalDate.now(zoneId);
    }
    
    /**
     * Gets the current time as epoch milliseconds (milliseconds since 1970-01-01T00:00:00Z).
     *
     * @return Current time in epoch milliseconds.
     */
    public static long getCurrentEpochMilli() {
        return Instant.now().toEpochMilli();
    }
    
    // --- Formatting Methods ---
    
    /**
     * Formats a {@link LocalDateTime} to a string using the default {@link #DATETIME_FORMATTER} (yyyy-MM-dd HH:mm:ss).
     *
     * @param localDateTime The {@link LocalDateTime} to format. Can be null.
     * @return The formatted date-time string, or null if localDateTime is null.
     */
    public static String formatLocalDateTime(LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.format(DATETIME_FORMATTER);
    }
    
    /**
     * Formats a {@link LocalDateTime} to a string using the specified {@link DateTimeFormatter}.
     *
     * @param localDateTime The {@link LocalDateTime} to format. Can be null.
     * @param formatter The {@link DateTimeFormatter} to use. Cannot be null if localDateTime is not null.
     * @return The formatted date-time string, or null if localDateTime is null.
     * @throws NullPointerException if localDateTime is not null and formatter is null.
     */
    public static String formatLocalDateTime(LocalDateTime localDateTime, DateTimeFormatter formatter) {
        if (localDateTime == null) {
            return null;
        }
        if (formatter == null) {
            throw new NullPointerException("DateTimeFormatter cannot be null when localDateTime is provided.");
        }
        return localDateTime.format(formatter);
    }
    
    /**
     * Formats a {@link LocalDateTime} to a string using the specified pattern.
     *
     * @param localDateTime The {@link LocalDateTime} to format. Can be null.
     * @param pattern The date-time pattern string. Cannot be null or empty if localDateTime is not null.
     * @return The formatted date-time string, or null if localDateTime is null.
     * @throws NullPointerException if localDateTime is not null and pattern is null.
     * @throws IllegalArgumentException if the pattern is invalid.
     */
    public static String formatLocalDateTime(LocalDateTime localDateTime, String pattern) {
        if (localDateTime == null) {
            return null;
        }
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("Pattern cannot be null or empty when localDateTime is provided.");
        }
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }
    
    /**
     * Formats a {@link LocalDate} to a string using the default {@link #DATE_FORMATTER} (yyyy-MM-dd).
     *
     * @param localDate The {@link LocalDate} to format. Can be null.
     * @return The formatted date string, or null if localDate is null.
     */
    public static String formatLocalDate(LocalDate localDate) {
        return localDate == null ? null : localDate.format(DATE_FORMATTER);
    }
    
    /**
     * Formats the current {@link LocalDateTime} (system default zone) to a string
     * using the default {@link #DATETIME_FORMATTER}.
     *
     * @return The formatted current date-time string.
     */
    public static String formatCurrentDateTime() {
        return getCurrentLocalDateTime().format(DATETIME_FORMATTER);
    }
    
    /**
     * Formats the current {@link LocalDateTime} (system default zone) to a string
     * using the specified pattern.
     *
     * @param pattern The date-time pattern string. Cannot be null or empty.
     * @return The formatted current date-time string.
     * @throws IllegalArgumentException if the pattern is invalid or null/empty.
     */
    public static String formatCurrentDateTime(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("Pattern cannot be null or empty.");
        }
        return getCurrentLocalDateTime().format(DateTimeFormatter.ofPattern(pattern));
    }
    
    // --- Start/End of Day Methods ---
    
    /**
     * Gets the start of the day (00:00:00.000) for the given {@link LocalDateTime}.
     *
     * @param dateTime The {@link LocalDateTime}. Cannot be null.
     * @return A new {@link LocalDateTime} representing the start of the day.
     * @throws NullPointerException if dateTime is null.
     */
    public static LocalDateTime getStartOfDay(LocalDateTime dateTime) {
        if (dateTime == null) {
            throw new NullPointerException("Input LocalDateTime cannot be null.");
        }
        return dateTime.toLocalDate().atStartOfDay(); // or dateTime.with(LocalTime.MIN);
    }
    
    /**
     * Gets the start of the day (00:00:00.000) for the given {@link LocalDate}.
     *
     * @param date The {@link LocalDate}. Cannot be null.
     * @return A new {@link LocalDateTime} representing the start of the day.
     * @throws NullPointerException if date is null.
     */
    public static LocalDateTime getStartOfDay(LocalDate date) {
        if (date == null) {
            throw new NullPointerException("Input LocalDate cannot be null.");
        }
        return date.atStartOfDay();
    }
    
    /**
     * Gets the end of the day (23:59:59.999999999) for the given {@link LocalDateTime}.
     *
     * @param dateTime The {@link LocalDateTime}. Cannot be null.
     * @return A new {@link LocalDateTime} representing the end of the day.
     * @throws NullPointerException if dateTime is null.
     */
    public static LocalDateTime getEndOfDay(LocalDateTime dateTime) {
        if (dateTime == null) {
            throw new NullPointerException("Input LocalDateTime cannot be null.");
        }
        return dateTime.with(LocalTime.MAX);
    }
    
    /**
     * Gets the end of the day (23:59:59.999999999) for the given {@link LocalDate}.
     *
     * @param date The {@link LocalDate}. Cannot be null.
     * @return A new {@link LocalDateTime} representing the end of the day.
     * @throws NullPointerException if date is null.
     */
    public static LocalDateTime getEndOfDay(LocalDate date) {
        if (date == null) {
            throw new NullPointerException("Input LocalDate cannot be null.");
        }
        return date.atTime(LocalTime.MAX);
    }
    
    /**
     * Gets the start of today (00:00:00.000) in the system default time zone.
     *
     * @return A {@link LocalDateTime} representing the start of today.
     */
    public static LocalDateTime getStartOfToday() {
        return getCurrentLocalDate().atStartOfDay();
    }
    
    /**
     * Gets the end of today (23:59:59.999999999) in the system default time zone.
     *
     * @return A {@link LocalDateTime} representing the end of today.
     */
    public static LocalDateTime getEndOfToday() {
        return getCurrentLocalDate().atTime(LocalTime.MAX);
    }
    
}
