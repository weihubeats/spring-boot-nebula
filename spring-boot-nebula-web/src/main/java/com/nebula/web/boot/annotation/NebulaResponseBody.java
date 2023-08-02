package com.nebula.web.boot.annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nebula.base.utils.JsonUtil;

import java.lang.annotation.*;

/**
 * @author : wh
 * @date : 2023/4/13 10:11
 * @description:
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NebulaResponseBody {

    /**
     * objectMapper
     *
     * @return
     */
    Class<? extends ObjectMapper> objectMapper() default JsonUtil.JacksonObjectMapper.class;

}
