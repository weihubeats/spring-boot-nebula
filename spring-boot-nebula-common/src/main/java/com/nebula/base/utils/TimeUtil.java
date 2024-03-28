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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @author : wh
 * @date : 2023/5/12 10:30
 * @description:
 */
public class TimeUtil {
    
    public static final String YYYYMMddHHmmss = "yyyy-MM-dd HH:mm:ss";
    
    public static final String YYYYMMdd = "yyyy-MM-dd";
    
    public static final String HHmmss = "HH:mm:ss";
    
    public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern(YYYYMMddHHmmss);
    
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(YYYYMMdd);
    
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern(HHmmss);
    
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
