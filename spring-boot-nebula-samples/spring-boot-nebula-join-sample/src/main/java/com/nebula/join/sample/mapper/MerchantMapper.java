package com.nebula.join.sample.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.join.annotation.AutoJoin;
import com.nebula.join.sample.entity.MerchantDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MerchantMapper extends BaseMapper<MerchantDO> {

    // 场景3：全自定义 (merchant_code -> csa_merchant_route.m_id)
    @AutoJoin(
        mainColumn = "merchant_code",
        joinTable = "csa_merchant_route",
        joinColumn = "m_id"
    )
    @Select("SELECT * FROM t_merchant")
    List<MerchantDO> selectAllMerchants();
}
