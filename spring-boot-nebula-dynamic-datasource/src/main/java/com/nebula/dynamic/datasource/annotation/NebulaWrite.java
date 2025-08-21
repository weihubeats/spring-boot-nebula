package com.nebula.dynamic.datasource.annotation;

import com.nebula.dynamic.datasource.DynamicConstant;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author : wh
 * @date : 2025/8/21
 * @description:
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NebulaDS(DynamicConstant.WRITE)
public @interface NebulaWrite {
}
