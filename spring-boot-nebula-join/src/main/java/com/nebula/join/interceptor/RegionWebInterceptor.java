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
 
package com.nebula.join.interceptor;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nebula.join.context.RegionRouteHelper;
import com.nebula.join.properties.RegionRouteProperties;
import com.nebula.join.provider.RegionProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class RegionWebInterceptor implements HandlerInterceptor {
    
    private final RegionProvider regionProvider;
    
    private final RegionRouteProperties properties;
    
    private final Cache<Long, List<Long>> providerCache;
    
    public RegionWebInterceptor(ObjectProvider<RegionProvider> regionProvider, RegionRouteProperties properties) {
        this.regionProvider = regionProvider.getIfAvailable();
        this.properties = properties;
        
        // 初始化缓存：10分钟过期，最大1万条
        // 注意：这是权限数据缓存，权限回收后最长有 10 分钟延迟生效窗口
        this.providerCache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            if (tryGetFromHeader(request)) {
                return true;
            }
            tryGetFromProvider();
        } catch (Exception e) {
            log.error("init region route exception", e);
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        // 请求结束，彻底清理 ThreadLocal
        RegionRouteHelper.clear();
    }
    
    private boolean tryGetFromHeader(HttpServletRequest request) {
        String headerKey = properties.getHeaderName();
        if (!StringUtils.hasText(headerKey)) {
            return false;
        }
        
        String headerValue = request.getHeader(headerKey);
        if (!StringUtils.hasText(headerValue)) {
            return false;
        }
        
        try {
            List<Long> regionIds = Arrays.stream(headerValue.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            
            if (!regionIds.isEmpty()) {
                RegionRouteHelper.setRegions(regionIds);
                return true;
            }
        } catch (NumberFormatException e) {
            log.warn("Failed to parse region header value", e);
        }
        return false;
    }
    
    private void tryGetFromProvider() {
        if (Objects.isNull(regionProvider)) {
            return;
        }
        Long userId = regionProvider.getCurrentUserId();
        if (userId == null) {
            return;
        }
        List<Long> regions = providerCache.get(userId, key -> {
            List<Long> dbResult = regionProvider.getRegionIds(key);
            return dbResult != null ? dbResult : Collections.emptyList();
        });
        
        if (regions != null && !regions.isEmpty()) {
            RegionRouteHelper.setRegions(regions);
        }
    }
}
