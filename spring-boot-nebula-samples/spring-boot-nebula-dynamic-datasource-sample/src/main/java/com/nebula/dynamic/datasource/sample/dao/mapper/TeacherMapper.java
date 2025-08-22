package com.nebula.dynamic.datasource.sample.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.dynamic.datasource.sample.dao.entity.TeacherDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author : wh
 * @date : 2025/8/21
 * @description:
 */
@Mapper
public interface TeacherMapper extends BaseMapper<TeacherDO> {
}
