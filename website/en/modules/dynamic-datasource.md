# Dynamic Datasource `spring-boot-nebula-dynamic-datasource`

Read/write splitting built on `DynamicRoutingDataSource`, switching datasources via annotations.

## Add the Dependency

```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-dynamic-datasource</artifactId>
    <version>3.0.6</version>
</dependency>
```

## Annotations

| Annotation | Description |
|------------|-------------|
| `@NebulaRead` | Route to the read datasource |
| `@NebulaWrite` | Route to the write datasource |
| `@NebulaDS("dsName")` | Route to a named datasource |

```java
@NebulaWrite
public void saveTeacher(TeacherDTO dto) { ... }

@NebulaRead
public NebulaPageRes<TeacherVO> list(TeacherDTO dto) { ... }
```

## Configuration Example

Configure the read and write datasources separately, then register them in `DynamicRoutingDataSource`:

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

For the full configuration, see `MybatisPlusConfig` in `spring-boot-nebula-dynamic-datasource-sample`.
