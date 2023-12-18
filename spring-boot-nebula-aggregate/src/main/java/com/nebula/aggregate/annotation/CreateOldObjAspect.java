package com.nebula.aggregate.annotation;

import com.nebula.aggregate.core.AbstractOldObj;
import com.nebula.base.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author : wh
 * @date : 2023/12/18 19:57
 * @description:
 */
@Aspect
@Slf4j
@Component
public class CreateOldObjAspect {

    @AfterReturning(pointcut = "@annotation(com.nebula.aggregate.annotation.AggregateCreate) || " +
        "@annotation(com.nebula.aggregate.annotation.CreateOldObj)", returning = "returnVal")
    public void handleRequestMethod(JoinPoint pjp, Object returnVal) {
        if (returnVal instanceof AbstractOldObj) {
            ((AbstractOldObj) returnVal).setOldObject(copy(returnVal));
        }
    }

    public Object copy(Object oldObject) {
        return JsonUtil.json2JavaBean(JsonUtil.toJSONString(oldObject), oldObject.getClass());
    }

}
