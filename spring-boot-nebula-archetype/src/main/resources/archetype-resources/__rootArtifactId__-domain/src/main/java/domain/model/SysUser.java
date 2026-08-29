package ${package}.domain.model;

import java.util.Optional;

/**
 * 领域模型：业务规则的内聚核心，不依赖任何框架与持久化细节
 *
 * @author generator
 */
public class SysUser {
    
    private Long id;
    
    private String name;
    
    private Integer age;
    
    private SysUser(Long id, String name, Integer age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }
    
    /**
     * 工厂方法：创建即校验，保证领域对象自始合法
     */
    public static SysUser create(String name, Integer age) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        Optional.ofNullable(age).filter(a -> a >= 0 && a <= 150)
                .orElseThrow(() -> new IllegalArgumentException("age must be in [0, 150]"));
        return new SysUser(null, name, age);
    }
    
    public static SysUser restore(Long id, String name, Integer age) {
        return new SysUser(id, name, age);
    }
    
    public void growOlder() {
        this.age = this.age + 1;
    }
    
    public Long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public Integer getAge() {
        return age;
    }
}
