package com.nebula.mybatis.sample.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nebula.base.model.NebulaPageRes;
import com.nebula.mybatis.sample.dao.entity.StudentDO;
import com.nebula.mybatis.sample.dto.StudentDTO;
import com.nebula.mybatis.sample.vo.StudentVO;

/**
 * @author : wh
 * @date : 2025/1/8 17:10
 * @description:
 */
public interface StudentDAO extends IService<StudentDO> {
    
    
    NebulaPageRes<StudentVO> getStudents(StudentDTO dto);
}
