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
 
package com.nebula.dynamic.datasource.sample.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.nebula.base.model.NebulaPageRes;
import com.nebula.dynamic.datasource.sample.converter.TeacherConverter;
import com.nebula.dynamic.datasource.sample.dao.TeacherDAO;
import com.nebula.dynamic.datasource.sample.dao.entity.TeacherDO;
import com.nebula.dynamic.datasource.sample.dao.mapper.TeacherMapper;
import com.nebula.dynamic.datasource.sample.dto.TeacherDTO;
import com.nebula.dynamic.datasource.sample.vo.TeacherVO;
import com.nebula.mybatis.utils.PageHelperUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * @author : wh
 * @date : 2025/1/8 17:11
 * @description:
 */
@Repository
@RequiredArgsConstructor
public class TeacherDAOImpl extends ServiceImpl<TeacherMapper, TeacherDO> implements TeacherDAO {
    
    private final TeacherMapper teacherMapper;
    
    @Override
    public NebulaPageRes<TeacherVO> list(TeacherDTO dto) {
        Page<TeacherDO> page = PageHelperUtils.startPage(dto);
        List<TeacherDO> dos = teacherMapper.selectList(null);
        List<TeacherVO> teacherVOS = TeacherConverter.toTeacherVOs(dos);
        return PageHelperUtils.of(teacherVOS, page);
    }
}
