package ${package}.application.service;

import com.nebula.base.pagination.NebulaPageQuery;
import com.nebula.base.pagination.NebulaPageRes;
import ${package}.application.dto.SysUserDTO;
import ${package}.domain.gateway.SysUserGateway;
import ${package}.domain.model.SysUser;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 应用服务：用例编排，不含业务规则（业务规则在 domain）
 *
 * @author generator
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserAppService {
    
    private final SysUserGateway sysUserGateway;
    
    @Transactional
    public Long save(SysUserDTO.Save dto) {
        Long id = sysUserGateway.save(SysUser.create(dto.getName(), dto.getAge()));
        log.info("user saved, id={}, name={}", id, dto.getName());
        return id;
    }
    
    public Optional<SysUser> findById(Long id) {
        return sysUserGateway.findById(id);
    }
    
    public NebulaPageRes<SysUser> page(SysUserDTO.PageQuery dto) {
        return sysUserGateway.page(dto.getPage(), dto.getName());
    }
}
