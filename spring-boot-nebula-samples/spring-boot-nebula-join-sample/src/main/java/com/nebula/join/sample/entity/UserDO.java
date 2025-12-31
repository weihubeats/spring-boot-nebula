package com.nebula.join.sample.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_user")
public class UserDO {

    private Long id;

    private String uid;

    private String username;
}
