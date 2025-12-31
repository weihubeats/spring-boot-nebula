package com.nebula.join.sample.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.join.annotation.AutoJoin;
import com.nebula.join.sample.entity.UserDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<UserDO> {

    // 场景1：默认配置 (uid -> csa_user_route.uid)
    @AutoJoin
    @Select("SELECT * FROM t_user")
    List<UserDO> selectAllUsers();


    @Select("SELECT * FROM t_user")
    List<UserDO> selectAllUsersNoRegion();
}
