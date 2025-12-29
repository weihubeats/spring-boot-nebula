package com.nebula.join.utils;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nebula.join.context.RegionRouteHelper;

public class RegionPageHelper {

    /**
     * 改写计数器统一由 拦截器释放
     * @param pageNum
     * @param pageSize
     * @return
     * @param <E>
     */
    public static <E> Page<E> startPage(int pageNum, int pageSize) {
        RegionRouteHelper.startScope();
        return PageHelper.startPage(pageNum, pageSize);
    }
}