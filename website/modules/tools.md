# 通用工具

## spring-boot-nebula-common

基础工具与分页模型，如 [NebulaPageQuery](https://github.com/weihubeats/spring-boot-nebula/tree/main/spring-boot-nebula-common/src/main/java/com/nebula/base/model)。

## spring-boot-nebula-web-common

Web 层基础工具，被 `spring-boot-nebula-web` 间接依赖，也可单独使用：

| 工具类 | 说明 |
|--------|------|
| `SpringBeanUtils` | 从容器获取 Spring Bean |
| `NebulaSysWebUtils` | 获取 Spring 环境信息 |
| `ExpressionUtil` | SpEL 表达式解析 |

```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-web-common</artifactId>
</dependency>
```