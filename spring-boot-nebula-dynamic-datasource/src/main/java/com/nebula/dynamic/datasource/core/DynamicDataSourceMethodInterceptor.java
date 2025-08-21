package com.nebula.dynamic.datasource.core;

import com.nebula.dynamic.datasource.annotation.NebulaDS;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author : wh
 * @date : 2025/8/21
 * @description:
 */
public class DynamicDataSourceMethodInterceptor implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        NebulaDS ds = getDSAnnotation(invocation);
        if (ds != null) {
            DynamicDataSourceContextHolder.setDataSource(ds.value());
        }
        try {
            return invocation.proceed();
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }

    private NebulaDS getDSAnnotation(MethodInvocation invocation) {
        NebulaDS ds = invocation.getMethod().getAnnotation(NebulaDS.class);
        if (ds == null) {
            ds = invocation.getThis().getClass().getAnnotation(NebulaDS.class);
        }
        return ds;
    }

}