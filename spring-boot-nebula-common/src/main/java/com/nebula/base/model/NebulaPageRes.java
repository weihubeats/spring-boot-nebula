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
 
package com.nebula.base.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author : wh
 * @date : 2023/8/16 10:02
 * @description: 基础分页对象
 */
public class NebulaPageRes<T> {
    
    private static final int DEFAULT_PAGE_SIZE = 1;
    
    private static final int MIN_PAGE_SIZE = 1;
    
    private static final long EMPTY_TOTAL_COUNT = 0L;
    
    private Collection<T> list = Collections.emptyList();
    
    private long totalCount = EMPTY_TOTAL_COUNT;
    
    private int pageSize = DEFAULT_PAGE_SIZE;
    
    public long getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(long totalCount) {
        this.totalCount = Math.max(EMPTY_TOTAL_COUNT, totalCount);
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = Math.max(MIN_PAGE_SIZE, pageSize);
        
    }
    
    public Collection<T> getList() {
        return list;
    }
    
    public void setList(Collection<T> list) {
        this.list = (list != null) ? new ArrayList<>(list) : Collections.emptyList();
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
    public static <T> NebulaPageRes<T> of(Collection<T> list, long total, int pageSize) {
        NebulaPageRes<T> page = new NebulaPageRes<>();
        page.setList(list);
        page.setTotalCount(total);
        page.setPageSize(pageSize);
        return page;
    }
    
    public static <T> NebulaPageRes<T> of(Collection<T> list, int pageSize) {
        return of(list, list != null ? list.size() : 0, pageSize);
    }
    
    /**
     * 内存分页
     *
     * @param source
     * @param pageQuery
     * @param <T>
     * @return
     */
    public static <T> NebulaPageRes<T> ofMemory(Collection<T> source, NebulaPageQuery pageQuery) {
        if (source == null || source.isEmpty()) {
            return of(Collections.emptyList(), 0, pageQuery.getPageSize());
        }
        
        int pageSize = pageQuery.getPageSize();
        int pageIndex = pageQuery.getPageIndex();
        long skip = (long) (pageIndex - 1) * pageSize;
        
        // 处理超出范围的情况
        if (skip >= source.size()) {
            return of(Collections.emptyList(), source.size(), pageSize);
        }
        
        List<T> pageList = source.stream()
                .skip(skip)
                .limit(pageSize)
                .collect(Collectors.toList());
        
        return of(pageList, source.size(), pageSize);
    }
    
    public static <T, R> NebulaPageRes<R> copy(NebulaPageRes<T> source, Function<T, R> converter) {
        Objects.requireNonNull(source, "Source page cannot be null");
        Objects.requireNonNull(converter, "Converter function cannot be null");
        
        NebulaPageRes<R> result = new NebulaPageRes<>();
        result.setList(
                source.getList().stream()
                        .map(converter)
                        .collect(Collectors.toList()));
        result.setTotalCount(source.getTotalCount());
        result.setPageSize(source.getPageSize());
        return result;
    }
    
    public boolean isEmpty() {
        return list.isEmpty();
    }
    
    public static <T> NebulaPageRes<T> empty() {
        return empty(DEFAULT_PAGE_SIZE);
    }
    
    public static <T> NebulaPageRes<T> empty(int pageSize) {
        return of(Collections.emptyList(), 0, pageSize);
    }
    
    public static <T> NebulaPageRes<T> empty(NebulaPageQuery pageQuery) {
        return empty(pageQuery.getPageSize());
    }
    
}
