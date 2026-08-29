package ${package}.api.dto;

import java.io.Serializable;

/**
 * 对外 DTO：只做数据传输，禁止携带行为；字段变更视为对外契约变更
 *
 * @author generator
 */
public class SysUserRespDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    
    private String name;
    
    private Integer age;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
    }
}
