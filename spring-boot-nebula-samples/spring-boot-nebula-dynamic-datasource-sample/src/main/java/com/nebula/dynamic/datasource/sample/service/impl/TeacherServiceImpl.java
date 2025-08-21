package com.nebula.dynamic.datasource.sample.service.impl;

import com.nebula.base.model.NebulaPageRes;
import com.nebula.dynamic.datasource.sample.dao.TeacherDAO;
import com.nebula.dynamic.datasource.sample.dto.TeacherDTO;
import com.nebula.dynamic.datasource.sample.service.TeacherService;
import com.nebula.dynamic.datasource.sample.vo.TeacherVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author : wh
 * @date : 2025/8/21
 * @description:
 */
@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final TeacherDAO teacherDAO;
    
    @Override
    public NebulaPageRes<TeacherVO> list(TeacherDTO dto) {
        return teacherDAO.list(dto);
    }
}
