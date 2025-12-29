package com.nebula.join.template;

import com.nebula.join.context.RegionRouteHelper;

import java.util.function.Supplier;

public class RegionRouteTemplate {

    /**
     * 执行带返回值的业务逻辑，并开启区域路由改写
     *
     * @param supplier 业务逻辑
     * @param <T>      返回值类型
     * @return 业务执行结果
     */
    public <T> T execute(Supplier<T> supplier) {
        RegionRouteHelper.startScope();
        return supplier.get();
    }

    /**
     * 执行无返回值的业务逻辑，并开启区域路由改写
     *
     * @param runnable 业务逻辑
     */
    public void run(Runnable runnable) {
        RegionRouteHelper.startScope();
        try {
            runnable.run();
        } finally {
            RegionRouteHelper.endScope();
        }
    }
}