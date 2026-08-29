package ${package}.application.dto;

import com.nebula.base.pagination.NebulaPageQuery;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author generator
 */
public class SysUserDTO {
    
    private SysUserDTO() {
    }
    
    @Data
    public static class Save {
        
        @NotBlank(message = "name must not be blank")
        private String name;
        
        private Integer age;
    }
    
    /**
     * 分页查询参数：组合 NebulaPageQuery，业务 DTO 不再继承分页基类
     */
    @Data
    public static class PageQuery {
        
        private String name;
        
        private NebulaPageQuery page = new NebulaPageQuery();
    }
}
