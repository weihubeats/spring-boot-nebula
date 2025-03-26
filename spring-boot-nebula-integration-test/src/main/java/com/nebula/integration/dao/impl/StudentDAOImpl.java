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
 
package com.nebula.integration.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.nebula.base.model.NebulaPageRes;
import com.nebula.integration.converter.StudentConverter;
import com.nebula.integration.dao.StudentDAO;
import com.nebula.integration.dao.entity.StudentDO;
import com.nebula.integration.dao.mapper.StudentMapper;
import com.nebula.integration.dto.StudentDTO;
import com.nebula.integration.vo.StudentVO;
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
public class StudentDAOImpl extends ServiceImpl<StudentMapper, StudentDO> implements StudentDAO {
    
    private final StudentMapper studentMapper;
    
    @Override
    public NebulaPageRes<StudentVO> getStudents(StudentDTO dto) {
        Page<StudentDO> page = PageHelperUtils.startPage(dto);
        List<StudentDO> dos = studentMapper.selectList(null);
        List<StudentVO> studentVO = StudentConverter.toStudentVOs(dos);
        return PageHelperUtils.of(studentVO, page);
    }
}
