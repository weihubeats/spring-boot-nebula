package ${package}.interfaces.controller;

import com.nebula.base.pagination.NebulaPageRes;
import ${package}.application.dto.SysUserDTO;
import ${package}.application.service.SysUserAppService;
import ${package}.domain.model.SysUser;
import ${package}.interfaces.vo.SysUserVO;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 接口层：协议转换（领域对象 -> VO），不写业务逻辑。响应由 nebula-web 统一包装
 *
 * @author generator
 */
@RestController
@RequestMapping("/sys-users")
@RequiredArgsConstructor
public class SysUserController {
    
    private final SysUserAppService sysUserAppService;
    
    @PostMapping
    public Long save(@Valid @RequestBody SysUserDTO.Save dto) {
        return sysUserAppService.save(dto);
    }
    
    @GetMapping("/{id}")
    public SysUserVO findById(@PathVariable Long id) {
        return sysUserAppService.findById(id)
                .map(this::toVO)
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + id));
    }
    
    @GetMapping("/page")
    public NebulaPageRes<SysUserVO> page(SysUserDTO.PageQuery dto) {
        NebulaPageRes<SysUser> page = sysUserAppService.page(dto);
        List<SysUserVO> voList = page.list().stream().map(this::toVO).toList();
        return new NebulaPageRes<>(voList, page.totalCount(), page.pageSize(), page.pageIndex());
    }
    
    private SysUserVO toVO(SysUser user) {
        SysUserVO vo = new SysUserVO();
        vo.setId(user.getId());
        vo.setName(user.getName());
        vo.setAge(user.getAge());
        return vo;
    }
}
