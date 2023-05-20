package com.nebula.web.boot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.nebula.web.boot.interceptor.NebulaResponseBodyHandleReturnValue;
import com.nebula.web.boot.jackson.MappingApiJackson2HttpMessageConverter;
import com.nebula.web.boot.resolver.TimestampArgumentResolver;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.ResourceRegionHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author XiaoLei
 * @date 2021/1/25 22:38
 * @description spring boot 所有的设置，主要是用于设置全局的中间件
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Configuration(proxyBeanMethods = false)
@AllArgsConstructor
public class BaseWebMvcConfig implements WebMvcConfigurer {


    private final ObjectMapper objectMapper;


    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        returnValueHandlers.add(new NebulaResponseBodyHandleReturnValue());
    }


    /**
     * 使用 JACKSON 作为JSON MessageConverter
     * @param converters
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.removeIf(x -> x instanceof StringHttpMessageConverter || x instanceof AbstractJackson2HttpMessageConverter);
        converters.add(new StringHttpMessageConverter(Charsets.UTF_8));
        converters.add(new ByteArrayHttpMessageConverter());
        converters.add(new ResourceHttpMessageConverter());
        converters.add(new ResourceRegionHttpMessageConverter());
        converters.add(new MappingApiJackson2HttpMessageConverter(objectMapper));
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new TimestampArgumentResolver());
    }

}
