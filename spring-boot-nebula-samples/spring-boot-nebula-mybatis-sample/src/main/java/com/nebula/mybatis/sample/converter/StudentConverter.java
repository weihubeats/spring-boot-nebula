package com.nebula.mybatis.sample.converter;

import com.nebula.mybatis.sample.dao.entity.StudentDO;
import com.nebula.mybatis.sample.vo.StudentVO;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : wh
 * @date : 2025/1/8 18:20
 * @description:
 */
public class StudentConverter {
    
    
    public static List<StudentVO> toStudentVOs(List<StudentDO> dos) {
        List<StudentVO> vos = dos.stream().map(StudentConverter::toStudentVO).collect(Collectors.toList());
        return vos;
    }

    public static StudentVO toStudentVO(StudentDO studentDO) {
        StudentVO studentVO = new StudentVO();
        studentVO.setId(studentDO.getId());
        studentVO.setName(studentDO.getName());
        studentVO.setAge(studentDO.getAge());
        return studentVO;
    }
    
}
