package com.nebula.dynamic.datasource.core;

import com.nebula.dynamic.datasource.annotation.NebulaDS;
import java.lang.reflect.Method;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;

/**
 * @author : wh
 * @date : 2025/8/21
 * @description:
 */
public class DynamicDataSourceMethodInterceptor implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        NebulaDS ds = getDSAnnotation(invocation);
        boolean pushed = false;
        if (ds != null) {
            DynamicDataSourceContextHolder.setDataSource(ds.value());
            pushed = true;
        }
        try {
            return invocation.proceed();
        } finally {
            if (pushed) {
                DynamicDataSourceContextHolder.clear();
            }
        }

    }

    private NebulaDS getDSAnnotation(MethodInvocation invocation) {
        Method method = invocation.getMethod();
        NebulaDS ds = AnnotatedElementUtils.findMergedAnnotation(method, NebulaDS.class);
        if (ds == null) {
            Class<?> targetClass = AopUtils.getTargetClass(invocation.getThis());
            Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
            ds = AnnotatedElementUtils.findMergedAnnotation(specificMethod, NebulaDS.class);
            if (ds == null) {
                ds = AnnotatedElementUtils.findMergedAnnotation(targetClass, NebulaDS.class);
            }
        }
        return ds;
    }

}