package ${package}.domain.gateway;

import com.nebula.base.pagination.NebulaPageQuery;
import com.nebula.base.pagination.NebulaPageRes;
import ${package}.domain.model.SysUser;
import java.util.Optional;

/**
 * 网关接口：领域层定义、基础设施层实现（依赖倒置）
 *
 * @author generator
 */
public interface SysUserGateway {
    
    Long save(SysUser user);
    
    Optional<SysUser> findById(Long id);
    
    NebulaPageRes<SysUser> page(NebulaPageQuery pageQuery, String name);
}
