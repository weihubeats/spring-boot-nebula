package com.nebula.join.sample.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_merchant")
public class MerchantDO {

    private Long id;

    private String merchantCode;

    private String merchantName;
}