package com.nebula.mybatis.sample.service;

import com.nebula.base.model.NebulaPageRes;
import com.nebula.mybatis.sample.dto.StudentDTO;
import com.nebula.mybatis.sample.vo.StudentVO;

/**
 * @author : wh
 * @date : 2025/1/8 18:13
 * @description:
 */
public interface StudentService {

    NebulaPageRes<StudentVO> list(StudentDTO dto);
}
