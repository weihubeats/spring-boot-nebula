package com.nebula.dynamic.datasource.sample.vo;

import java.util.List;
import lombok.Data;

/**
 * @author : wh
 * @date : 2025/8/21
 * @description:
 */
@Data
public class TeacherVO {

    private Long id;

    private String name;

    private Integer age;

    private String[] tags;
    
    private List<String> tags1;

}
