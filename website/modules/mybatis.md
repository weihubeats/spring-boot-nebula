# MyBatis-Plus `spring-boot-nebula-mybatis`

MyBatis-Plus 封装，提供基础实体、审计字段自动填充、类型处理器与分页工具。

## 引入依赖

```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-mybatis</artifactId>
    <version>3.0.3</version>
</dependency>
```

## 主要能力

| 类 | 说明 |
|----|------|
| `BaseDO` | 基础实体（`id`、`createTime`、`updateTime`） |
| `NebulaMetaObjectHandler` | 插入/更新时自动填充审计字段 |
| `ArrayTypeHandler` / `ListTypeHandler` | 数组、列表类型处理器 |
| `PageHelperUtils` | 结合 `NebulaPageQuery` 的分页工具 |

## 分页示例

```java
Page<StudentDO> page = PageHelperUtils.startPage(dto);
List<StudentVO> list = ...;
return PageHelperUtils.of(list, page);
```

示例模块：`spring-boot-nebula-mybatis-sample`。