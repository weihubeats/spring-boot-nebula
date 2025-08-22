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
