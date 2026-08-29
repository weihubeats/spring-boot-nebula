package ${package}.provider;

import ${package}.api.SysUserApi;
import ${package}.api.dto.SysUserRespDTO;
import ${package}.application.service.SysUserAppService;
import ${package}.domain.model.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 对外契约实现：组合应用服务完成委托。
 * <p>
 * 接入 Dubbo 时在类上加 @DubboService（org.apache.dubbo），接入 Feign 时
 * 为本类补一个 controller 或直接用 @FeignClient 指向内部接口。外部系统只需依赖 api 模块。
 *
 * @author generator
 */
@Component
@RequiredArgsConstructor
public class SysUserApiImpl implements SysUserApi {
    
    private final SysUserAppService sysUserAppService;
    
    @Override
    public SysUserRespDTO getById(Long id) {
        return sysUserAppService.findById(id)
                .map(this::toResp)
                .orElse(null);
    }
    
    private SysUserRespDTO toResp(SysUser user) {
        SysUserRespDTO dto = new SysUserRespDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setAge(user.getAge());
        return dto;
    }
}
