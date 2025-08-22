package com.nebula.dynamic.datasource.sample.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nebula.mybatis.handler.ArrayTypeHandler;
import com.nebula.mybatis.handler.ListTypeHandler;
import java.util.List;
import lombok.Data;

/**
 * @author : wh
 * @date : 2025/8/21
 * @description:
 */
@Data
@TableName("teacher")
public class TeacherDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Integer age;

    @TableField(typeHandler = ArrayTypeHandler.class)
    private String[] tags;
    @TableField(typeHandler = ListTypeHandler.class)
    private List<String> tags1;

}
