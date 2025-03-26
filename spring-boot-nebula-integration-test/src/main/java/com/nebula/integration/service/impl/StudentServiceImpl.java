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
 
package com.nebula.integration.service.impl;

import com.nebula.base.model.NebulaPageRes;
import com.nebula.integration.dao.StudentDAO;
import com.nebula.integration.dto.StudentDTO;
import com.nebula.integration.service.StudentService;
import com.nebula.integration.vo.StudentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author : wh
 * @date : 2025/1/8
 * @description:
 */
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    
    private final StudentDAO studentDAO;
    
    @Override
    public NebulaPageRes<StudentVO> list(StudentDTO dto) {
        return studentDAO.getStudents(dto);
    }
}
