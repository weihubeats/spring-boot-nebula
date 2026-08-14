# 区域路由 JOIN `spring-boot-nebula-join`

多区域场景下，自动为 Mapper 查询拼接区域路由 JOIN 条件。

## 引入依赖

```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-join</artifactId>
    <version>3.0.3</version>
</dependency>
```

## 配置

```yaml
region-route:
  enabled: true
  join-table: csa_user_route
  region-column-name: csa_region_id
  join-column: uid
  main-column: uid
  header-name: X-REGION
```

## 使用方式

在 Mapper 方法上标注 `@AutoJoin`，框架根据请求头 `X-REGION` 自动拼接 JOIN：

> **安全提醒**：`X-REGION` 不是鉴权边界。`RegionWebInterceptor` 接受任意合法数值，优先使用请求头，回退到 `RegionProvider`。网关必须剥离并重写该头，或从已认证身份派生区域值。

```java
@AutoJoin
List<UserDO> selectUsers();

@AutoJoin(mainColumn = "creating_uid")
List<OrderDO> selectOrders();

@AutoJoin(mainColumn = "merchant_code", joinTable = "csa_merchant_route", joinColumn = "m_id")
List<MerchantDO> selectMerchants();
```

示例模块：`spring-boot-nebula-join-sample`。