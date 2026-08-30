# MyBatis-Plus `spring-boot-nebula-mybatis`

A MyBatis-Plus wrapper providing base entities, auto-filled audit fields, type handlers, and pagination utilities.

## Add the Dependency

```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-mybatis</artifactId>
    <version>3.0.6</version>
</dependency>
```

## Main Capabilities

| Class | Description |
|-------|-------------|
| `BaseDO` | Base entity (`id`, `createTime`, `updateTime`) |
| `NebulaMetaObjectHandler` | Auto-fills audit fields on insert/update |
| `ArrayTypeHandler` / `ListTypeHandler` | Array and list type handlers |
| `PageHelperUtils` | Pagination utility pairing with `NebulaPageQuery` |

## Pagination Example

```java
Page<StudentDO> page = PageHelperUtils.startPage(dto);
List<StudentVO> list = ...;
return PageHelperUtils.of(list, page);
```

Sample module: `spring-boot-nebula-mybatis-sample`.
