---
layout: home
title: Nebula

hero:
  name: Nebula
  text: Spring Boot 3 企业级组件库
  tagline: 统一响应封装 · 异常飞书告警 · 分布式锁 · 读写分离 · Excel · 区域路由 JOIN
  image:
    src: /nebula-logo.svg
    alt: Nebula
  actions:
    - theme: brand
      text: 快速开始
      link: /guide/quick-start
    - theme: alt
      text: 查看源码
      link: https://github.com/weihubeats/spring-boot-nebula

features:
  - icon: 📦
    title: 统一依赖管理
    details: BOM 锁定 Spring Boot / MyBatis-Plus / Redisson 等版本，子模块无需手写版本号。
  - icon: ⚡
    title: Web 响应封装
    details: '@NebulaResponseBody 自动统一 JSON 结构，全局异常处理 + 飞书 Webhook 告警。'
  - icon: 🔒
    title: 分布式锁
    details: '@NebulaDistributedLock 基于 Redisson，声明式加锁，支持 SpEL 锁名与公平锁。'
  - icon: 🗃️
    title: MyBatis-Plus 封装
    details: BaseDO、审计字段自动填充、类型处理器、与 NebulaPageQuery 配套的分页工具。
  - icon: 🔀
    title: 读写分离
    details: '@NebulaRead / @NebulaWrite / @NebulaDS 动态数据源路由，一套配置两种需求。'
  - icon: 📊
    title: 区域路由 JOIN
    details: '@AutoJoin 自动拼接区域路由表，多区域部署零侵入。'
---

## 一条注解 = 开箱即用

```java
@GetMapping("/test")
@NebulaResponseBody
public String test() {
    return "小奏";
}
```

无需手动包装 `Response`，框架自动输出统一 JSON 结构。

```json
{
  "code": 200,
  "data": "小奏",
  "msg": "success"
}
```

## 模块速览

| 模块 | ArtifactId | 说明 |
|------|------------|------|
| [项目脚手架](/modules/archetype) | `spring-boot-nebula-archetype` | 一键生成多模块 DDD 项目骨架 |
| [BOM 依赖](/modules/dependencies) | `spring-boot-nebula-dependencies` | 统一依赖版本管理 |
| [Web 封装](/modules/web) | `spring-boot-nebula-web` | 统一响应、异常处理、飞书告警 |
| [国际化](/modules/i18n) | `spring-boot-nebula-i18n` | 多语言响应、远程文案加载 |
| [日志与告警](/modules/log) | `spring-boot-nebula-logback` | 日志脱敏、ERROR 飞书报警 |
| [MyBatis-Plus](/modules/mybatis) | `spring-boot-nebula-mybatis` | 基础实体、审计填充、分页 |
| [动态数据源](/modules/dynamic-datasource) | `spring-boot-nebula-dynamic-datasource` | 读写分离 |
| [分布式锁](/modules/distribute-lock) | `spring-boot-nebula-distribute-lock` | Redisson 声明式锁 |
| [Excel](/modules/excel) | `spring-boot-nebula-excel` | FastExcel 导入导出 |
| [区域路由 JOIN](/modules/join) | `spring-boot-nebula-join` | 自动拼接路由表 JOIN |
| [Feign 自动解包](/modules/feign) | `spring-boot-nebula-feign` | 自动解包 NebulaResponse |
| [聚合根 DDD](/modules/aggregate) | `spring-boot-nebula-aggregate` | 聚合变更追踪 |
| [一键聚合](/modules/all) | `spring-boot-nebula-all` | 常用模块一站式引入 |

## 技术栈

| 项目 | 版本 |
|------|------|
| Java | 17+ |
| Spring Boot | 3.4.x |
| 当前版本 | `3.0.6` |
| 许可证 | Apache 2.0 |

<footer class="vp-flex" style="margin-top:40px">
  无侵入、纯注解使用。加入 [GitHub](https://github.com/weihubeats/spring-boot-nebula) 了解全部用法。
</footer>