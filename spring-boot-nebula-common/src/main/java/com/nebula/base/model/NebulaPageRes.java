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

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author : wh
 * @date : 2023/8/16 10:02
 * @description: 基础分页对象
 */
public class NebulaPageRes<T> {
    
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
    public static <T> NebulaPageRes<T> of(Collection<T> list, long total, int pageSize) {
        NebulaPageRes<T> page = new NebulaPageRes<>();
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
    public static <T> NebulaPageRes<T> ofMemory(Collection<T> list, NebulaPageQuery pageQuery) {
        List<T> pageList = list.stream().skip((long) (pageQuery.getPageIndex() - 1) * pageQuery.getPageSize())
                .limit(pageQuery.getPageSize()).collect(Collectors.toList());
        NebulaPageRes<T> page = new NebulaPageRes<>();
        page.setList(pageList);
        page.setTotalCount(list.size());
        page.setPageSize(pageQuery.getPageSize());
        return page;
    }
    
    public static <T, R> NebulaPageRes<R> copy(NebulaPageRes<T> source, Function<T, R> converterFunction) {
        NebulaPageRes<R> Page = new NebulaPageRes<>();
        Page.setList(source.getList().stream().map(converterFunction).collect(Collectors.toList()));
        Page.setTotalCount(source.getTotalCount());
        Page.setPageSize(source.getPageSize());
        return Page;
    }
    
}
