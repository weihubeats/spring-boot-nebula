package com.nebula.join.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AutoJoin {

        /**
     * 主表的关联字段 (Main Table Join Column)
     * 例如：业务表中的 user_id, creating_uid
     * 默认值：uid
     */
    String mainColumn() default "uid";

    /**
     * 需要join的表名
     * 如果为空，则使用全局配置(RegionRouteProperties)中的默认表名
     */
    String joinTable() default "csa_user_route";

    String joinColumn() default "uid";
    
}
