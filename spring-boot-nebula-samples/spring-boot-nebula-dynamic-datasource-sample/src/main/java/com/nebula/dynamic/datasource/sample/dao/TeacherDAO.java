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
 
package com.nebula.dynamic.datasource.sample.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nebula.base.model.NebulaPageRes;
import com.nebula.dynamic.datasource.sample.dao.entity.TeacherDO;
import com.nebula.dynamic.datasource.sample.dto.TeacherDTO;
import com.nebula.dynamic.datasource.sample.vo.TeacherVO;

/**
 * @author : wh
 * @date : 2025/1/8 17:10
 * @description:
 */
public interface TeacherDAO extends IService<TeacherDO> {
    
    NebulaPageRes<TeacherVO> list(TeacherDTO dto);
}
