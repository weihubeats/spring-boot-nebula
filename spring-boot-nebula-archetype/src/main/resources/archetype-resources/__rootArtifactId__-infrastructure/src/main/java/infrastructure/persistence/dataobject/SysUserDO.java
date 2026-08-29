package ${package}.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 持久化对象：只映射表结构，禁止泄漏到领域层
 *
 * @author generator
 */
@Data
@TableName("sys_user")
public class SysUserDO {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private Integer age;
    
    private LocalDateTime createdAt;
}
