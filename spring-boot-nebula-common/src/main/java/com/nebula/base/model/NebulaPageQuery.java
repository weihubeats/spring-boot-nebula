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

/**
 * @author : wh
 * @date : 2023/8/16 10:04
 * @description:
 */
public abstract class NebulaPageQuery {
    
    public static final String ASC = "ASC";
    
    public static final String DESC = "DESC";
    
    private static final int DEFAULT_PAGE_SIZE = 20;
    
    private Integer pageSize = DEFAULT_PAGE_SIZE;
    
    private Integer pageIndex = 1;
    
    private String orderBy;
    
    private String orderDirection = DESC;
    
    private String groupBy;
    
    private boolean needTotalCount = true;
    
    public int getPageIndex() {
        if (pageIndex < 1) {
            return 1;
        }
        return pageIndex;
    }
    
    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }
    
    public int getPageSize() {
        if (pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        if (pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        this.pageSize = pageSize;
    }
    
    public int getOffset() {
        return (getPageIndex() - 1) * getPageSize();
    }
    
    public String getOrderBy() {
        return orderBy;
    }
    
    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }
    
    public String getOrderDirection() {
        return orderDirection;
    }
    
    public void setOrderDirection(String orderDirection) {
        if (ASC.equalsIgnoreCase(orderDirection) || DESC.equalsIgnoreCase(orderDirection)) {
            this.orderDirection = orderDirection;
        }
    }
    
    public String getGroupBy() {
        return groupBy;
    }
    
    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }
    
    public boolean isNeedTotalCount() {
        return needTotalCount;
    }
    
    public void setNeedTotalCount(boolean needTotalCount) {
        this.needTotalCount = needTotalCount;
    }
}
