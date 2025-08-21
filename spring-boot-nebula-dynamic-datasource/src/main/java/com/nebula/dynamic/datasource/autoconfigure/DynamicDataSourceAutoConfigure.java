package com.nebula.dynamic.datasource.autoconfigure;

import com.nebula.dynamic.datasource.annotation.NebulaDS;
import com.nebula.dynamic.datasource.core.DynamicDataSourceMethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : wh
 * @date : 2025/8/21
 * @description:
 */
@Configuration
public class DynamicDataSourceAutoConfigure {

    @Bean
    public Advisor dynamicDataSourceAdvisor() {
        AnnotationMatchingPointcut classPointcut = new AnnotationMatchingPointcut(NebulaDS.class, true);
        Pointcut methodPointcut = AnnotationMatchingPointcut.forMethodAnnotation(NebulaDS.class);
        Pointcut union = new ComposablePointcut(classPointcut).union(methodPointcut);
        return new DefaultPointcutAdvisor(union, new DynamicDataSourceMethodInterceptor());
    }
}
