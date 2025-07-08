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
