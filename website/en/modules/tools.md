# Common Utilities

## spring-boot-nebula-common

Basic utilities and pagination models, e.g. [NebulaPageQuery](https://github.com/weihubeats/spring-boot-nebula/tree/main/spring-boot-nebula-common/src/main/java/com/nebula/base/pagination).

## spring-boot-nebula-web-common

Web-layer basic utilities, pulled in transitively by `spring-boot-nebula-web` but also usable standalone:

| Utility | Description |
|---------|-------------|
| `SpringBeanUtils` | Fetch Spring beans from the container |
| `NebulaSysWebUtils` | Access Spring environment information |
| `ExpressionUtil` | SpEL expression parsing |

```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-web-common</artifactId>
</dependency>
```
