# 动态数据源 `spring-boot-nebula-dynamic-datasource`

基于 `DynamicRoutingDataSource` 的读写分离，通过注解切换数据源。

## 引入依赖

```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-dynamic-datasource</artifactId>
    <version>3.0.6</version>
</dependency>
```

## 注解

| 注解 | 说明 |
|------|------|
| `@NebulaRead` | 路由到读库 |
| `@NebulaWrite` | 路由到写库 |
| `@NebulaDS("dsName")` | 路由到指定数据源 |

```java
@NebulaWrite
public void saveTeacher(TeacherDTO dto) { ... }

@NebulaRead
public NebulaPageRes<TeacherVO> list(TeacherDTO dto) { ... }
```

## 配置示例

分别配置读写数据源，再注册到 `DynamicRoutingDataSource`：

```yaml
db:
  nebula:
    pg:
      write:
        driverClassName: org.postgresql.Driver
        url: jdbc:postgresql://localhost:5432/app_write
        username: user
        password: pass
      read:
        driverClassName: org.postgresql.Driver
        url: jdbc:postgresql://localhost:5432/app_read
        username: user
        password: pass
```

完整配置参考 `spring-boot-nebula-dynamic-datasource-sample` 中的 `MybatisPlusConfig`。