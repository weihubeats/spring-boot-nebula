package com.nebula.join.sample.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.join.annotation.AutoJoin;
import com.nebula.join.sample.entity.OrderDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper extends BaseMapper<OrderDO> {

    @AutoJoin(mainColumn = "creating_uid")
    @Select("SELECT * FROM t_order")
    List<OrderDO> selectAllOrders();

    @Select("SELECT * FROM t_order")
    List<OrderDO> selectOrdersDynamic();
}
