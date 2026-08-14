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
import java.util.Objects;

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
        Objects.requireNonNull(zoneOffset, "ZoneOffset cannot be null.");
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), zoneOffset);
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
        Objects.requireNonNull(zoneId, "ZoneId cannot be null.");
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), zoneId);
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
        if (Objects.isNull(epochMilli)) {
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
        if (Objects.isNull(epochMilli)) {
            return null;
        }
        Objects.requireNonNull(zoneId, "ZoneId cannot be null when epochMilli is provided.");
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
        Objects.requireNonNull(localDateTime, "localDateTime");
        return localDateTime.atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli();
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
        Objects.requireNonNull(localDateTime, "localDateTime");
        Objects.requireNonNull(zoneId, "zoneId");
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
        Objects.requireNonNull(localDateTime, "localDateTime");
        Objects.requireNonNull(zoneOffset, "zoneOffset");
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
        return LocalDateTime.now(Objects.requireNonNull(zoneId, "zoneId"));
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
        return LocalDate.now(Objects.requireNonNull(zoneId, "zoneId"));
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
        return Objects.isNull(localDateTime) ? null : localDateTime.format(DATETIME_FORMATTER);
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
        if (Objects.isNull(localDateTime)) {
            return null;
        }
        return localDateTime.format(Objects.requireNonNull(formatter, "formatter"));
    }

    /**
     * Formats a {@link LocalDateTime} to a string using the specified pattern.
     *
     * @param localDateTime The {@link LocalDateTime} to format. Can be null.
     * @param pattern The date-time pattern string. Cannot be null or empty if localDateTime is not null.
     * @return The formatted date-time string, or null if localDateTime is null.
     * @throws IllegalArgumentException if the pattern is null, empty or invalid.
     */
    public static String formatLocalDateTime(LocalDateTime localDateTime, String pattern) {
        if (Objects.isNull(localDateTime)) {
            return null;
        }
        if (Objects.isNull(pattern) || pattern.isEmpty()) {
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
        return Objects.isNull(localDate) ? null : localDate.format(DATE_FORMATTER);
    }

    /**
     * Formats a {@link LocalDate} to a string using the specified {@link DateTimeFormatter}.
     *
     * @param localDate The {@link LocalDate} to format. Can be null.
     * @param formatter The {@link DateTimeFormatter} to use. Cannot be null if localDate is not null.
     * @return The formatted date string, or null if localDate is null.
     * @throws NullPointerException if localDate is not null and formatter is null.
     */
    public static String formatLocalDate(LocalDate localDate, DateTimeFormatter formatter) {
        if (Objects.isNull(localDate)) {
            return null;
        }
        return localDate.format(Objects.requireNonNull(formatter, "formatter"));
    }

    /**
     * Formats a {@link LocalDate} to a string using the specified pattern.
     *
     * @param localDate The {@link LocalDate} to format. Can be null.
     * @param pattern The date pattern string. Cannot be null or empty if localDate is not null.
     * @return The formatted date string, or null if localDate is null.
     * @throws IllegalArgumentException if the pattern is null, empty or invalid.
     */
    public static String formatLocalDate(LocalDate localDate, String pattern) {
        if (Objects.isNull(localDate)) {
            return null;
        }
        if (Objects.isNull(pattern) || pattern.isEmpty()) {
            throw new IllegalArgumentException("Pattern cannot be null or empty when localDate is provided.");
        }
        return localDate.format(DateTimeFormatter.ofPattern(pattern));
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
        if (Objects.isNull(pattern) || pattern.isEmpty()) {
            throw new IllegalArgumentException("Pattern cannot be null or empty.");
        }
        return getCurrentLocalDateTime().format(DateTimeFormatter.ofPattern(pattern));
    }

    // --- Parsing Methods ---

    /**
     * Parses a string into a {@link LocalDateTime} using the default {@link #DATETIME_FORMATTER} (yyyy-MM-dd HH:mm:ss).
     *
     * @param text The date-time string. Can be null or empty.
     * @return The parsed {@link LocalDateTime}, or null if text is null or empty.
     * @throws java.time.format.DateTimeParseException if the text cannot be parsed.
     */
    public static LocalDateTime parseLocalDateTime(String text) {
        return DataUtils.isEmpty(text) ? null : LocalDateTime.parse(text, DATETIME_FORMATTER);
    }

    /**
     * Parses a string into a {@link LocalDateTime} using the specified {@link DateTimeFormatter}.
     *
     * @param text The date-time string. Can be null or empty.
     * @param formatter The {@link DateTimeFormatter} to use. Cannot be null if text is not empty.
     * @return The parsed {@link LocalDateTime}, or null if text is null or empty.
     * @throws NullPointerException if text is not empty and formatter is null.
     * @throws java.time.format.DateTimeParseException if the text cannot be parsed.
     */
    public static LocalDateTime parseLocalDateTime(String text, DateTimeFormatter formatter) {
        if (DataUtils.isEmpty(text)) {
            return null;
        }
        return LocalDateTime.parse(text, Objects.requireNonNull(formatter, "formatter"));
    }

    /**
     * Parses a string into a {@link LocalDateTime} using the specified pattern.
     *
     * @param text The date-time string. Can be null or empty.
     * @param pattern The date-time pattern string. Cannot be null or empty if text is not empty.
     * @return The parsed {@link LocalDateTime}, or null if text is null or empty.
     * @throws IllegalArgumentException if the pattern is null, empty or invalid.
     * @throws java.time.format.DateTimeParseException if the text cannot be parsed.
     */
    public static LocalDateTime parseLocalDateTime(String text, String pattern) {
        if (DataUtils.isEmpty(text)) {
            return null;
        }
        if (Objects.isNull(pattern) || pattern.isEmpty()) {
            throw new IllegalArgumentException("Pattern cannot be null or empty when text is provided.");
        }
        return LocalDateTime.parse(text, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Parses a string into a {@link LocalDate} using the default {@link #DATE_FORMATTER} (yyyy-MM-dd).
     *
     * @param text The date string. Can be null or empty.
     * @return The parsed {@link LocalDate}, or null if text is null or empty.
     * @throws java.time.format.DateTimeParseException if the text cannot be parsed.
     */
    public static LocalDate parseLocalDate(String text) {
        return DataUtils.isEmpty(text) ? null : LocalDate.parse(text, DATE_FORMATTER);
    }

    /**
     * Parses a string into a {@link LocalDate} using the specified {@link DateTimeFormatter}.
     *
     * @param text The date string. Can be null or empty.
     * @param formatter The {@link DateTimeFormatter} to use. Cannot be null if text is not empty.
     * @return The parsed {@link LocalDate}, or null if text is null or empty.
     * @throws NullPointerException if text is not empty and formatter is null.
     * @throws java.time.format.DateTimeParseException if the text cannot be parsed.
     */
    public static LocalDate parseLocalDate(String text, DateTimeFormatter formatter) {
        if (DataUtils.isEmpty(text)) {
            return null;
        }
        return LocalDate.parse(text, Objects.requireNonNull(formatter, "formatter"));
    }

    /**
     * Parses a string into a {@link LocalDate} using the specified pattern.
     *
     * @param text The date string. Can be null or empty.
     * @param pattern The date pattern string. Cannot be null or empty if text is not empty.
     * @return The parsed {@link LocalDate}, or null if text is null or empty.
     * @throws IllegalArgumentException if the pattern is null, empty or invalid.
     * @throws java.time.format.DateTimeParseException if the text cannot be parsed.
     */
    public static LocalDate parseLocalDate(String text, String pattern) {
        if (DataUtils.isEmpty(text)) {
            return null;
        }
        if (Objects.isNull(pattern) || pattern.isEmpty()) {
            throw new IllegalArgumentException("Pattern cannot be null or empty when text is provided.");
        }
        return LocalDate.parse(text, DateTimeFormatter.ofPattern(pattern));
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
        return Objects.requireNonNull(dateTime, "dateTime").toLocalDate().atStartOfDay();
    }

    /**
     * Gets the start of the day (00:00:00.000) for the given {@link LocalDate}.
     *
     * @param date The {@link LocalDate}. Cannot be null.
     * @return A new {@link LocalDateTime} representing the start of the day.
     * @throws NullPointerException if date is null.
     */
    public static LocalDateTime getStartOfDay(LocalDate date) {
        return Objects.requireNonNull(date, "date").atStartOfDay();
    }

    /**
     * Gets the end of the day (23:59:59.999999999) for the given {@link LocalDateTime}.
     *
     * @param dateTime The {@link LocalDateTime}. Cannot be null.
     * @return A new {@link LocalDateTime} representing the end of the day.
     * @throws NullPointerException if dateTime is null.
     */
    public static LocalDateTime getEndOfDay(LocalDateTime dateTime) {
        return Objects.requireNonNull(dateTime, "dateTime").with(LocalTime.MAX);
    }

    /**
     * Gets the end of the day (23:59:59.999999999) for the given {@link LocalDate}.
     *
     * @param date The {@link LocalDate}. Cannot be null.
     * @return A new {@link LocalDateTime} representing the end of the day.
     * @throws NullPointerException if date is null.
     */
    public static LocalDateTime getEndOfDay(LocalDate date) {
        return Objects.requireNonNull(date, "date").atTime(LocalTime.MAX);
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
