package com.nebula.mybatis.entity;

import java.time.LocalDateTime;

/**
 * @author : wh
 * @date : 2024/3/11 13:06
 * @description:
 */
public class BaseDO {
    
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
