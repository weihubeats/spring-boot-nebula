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

import jakarta.validation.constraints.Min;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页查询参数。设计为 final 具体类，业务查询 DTO 通过组合方式持有：
 * <pre>
 *     public class UserQuery {
 *         private String name;
 *         private NebulaPageQuery page = new NebulaPageQuery();
 *     }
 * </pre>
 *
 * @author : wh
 * @date : 2023/8/16 10:04
 */
public final class NebulaPageQuery implements Serializable {
    
    private static final long serialVersionUID = -8802416375219054762L;
    
    private static final int DEFAULT_PAGE_INDEX = 1;
    
    private static final int DEFAULT_PAGE_SIZE = 20;
    
    private static final int MIN_PAGE_SIZE = 1;
    
    @Min(value = 1, message = "pageIndex must be >= 1")
    private Integer pageIndex = DEFAULT_PAGE_INDEX;
    
    @Min(value = 1, message = "pageSize must be >= 1")
    private Integer pageSize = DEFAULT_PAGE_SIZE;
    
    private List<Sort> sorts = List.of();
    
    private boolean needTotalCount = true;
    
    public int getPageIndex() {
        return (pageIndex == null || pageIndex < 1) ? DEFAULT_PAGE_INDEX : pageIndex;
    }
    
    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = (pageIndex == null) ? DEFAULT_PAGE_INDEX : Math.max(pageIndex, DEFAULT_PAGE_INDEX);
    }
    
    public int getPageSize() {
        return (pageSize == null || pageSize < 1) ? DEFAULT_PAGE_SIZE : pageSize;
    }
    
    public void setPageSize(Integer pageSize) {
        this.pageSize = (pageSize == null) ? DEFAULT_PAGE_SIZE : Math.max(pageSize, MIN_PAGE_SIZE);
    }
    
    /**
     * 查询偏移量，long 防止深页码整型溢出
     */
    public long getOffset() {
        return (long) (getPageIndex() - 1) * getPageSize();
    }
    
    public List<Sort> getSorts() {
        return sorts;
    }
    
    public void setSorts(List<Sort> sorts) {
        this.sorts = (sorts == null) ? List.of() : List.copyOf(sorts);
    }
    
    public void addSort(Sort sort) {
        List<Sort> next = new ArrayList<>(this.sorts);
        next.add(sort);
        this.sorts = List.copyOf(next);
    }
    
    public boolean isNeedTotalCount() {
        return needTotalCount;
    }
    
    public void setNeedTotalCount(boolean needTotalCount) {
        this.needTotalCount = needTotalCount;
    }
    
    @Override
    public String toString() {
        return "NebulaPageQuery{pageIndex=" + getPageIndex() + ", pageSize=" + getPageSize()
                + ", sorts=" + sorts + ", needTotalCount=" + needTotalCount + '}';
    }
}
