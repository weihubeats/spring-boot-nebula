package ${package}.api;

import ${package}.api.dto.SysUserRespDTO;

/**
 * 对外服务契约示例：供其他系统通过 Dubbo（@DubboService）或 Feign 暴露与调用。
 * <p>
 * 约定：
 * <ul>
 *   <li>本模块只放接口定义 + 出入参 DTO，保持零框架依赖，外部系统引用时不被拖入任何框架</li>
 *   <li>实现类放 start 模块 provider 包，通过组合 application 服务完成委托</li>
 *   <li>RPC 方法返回 null 表示资源不存在，避免 Optional 的序列化兼容问题</li>
 * </ul>
 *
 * @author generator
 */
public interface SysUserApi {
    
    /**
     * 按 ID 查询用户，不存在返回 null
     */
    SysUserRespDTO getById(Long id);
}
