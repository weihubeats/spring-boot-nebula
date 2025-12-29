package com.nebula.join.context;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.List;

public class RegionRouteHelper {

    private static final ThreadLocal<Boolean> ENABLED = ThreadLocal.withInitial(() -> false);

    private static final TransmittableThreadLocal<List<Long>> USER_REGION_HOLDER = new TransmittableThreadLocal<>();
    // 使用 Integer 实现引用计数，支持嵌套调用
    private static final TransmittableThreadLocal<Integer> REWRITE_COUNT_HOLDER = TransmittableThreadLocal.withInitial(() -> 0);

    private static final ThreadLocal<RouteContextConfig> CONFIG_CONTEXT = ThreadLocal.withInitial(() -> null);

    private RegionRouteHelper() {
    }

    public static void setRegions(List<Long> regions) {
        USER_REGION_HOLDER.set(regions);
    }

    public static void setContextConfig(RouteContextConfig config) {
        CONFIG_CONTEXT.set(config);
    }

    public static List<Long> getRegions() {
        return USER_REGION_HOLDER.get();
    }

    public static RouteContextConfig getContextConfig() {
        return CONFIG_CONTEXT.get();
    }

    public static void clear() {
        USER_REGION_HOLDER.remove();
        REWRITE_COUNT_HOLDER.remove();
        CONFIG_CONTEXT.remove();
    }

    public static boolean isRewriteEnabled() {
        return REWRITE_COUNT_HOLDER.get() > 0;
    }

    public static void startScope() {
        REWRITE_COUNT_HOLDER.set(REWRITE_COUNT_HOLDER.get() + 1);
    }

    public static void startScope(RouteContextConfig config) {
        REWRITE_COUNT_HOLDER.set(REWRITE_COUNT_HOLDER.get() + 1);
        CONFIG_CONTEXT.set(config);
    }

    public static void endScope() {
        Integer current = REWRITE_COUNT_HOLDER.get();
        if (current > 0) {
            current--;
            REWRITE_COUNT_HOLDER.set(current);
            if (current == 0) {
                REWRITE_COUNT_HOLDER.remove();
                CONFIG_CONTEXT.remove();
            }
        }
    }
}
