package com.nebula.dynamic.datasource.core;

import java.util.Deque;
import java.util.LinkedList;


/**
 * @author : wh
 * @date : 2025/8/21
 * @description:
 */
public class DynamicDataSourceContextHolder {

    private static final ThreadLocal<Deque<String>> CONTEXT_HOLDER = ThreadLocal.withInitial(LinkedList::new);


    /**
     * 设置数据源
     */
    public static void setDataSource(String dataSource) {
        CONTEXT_HOLDER.get().push(dataSource);
    }

    /**
     * 获取当前数据源
     */
    public static String getDataSource() {
        Deque<String> deque = CONTEXT_HOLDER.get();
        return deque.isEmpty() ? null : deque.peek();
    }

    /**
     * 清除当前数据源（回退到上一个，如果有的话）
     */
    public static void clear() {
        Deque<String> deque = CONTEXT_HOLDER.get();
        if (!deque.isEmpty()) {
            deque.pop();
        }
        if (deque.isEmpty()) {
            CONTEXT_HOLDER.remove();
        }
    }

}
