package ${package}.infrastructure.gateway;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.Page;
import com.nebula.base.pagination.NebulaPageQuery;
import com.nebula.base.pagination.NebulaPageRes;
import com.nebula.mybatis.utils.PageHelperUtils;
import ${package}.domain.gateway.SysUserGateway;
import ${package}.domain.model.SysUser;
import ${package}.infrastructure.persistence.dataobject.SysUserDO;
import ${package}.infrastructure.persistence.mapper.SysUserMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 网关实现：领域模型与持久化对象的转换边界
 *
 * @author generator
 */
@Repository
@RequiredArgsConstructor
public class SysUserGatewayImpl implements SysUserGateway {
    
    private final SysUserMapper sysUserMapper;
    
    @Override
    public Long save(SysUser user) {
        SysUserDO record = new SysUserDO();
        record.setName(user.getName());
        record.setAge(user.getAge());
        record.setCreatedAt(LocalDateTime.now());
        sysUserMapper.insert(record);
        return record.getId();
    }
    
    @Override
    public Optional<SysUser> findById(Long id) {
        return Optional.ofNullable(sysUserMapper.selectById(id)).map(this::toDomain);
    }
    
    @Override
    public NebulaPageRes<SysUser> page(NebulaPageQuery pageQuery, String name) {
        Page<SysUserDO> page = PageHelperUtils.startPage(pageQuery);
        LambdaQueryWrapper<SysUserDO> wrapper = new LambdaQueryWrapper<SysUserDO>()
                .like(name != null && !name.isBlank(), SysUserDO::getName, name)
                .orderByDesc(SysUserDO::getId);
        List<SysUserDO> records = sysUserMapper.selectList(wrapper);
        List<SysUser> users = records.stream().map(this::toDomain).toList();
        return new NebulaPageRes<>(users, page.getTotal(), page.getPageSize(), page.getPageNum());
    }
    
    private SysUser toDomain(SysUserDO record) {
        return SysUser.restore(record.getId(), record.getName(), record.getAge());
    }
}
