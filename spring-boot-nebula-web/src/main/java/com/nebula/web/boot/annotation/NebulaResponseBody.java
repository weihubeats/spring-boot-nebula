package com.nebula.web.boot.annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
    Class<? extends ObjectMapper> objectMapper();

}
