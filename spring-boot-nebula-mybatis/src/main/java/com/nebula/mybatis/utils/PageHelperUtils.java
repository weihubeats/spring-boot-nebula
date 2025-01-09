package com.nebula.mybatis.utils;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nebula.base.model.NebulaPageQuery;
import com.nebula.base.model.NebulaPageRes;
import java.util.Collection;

/**
 * @author : wh
 * @date : 2025/1/8 16:14
 * @description:
 */
public class PageHelperUtils {

    public static <E, T extends NebulaPageQuery> Page<E> startPage(T e) {
        return PageHelper.startPage(e.getPageIndex(), e.getPageSize());
    }

    public static <T> NebulaPageRes<T> of(Collection<T> list, Page page) {
        NebulaPageRes<T> pageResponse = new NebulaPageRes();
        pageResponse.setList(list);
        pageResponse.setTotalCount(page.getTotal());
        pageResponse.setPageSize(page.getPageSize());
        return pageResponse;
    }
    
}
