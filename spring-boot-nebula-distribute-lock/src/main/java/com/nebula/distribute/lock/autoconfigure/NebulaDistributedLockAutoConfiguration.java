package com.nebula.distribute.lock.autoconfigure;

import com.nebula.aop.base.NebulaBaseAnnotationAdvisor;
import com.nebula.distribute.lock.annotation.NebulaDistributedLock;
import com.nebula.distribute.lock.aop.NebulaDistributedLockAnnotationInterceptor;
import com.nebula.distribute.lock.core.NebulaDistributedLockTemplate;
import com.nebula.distribute.lock.core.RedissonDistributedLockTemplate;
import org.redisson.api.RedissonClient;
import org.springframework.aop.Advisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author : wh
 * @date : 2024/3/15 13:39
 * @description:
 */
@Configuration(proxyBeanMethods = false)
public class NebulaDistributedLockAutoConfiguration {

    @Bean
    public RedissonDistributedLockTemplate redissonDistributedLockTemplate(RedissonClient redissonClient) {
        RedissonDistributedLockTemplate template = new RedissonDistributedLockTemplate(redissonClient);
        return template;
    }

    @Bean
    @Order(1)
    public Advisor distributedLockAnnotationAdvisor(NebulaDistributedLockTemplate nebulaDistributedLockTemplate) {
        NebulaDistributedLockAnnotationInterceptor advisor = new NebulaDistributedLockAnnotationInterceptor(nebulaDistributedLockTemplate);
        return new NebulaBaseAnnotationAdvisor(advisor, NebulaDistributedLock.class);
    }

}
