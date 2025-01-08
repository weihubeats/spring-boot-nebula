package com.nebula.mybatis.sample.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.mybatis.sample.dao.entity.StudentDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author : wh
 * @date : 2025/1/8 16:43
 * @description:
 */
@Mapper
public interface StudentMapper extends BaseMapper<StudentDO> {
}
