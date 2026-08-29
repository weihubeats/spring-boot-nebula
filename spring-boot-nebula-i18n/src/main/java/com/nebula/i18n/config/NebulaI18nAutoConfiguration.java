/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package com.nebula.i18n.config;

import com.nebula.i18n.core.NebulaCompositeMessageSource;
import com.nebula.i18n.core.NebulaLocaleResolver;
import com.nebula.i18n.remote.HttpRemoteMessageLoader;
import com.nebula.i18n.remote.RemoteMessageLoader;
import com.nebula.i18n.remote.RemoteMessageSource;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.servlet.LocaleResolver;

/**
 * 国际化自动配置：
 * <ul>
 *   <li>装配全局 {@link MessageSource}（本地资源为兜底，远程文案优先覆盖）</li>
 *   <li>装配请求语言解析 {@link LocaleResolver}（X-Lang / lang / Accept-Language）</li>
 *   <li>装配远程文案加载 {@link RemoteMessageLoader}（内置 HTTP 实现，可被自定义 bean 覆盖）</li>
 * </ul>
 */
@AutoConfiguration(before = {MessageSourceAutoConfiguration.class, WebMvcAutoConfiguration.class})
@EnableConfigurationProperties(NebulaI18nProperties.class)
@ConditionalOnProperty(prefix = "nebula.i18n", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NebulaI18nAutoConfiguration {
    
    @Bean(name = "messageSource")
    @ConditionalOnMissingBean(MessageSource.class)
    public MessageSource nebulaMessageSource(NebulaI18nProperties properties,
                                             @Autowired(required = false) RemoteMessageLoader remoteMessageLoader,
                                             @Autowired(required = false) ThreadPoolTaskScheduler nebulaI18nTaskScheduler) {
        ResourceBundleMessageSource local = new ResourceBundleMessageSource();
        local.setBasename(properties.getBasename());
        local.setDefaultEncoding(properties.getCharset());
        local.setFallbackToSystemLocale(properties.isFallbackToDefault());
        local.setUseCodeAsDefaultMessage(false);
        
        if (remoteMessageLoader == null) {
            return local;
        }
        
        RemoteMessageSource remote = new RemoteMessageSource(remoteMessageLoader);
        remote.refresh();
        if (properties.getRemote().getRefreshIntervalSeconds() > 0 && nebulaI18nTaskScheduler != null) {
            nebulaI18nTaskScheduler.scheduleWithFixedDelay(remote::refresh,
                    Duration.ofSeconds(properties.getRemote().getRefreshIntervalSeconds()));
        }
        
        NebulaCompositeMessageSource composite = new NebulaCompositeMessageSource(remote, local);
        return composite;
    }
    
    @Bean
    @ConditionalOnMissingBean(RemoteMessageLoader.class)
    @ConditionalOnProperty(prefix = "nebula.i18n.remote", name = "enabled", havingValue = "true")
    public RemoteMessageLoader httpRemoteMessageLoader(NebulaI18nProperties properties) {
        return new HttpRemoteMessageLoader(properties);
    }
    
    @Bean
    @ConditionalOnMissingBean(ThreadPoolTaskScheduler.class)
    public ThreadPoolTaskScheduler nebulaI18nTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("nebula-i18n-");
        scheduler.setDaemon(true);
        return scheduler;
    }
    
    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnMissingBean(LocaleResolver.class)
    public LocaleResolver localeResolver(NebulaI18nProperties properties) {
        return new NebulaLocaleResolver(properties);
    }
    
}
