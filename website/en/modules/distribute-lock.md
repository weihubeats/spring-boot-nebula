# Distributed Lock `spring-boot-nebula-distribute-lock`

Declarative distributed locking built on Redisson.

## Add the Dependency

```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-distribute-lock</artifactId>
    <version>3.0.6</version>
</dependency>
```

## Prerequisite

A `RedissonClient` bean must be configured by yourself (see `RedissonConfig` in the sample).

## Usage

```java
@NebulaDistributedLock(lockNamePre = "order:updateOrder:", lockNamePost = "#dto.orderId")
public void updateOrder(OrderDTO dto) { ... }
```

| Attribute | Default | Description |
|-----------|---------|-------------|
| `lockNamePre` | `""` | Lock name prefix |
| `lockNamePost` | `""` | Lock name suffix, supports SpEL |
| `tryLock` | `false` | Whether to attempt locking |
| `tryWaitTime` | `30` | Attempt wait time |
| `outTime` | `20` | Auto-release timeout for the lock |
| `timeUnit` | `SECONDS` | Time unit |
| `fairLock` | `false` | Whether to use a fair lock |

Sample module: `spring-boot-nebula-distribute-lock-sample`.
