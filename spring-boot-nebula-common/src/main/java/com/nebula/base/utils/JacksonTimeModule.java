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

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.PackageVersion;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * java 8 时间默认序列化 maybe use link{ @com.fasterxml.jackson.datatype.jsr310.JavaTimeModule }
 */
public class JacksonTimeModule extends SimpleModule {
    
    public JacksonTimeModule() {
        super(PackageVersion.VERSION);
        this.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(TimeUtil.DATETIME_FORMATTER));
        this.addDeserializer(LocalDate.class, new LocalDateDeserializer(TimeUtil.DATE_FORMATTER));
        this.addDeserializer(LocalTime.class, new LocalTimeDeserializer(TimeUtil.TIME_FORMATTER));
        this.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(TimeUtil.DATETIME_FORMATTER));
        this.addSerializer(LocalDate.class, new LocalDateSerializer(TimeUtil.DATE_FORMATTER));
        this.addSerializer(LocalTime.class, new LocalTimeSerializer(TimeUtil.TIME_FORMATTER));
    }
    
}
