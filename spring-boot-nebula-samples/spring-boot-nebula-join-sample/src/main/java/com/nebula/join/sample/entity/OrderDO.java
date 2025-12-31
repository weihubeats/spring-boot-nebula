package com.nebula.join.sample.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_order")
public class OrderDO {

    private Long id;
    private String orderNo;
    private String creatingUid;
}
