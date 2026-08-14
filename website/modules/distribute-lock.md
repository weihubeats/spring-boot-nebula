# 分布式锁 `spring-boot-nebula-distribute-lock`

基于 Redisson 的声明式分布式锁。

## 引入依赖

```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-distribute-lock</artifactId>
    <version>3.0.3</version>
</dependency>
```

## 前置条件

需自行配置 `RedissonClient` Bean（参考示例中的 `RedissonConfig`）。

## 使用方式

```java
@NebulaDistributedLock(lockNamePre = "order:updateOrder:", lockNamePost = "#dto.orderId")
public void updateOrder(OrderDTO dto) { ... }
```

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `lockNamePre` | `""` | 锁名前缀 |
| `lockNamePost` | `""` | 锁名后缀，支持 SpEL |
| `tryLock` | `false` | 是否尝试加锁 |
| `tryWaitTime` | `30` | 尝试等待时间 |
| `outTime` | `20` | 锁超时自动释放时间 |
| `timeUnit` | `SECONDS` | 时间单位 |
| `fairLock` | `false` | 是否公平锁 |

示例模块：`spring-boot-nebula-distribute-lock-sample`。