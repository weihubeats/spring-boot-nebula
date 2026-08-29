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
 
package com.nebula.mybatis.utils;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nebula.base.pagination.NebulaPageQuery;
import com.nebula.base.pagination.NebulaPageRes;
import com.nebula.base.pagination.Sort;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author : wh
 * @date : 2025/1/8 16:14
 * @description:
 */
public class PageHelperUtils {
    
    public static <E> Page<E> startPage(NebulaPageQuery query) {
        Page<E> page = PageHelper.startPage(query.getPageIndex(), query.getPageSize(), query.isNeedTotalCount());
        if (!query.getSorts().isEmpty()) {
            page.setOrderBy(query.getSorts().stream()
                    .map(Sort::toOrderBySql)
                    .collect(Collectors.joining(",")));
        }
        return page;
    }
    
    public static <T> NebulaPageRes<T> of(Collection<T> list, Page<?> page) {
        return NebulaPageRes.of(list, page.getTotal(), page.getPageSize(), page.getPageNum());
    }
}
