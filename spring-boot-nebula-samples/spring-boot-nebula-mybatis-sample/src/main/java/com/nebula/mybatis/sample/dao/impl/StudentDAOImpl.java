package com.nebula.mybatis.sample.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.nebula.base.model.NebulaPageRes;
import com.nebula.mybatis.sample.converter.StudentConverter;
import com.nebula.mybatis.sample.dao.StudentDAO;
import com.nebula.mybatis.sample.dao.mapper.StudentMapper;
import com.nebula.mybatis.sample.dao.entity.StudentDO;
import com.nebula.mybatis.sample.dto.StudentDTO;
import com.nebula.mybatis.sample.vo.StudentVO;
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
