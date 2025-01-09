package com.nebula.mybatis.sample.dto;

import com.nebula.base.model.NebulaPageQuery;
import lombok.Data;

/**
 * @author : wh
 * @date : 2025/1/8 18:17
 * @description:
 */
@Data
public class StudentDTO extends NebulaPageQuery {
    
    private Long id;
    
    private String name;
    
    private Integer age;
}
