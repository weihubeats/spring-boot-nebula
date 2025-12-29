package com.nebula.join.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteContextConfig {

    private boolean enabled;

    /**
     * 主表关联字段
     */
    private String mainColumn;

    /**
     * 关联表名字
     */
    private String joinTable;

    private String joinColumn;


}

