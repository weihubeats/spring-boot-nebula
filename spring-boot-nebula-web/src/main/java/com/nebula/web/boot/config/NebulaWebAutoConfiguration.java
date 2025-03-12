package com.nebula.web.boot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : wh
 * @date : 2025/3/12 17:02
 * @description:
 */
@EnableConfigurationProperties(NebulaWebProperties.class)
@Configuration
public class NebulaWebAutoConfiguration {
    
    @Bean
    public BaseWebMvcConfig baseWebMvcConfig(ObjectMapper objectMapper, NebulaWebProperties webProperties) {
        return new BaseWebMvcConfig(objectMapper, webProperties);
        
    }
}
