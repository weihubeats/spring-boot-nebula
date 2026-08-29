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
 
package com.nebula.base.pagination;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 基础分页结果（不可变）
 *
 * @author : wh
 * @date : 2023/8/16 10:02
 */
public record NebulaPageRes<T>(List<T> list, long totalCount, int pageSize, int pageIndex)
        implements Serializable {
    
    private static final long serialVersionUID = 3375291462084173562L;
    
    private static final int MIN_PAGE_SIZE = 1;
    
    private static final int DEFAULT_PAGE_INDEX = 1;
    
    public NebulaPageRes {
        list = (list == null) ? List.of() : List.copyOf(list);
        totalCount = Math.max(0L, totalCount);
        pageSize = Math.max(MIN_PAGE_SIZE, pageSize);
        pageIndex = Math.max(DEFAULT_PAGE_INDEX, pageIndex);
    }
    
    public static <T> NebulaPageRes<T> of(Collection<T> list, long total, int pageSize) {
        return of(list, total, pageSize, DEFAULT_PAGE_INDEX);
    }
    
    public static <T> NebulaPageRes<T> of(Collection<T> list, long total, int pageSize, int pageIndex) {
        return new NebulaPageRes<>(toList(list), total, pageSize, pageIndex);
    }
    
    public static <T> NebulaPageRes<T> of(Collection<T> list, int pageSize) {
        return of(list, (list == null) ? 0L : list.size(), pageSize);
    }
    
    /**
     * 内存分页
     */
    public static <T> NebulaPageRes<T> ofMemory(Collection<T> source, NebulaPageQuery pageQuery) {
        if (source == null || source.isEmpty()) {
            return of(Collections.emptyList(), 0, pageQuery.getPageSize(), pageQuery.getPageIndex());
        }
        
        int pageSize = pageQuery.getPageSize();
        long skip = pageQuery.getOffset();
        
        if (skip >= source.size()) {
            return of(Collections.emptyList(), source.size(), pageSize, pageQuery.getPageIndex());
        }
        
        List<T> pageList = source.stream()
                .skip(skip)
                .limit(pageSize)
                .collect(Collectors.toList());
        
        return of(pageList, source.size(), pageSize, pageQuery.getPageIndex());
    }
    
    public static <T, R> NebulaPageRes<R> copy(NebulaPageRes<T> source, Function<T, R> converter) {
        Objects.requireNonNull(source, "Source page cannot be null");
        Objects.requireNonNull(converter, "Converter function cannot be null");
        
        return new NebulaPageRes<>(
                source.list().stream().map(converter).collect(Collectors.toList()),
                source.totalCount(),
                source.pageSize(),
                source.pageIndex());
    }
    
    public boolean isEmpty() {
        return list.isEmpty();
    }
    
    public long totalPages() {
        return (totalCount + pageSize - 1) / pageSize;
    }
    
    public static <T> NebulaPageRes<T> empty() {
        return empty(MIN_PAGE_SIZE);
    }
    
    public static <T> NebulaPageRes<T> empty(int pageSize) {
        return of(Collections.emptyList(), 0, pageSize);
    }
    
    public static <T> NebulaPageRes<T> empty(NebulaPageQuery pageQuery) {
        return of(Collections.emptyList(), 0, pageQuery.getPageSize(), pageQuery.getPageIndex());
    }
    
    private static <T> List<T> toList(Collection<T> collection) {
        if (collection == null) {
            return Collections.emptyList();
        }
        if (collection instanceof List<T> l) {
            return l;
        }
        return List.copyOf(collection);
    }
    
    @Override
    public String toString() {
        return "NebulaPageRes{totalCount=" + totalCount + ", pageSize=" + pageSize
                + ", pageIndex=" + pageIndex + ", listSize=" + list.size() + '}';
    }
}
