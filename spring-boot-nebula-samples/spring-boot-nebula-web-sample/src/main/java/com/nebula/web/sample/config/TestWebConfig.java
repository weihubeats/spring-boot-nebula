package com.nebula.web.sample.config;

import com.nebula.web.sample.interceptor.LogBodyInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class TestWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LogBodyInterceptor()).addPathPatterns("/test/real-scene");
    }
}
