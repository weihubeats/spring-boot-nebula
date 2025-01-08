package com.nebula.mybatis.sample.service.impl;

import com.nebula.base.model.NebulaPageRes;
import com.nebula.mybatis.sample.dao.StudentDAO;
import com.nebula.mybatis.sample.dto.StudentDTO;
import com.nebula.mybatis.sample.service.StudentService;
import com.nebula.mybatis.sample.vo.StudentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author : wh
 * @date : 2025/1/8 18:13
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
