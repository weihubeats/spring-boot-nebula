# Region-Routing JOIN `spring-boot-nebula-join`

For multi-region deployments, automatically appends region-routing JOIN conditions to Mapper queries.

## Add the Dependency

```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-join</artifactId>
    <version>3.0.6</version>
</dependency>
```

## Configuration

```yaml
region-route:
  enabled: true
  join-table: csa_user_route
  region-column-name: csa_region_id
  join-column: uid
  main-column: uid
  header-name: X-REGION
```

## Usage

Annotate Mapper methods with `@AutoJoin`, and the framework assembles the JOIN automatically based on the `X-REGION` request header:

> **Security note**: `X-REGION` is not an authentication boundary. `RegionWebInterceptor` accepts any valid numeric value, preferring the request header and falling back to `RegionProvider`. Your gateway must strip and rewrite this header, or derive the region value from an authenticated identity.

```java
@AutoJoin
List<UserDO> selectUsers();

@AutoJoin(mainColumn = "creating_uid")
List<OrderDO> selectOrders();

@AutoJoin(mainColumn = "merchant_code", joinTable = "csa_merchant_route", joinColumn = "m_id")
List<MerchantDO> selectMerchants();
```

Sample module: `spring-boot-nebula-join-sample`.
