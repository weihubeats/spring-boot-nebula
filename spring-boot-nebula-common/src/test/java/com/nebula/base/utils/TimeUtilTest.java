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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

/**
 * TimeUtil 格式化 / 解析行为校验。
 */
class TimeUtilTest {

    @Test
    void shouldParseLocalDateTimeWithDefaultFormatter() {
        LocalDateTime parsed = TimeUtil.parseLocalDateTime("2024-01-02 03:04:05");
        assertEquals(LocalDateTime.of(2024, 1, 2, 3, 4, 5), parsed);
    }

    @Test
    void shouldParseLocalDateTimeWithFormatter() {
        LocalDateTime parsed = TimeUtil.parseLocalDateTime(
                "20240102030405", TimeUtil.COMPACT_DATETIME_FORMATTER);
        assertEquals(LocalDateTime.of(2024, 1, 2, 3, 4, 5), parsed);
    }

    @Test
    void shouldParseLocalDateTimeWithPattern() {
        LocalDateTime parsed = TimeUtil.parseLocalDateTime("20240102030405", TimeUtil.YYYYMMDDHHMMSS);
        assertEquals(LocalDateTime.of(2024, 1, 2, 3, 4, 5), parsed);
    }

    @Test
    void shouldReturnNullWhenParsingBlankLocalDateTime() {
        assertNull(TimeUtil.parseLocalDateTime(null));
        assertNull(TimeUtil.parseLocalDateTime(""));
    }

    @Test
    void shouldRejectInvalidDateTimePattern() {
        assertThrows(IllegalArgumentException.class,
                () -> TimeUtil.parseLocalDateTime("2024-01-02 03:04:05", ""));
    }

    @Test
    void testParseLocalDateWithDefaultFormatter() {
        LocalDate parsed = TimeUtil.parseLocalDate("2024-01-02");
        assertEquals(LocalDate.of(2024, 1, 2), parsed);
        assertNull(TimeUtil.parseLocalDate(null));
        assertNull(TimeUtil.parseLocalDate(""));
    }

    @Test
    void testFormatLocalDateOverloads() {
        LocalDate date = LocalDate.of(2024, 1, 2);
        assertEquals("2024-01-02", TimeUtil.formatLocalDate(date));
        assertEquals("20240102", TimeUtil.formatLocalDate(date, TimeUtil.COMPACT_DATE_FORMATTER));
        assertEquals("20240102", TimeUtil.formatLocalDate(date, TimeUtil.YYYYMMDD));
        assertNull(TimeUtil.formatLocalDate(null));
        assertNull(TimeUtil.formatLocalDate(null, TimeUtil.DATE_FORMATTER));
    }

    @Test
    void testFormatParseRoundTrip() {
        LocalDateTime value = LocalDateTime.of(2024, 6, 7, 8, 9, 10);
        String text = TimeUtil.formatLocalDateTime(value);
        assertEquals(value, TimeUtil.parseLocalDateTime(text));

        LocalDate date = LocalDate.of(2024, 6, 7);
        assertEquals(date, TimeUtil.parseLocalDate(TimeUtil.formatLocalDate(date)));
    }

    @Test
    void shouldRejectNullLocalDateTimeForEpochConversion() {
        assertThrows(NullPointerException.class,
                () -> TimeUtil.toEpochMilli((LocalDateTime) null));
    }
}