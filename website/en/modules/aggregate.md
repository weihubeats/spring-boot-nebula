# Aggregate Root DDD `spring-boot-nebula-aggregate`

DDD aggregate root support providing change tracking (`AggregateDiff`) and old-object snapshots (`@CreateOldObj`).

## Add the Dependency

```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-aggregate</artifactId>
    <version>3.0.6</version>
</dependency>
```

Aggregate classes extend `AbstractAggregate<T>`, used together with `@AggregateCreate` and `@CreateOldObj`. For a complete real-world example, see [ddd-example](https://github.com/weihubeats/ddd-example).
