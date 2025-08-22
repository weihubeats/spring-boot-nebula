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
 
package com.nebula.dynamic.datasource.sample.converter;

import com.nebula.dynamic.datasource.sample.dao.entity.TeacherDO;
import com.nebula.dynamic.datasource.sample.vo.TeacherVO;
import java.util.List;

/**
 * @author : wh
 * @date : 2025/8/21
 * @description:
 */
public class TeacherConverter {
    
    public static List<TeacherVO> toTeacherVOs(List<TeacherDO> dos) {
        return dos.stream().map(TeacherConverter::toTeacherVO).toList();
    }
    
    private static TeacherVO toTeacherVO(TeacherDO aDo) {
        TeacherVO teacherVO = new TeacherVO();
        teacherVO.setId(aDo.getId());
        teacherVO.setName(aDo.getName());
        teacherVO.setAge(aDo.getAge());
        teacherVO.setTags(aDo.getTags());
        teacherVO.setTags1(aDo.getTags1());
        return teacherVO;
    }
}
