package com.nebula.web.boot.api;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author : wh
 * @date : 2023/8/16 10:02
 * @description: 基础分页对象
 */
public class NebulaPage<T> {

    private long totalCount = 0;

    private int pageSize = 1;

    private Collection<T> list;

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public int getPageSize() {
        if (pageSize < 1) {
            return 1;
        }
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        if (pageSize < 1) {
            this.pageSize = 1;
        } else {
            this.pageSize = pageSize;
        }
    }

    public Collection<T> getList() {
        return list;
    }

    public void setList(Collection<T> list) {
        this.list = list;
    }

    /**
     * 构造器
     *
     * @param list
     * @param total
     * @param pageSize
     * @param <T>
     * @return
     */
    public static <T> NebulaPage<T> of(Collection<T> list, long total, int pageSize) {
        NebulaPage<T> page = new NebulaPage<>();
        page.setList(list);
        page.setTotalCount(total);
        page.setPageSize(pageSize);
        return page;
    }

    /**
     * 内存分页
     *
     * @param list
     * @param pageQuery
     * @param <T>
     * @return
     */
    public static <T> NebulaPage<T> ofMemory(Collection<T> list, NebulaPageQuery pageQuery) {
        List<T> pageList = list.stream().skip((long) (pageQuery.getPageIndex() - 1) * pageQuery.getPageSize())
            .limit(pageQuery.getPageSize()).collect(Collectors.toList());
        NebulaPage<T> page = new NebulaPage<>();
        page.setList(pageList);
        page.setTotalCount(list.size());
        page.setPageSize(pageQuery.getPageSize());
        return page;
    }

    public static <T, R> NebulaPage<R> copy(NebulaPage<T> source, Function<T, R> converterFunction) {
        NebulaPage<R> Page = new NebulaPage<>();
        Page.setList(source.getList().stream().map(converterFunction).collect(Collectors.toList()));
        Page.setTotalCount(source.getTotalCount());
        Page.setPageSize(source.getPageSize());
        return Page;
    }

}
