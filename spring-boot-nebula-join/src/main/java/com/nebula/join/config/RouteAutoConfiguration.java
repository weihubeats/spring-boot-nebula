package com.nebula.join.config;

import com.nebula.join.interceptor.RegionWebInterceptor;
import com.nebula.join.properties.RegionRouteProperties;
import com.nebula.join.provider.RegionProvider;
import com.nebula.join.template.RegionRouteTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(RegionRouteProperties.class)
@ConditionalOnProperty(prefix = "region-route", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RouteAutoConfiguration {

    @Bean
    public RegionWebInterceptor regionWebInterceptor(ObjectProvider<RegionProvider> regionProvider,
                                                     RegionRouteProperties properties) {
        return new RegionWebInterceptor(regionProvider, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public RegionRouteTemplate regionRouteTemplate() {
        return new RegionRouteTemplate();
    }


    @Bean
    @ConditionalOnBean(RegionWebInterceptor.class)
    public WebMvcConfigurer regionWebMvcConfigurer(RegionWebInterceptor interceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(interceptor)
                    .addPathPatterns("/**");
            }
        };
    }
}
