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
 
package com.nebula.base.utils;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author : wh
 * @date : 2025/6/30
 * @description:
 */
public final class SingletonUtils {
    
    private static final ConcurrentHashMap<String, Object> INSTANCE_CACHE = new ConcurrentHashMap<>();
    
    private SingletonUtils() {
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) INSTANCE_CACHE.get(key);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T get(String key, Supplier<T> supplier) {
        return (T) INSTANCE_CACHE.computeIfAbsent(key, k -> supplier.get());
    }
    
    public static <T> T get(Class<T> clazz) {
        return get(clazz.getName(), () -> {
            try {
                Constructor<T> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create singleton instance for class " + clazz.getName(), e);
            }
        });
    }
    
    public static <T> T get(Class<T> clazz, Supplier<T> supplier) {
        return get(clazz.getName(), supplier);
    }
    
    public static void remove(String key) {
        INSTANCE_CACHE.remove(key);
    }
    
    public static void remove(Class<?> clazz) {
        INSTANCE_CACHE.remove(clazz.getName());
    }
    
    public static void clear() {
        INSTANCE_CACHE.clear();
    }
    
}
