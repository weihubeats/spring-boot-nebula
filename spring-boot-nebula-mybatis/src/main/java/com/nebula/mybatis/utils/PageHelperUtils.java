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
