package com.nebula.web.common.autoconfigure;

import com.nebula.web.common.utils.SpringBeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : wh
 * @date : 2024/3/16 10:33
 * @description:
 */
@Configuration(proxyBeanMethods = false)
public class NebulaApplicationContextAware {
    
    @Bean
    public SpringBeanUtils springBeanUtils() {
        return new SpringBeanUtils();
    }
    
    
}
