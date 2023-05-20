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
 * java 8 时间默认序列化
 */
public class JacksonTimeModule extends SimpleModule {


    public JacksonTimeModule() {
        super(PackageVersion.VERSION);
        this.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(TimeUtil.DATETIME_FORMAT));
        this.addDeserializer(LocalDate.class, new LocalDateDeserializer(TimeUtil.DATE_FORMAT));
        this.addDeserializer(LocalTime.class, new LocalTimeDeserializer(TimeUtil.TIME_FORMAT));
        this.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(TimeUtil.DATETIME_FORMAT));
        this.addSerializer(LocalDate.class, new LocalDateSerializer(TimeUtil.DATE_FORMAT));
        this.addSerializer(LocalTime.class, new LocalTimeSerializer(TimeUtil.TIME_FORMAT));
    }


}
