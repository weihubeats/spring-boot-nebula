# 聚合根 DDD `spring-boot-nebula-aggregate`

DDD 聚合根支持，提供变更追踪（`AggregateDiff`）与旧对象快照（`@CreateOldObj`）。

## 引入依赖

```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-aggregate</artifactId>
    <version>3.0.6</version>
</dependency>
```

聚合类继承 `AbstractAggregate<T>`，配合 `@AggregateCreate`、`@CreateOldObj` 使用。完整实践可参考 [ddd-example](https://github.com/weihubeats/ddd-example)。