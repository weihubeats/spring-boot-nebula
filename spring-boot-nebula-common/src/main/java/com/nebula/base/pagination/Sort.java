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
import java.util.regex.Pattern;

/**
 * 单列排序，列名经白名单校验防止 SQL 注入
 *
 * @author : wh
 * @date : 2026/8/24
 */
public record Sort(String column, SortDirection direction) implements Serializable {
    
    private static final long serialVersionUID = -7412085366214753901L;
    
    private static final Pattern COLUMN_PATTERN = Pattern.compile("^[a-zA-Z0-9_.]+$");
    
    public Sort {
        if (column == null || !COLUMN_PATTERN.matcher(column).matches()) {
            throw new IllegalArgumentException("Invalid column identifier: " + column);
        }
        direction = (direction == null) ? SortDirection.DESC : direction;
    }
    
    public static Sort asc(String column) {
        return new Sort(column, SortDirection.ASC);
    }
    
    public static Sort desc(String column) {
        return new Sort(column, SortDirection.DESC);
    }
    
    public String toOrderBySql() {
        return column + " " + direction.name();
    }
    
    @Override
    public String toString() {
        return toOrderBySql();
    }
}
