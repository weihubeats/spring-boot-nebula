package com.nebula.dynamic.datasource.autoconfigure;

import com.nebula.dynamic.datasource.annotation.NebulaDS;
import com.nebula.dynamic.datasource.core.DynamicDataSourceMethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * @author : wh
 * @date : 2025/8/21
 * @description:
 */
@AutoConfiguration(after = DataSourceAutoConfiguration.class)
public class DynamicDataSourceAutoConfigure {

    @Bean
    public Advisor dynamicDataSourceAdvisor() {
        AnnotationMatchingPointcut classPointcut = new AnnotationMatchingPointcut(NebulaDS.class, true);
        Pointcut methodPointcut = AnnotationMatchingPointcut.forMethodAnnotation(NebulaDS.class);
        Pointcut union = new ComposablePointcut(classPointcut).union(methodPointcut);
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(union, new DynamicDataSourceMethodInterceptor());
        advisor.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return advisor;
    }
}
