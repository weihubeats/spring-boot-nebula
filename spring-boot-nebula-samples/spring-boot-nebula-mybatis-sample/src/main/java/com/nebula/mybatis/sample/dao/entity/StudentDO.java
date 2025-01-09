package com.nebula.mybatis.sample.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author : wh
 * @date : 2025/1/8 16:40
 * @description:
 */
@Data
public class StudentDO {

    @TableId(type = IdType.AUTO)    private Long id;
    
    private String name;
    
    
    private Integer age;
    
}
