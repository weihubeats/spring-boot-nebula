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
 
package com.nebula.mybatis.sample.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nebula.mybatis.handler.ArrayTypeHandler;
import com.nebula.mybatis.handler.ListTypeHandler;
import java.util.List;
import lombok.Data;

/**
 * @author : wh
 * @date : 2025/1/8 16:40
 * @description:
 */
@Data
@TableName("student")
public class StudentDO {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private Integer age;
    
    @TableField(typeHandler = ArrayTypeHandler.class)
    private String[] tags;
    @TableField(typeHandler = ListTypeHandler.class)
    private List<String> tags1;
    
}
