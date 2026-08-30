---
layout: home
title: Nebula

hero:
  name: Nebula
  text: Enterprise-Grade Component Library for Spring Boot 3
  tagline: Unified Response Wrapper · Feishu Exception Alerting · Distributed Lock · Read/Write Splitting · Excel · Region-Routing JOIN
  image:
    src: /nebula-logo.svg
    alt: Nebula
  actions:
    - theme: brand
      text: Quick Start
      link: /en/guide/quick-start
    - theme: alt
      text: View Source
      link: https://github.com/weihubeats/spring-boot-nebula

features:
  - icon: 📦
    title: Unified Dependency Management
    details: The BOM locks versions of Spring Boot / MyBatis-Plus / Redisson and more — no more hand-written version numbers in submodules.
  - icon: ⚡
    title: Web Response Wrapper
    details: '@NebulaResponseBody unifies the JSON structure automatically, with global exception handling + Feishu Webhook alerting.'
  - icon: 🔒
    title: Distributed Lock
    details: '@NebulaDistributedLock is built on Redisson — declarative locking with SpEL lock names and fair-lock support.'
  - icon: 🗃️
    title: MyBatis-Plus Wrapper
    details: BaseDO, auto-filled audit fields, type handlers, and pagination utilities that pair with NebulaPageQuery.
  - icon: 🔀
    title: Read/Write Splitting
    details: '@NebulaRead / @NebulaWrite / @NebulaDS dynamic datasource routing — one configuration, two workloads.'
  - icon: 📊
    title: Region-Routing JOIN
    details: '@AutoJoin stitches region-routed tables together automatically — zero intrusion for multi-region deployments.'
---

## One Annotation = Out of the Box

```java
@GetMapping("/test")
@NebulaResponseBody
public String test() {
    return "小奏";
}
```

No manual `Response` wrapping needed — the framework automatically outputs a unified JSON structure.

```json
{
  "code": 200,
  "data": "小奏",
  "msg": "success"
}
```

## Modules at a Glance

| Module | ArtifactId | Description |
|--------|------------|-------------|
| [Project Archetype](/en/modules/archetype) | `spring-boot-nebula-archetype` | Generate a multi-module DDD project skeleton in one command |
| [BOM Dependencies](/en/modules/dependencies) | `spring-boot-nebula-dependencies` | Centralized dependency version management |
| [Web Wrapper](/en/modules/web) | `spring-boot-nebula-web` | Unified response, exception handling, Feishu alerting |
| [Internationalization](/en/modules/i18n) | `spring-boot-nebula-i18n` | Multi-language responses, remote message loading |
| [Logging & Alerting](/en/modules/log) | `spring-boot-nebula-logback` | Log masking, ERROR-level Feishu alerts |
| [MyBatis-Plus](/en/modules/mybatis) | `spring-boot-nebula-mybatis` | Base entities, audit filling, pagination |
| [Dynamic Datasource](/en/modules/dynamic-datasource) | `spring-boot-nebula-dynamic-datasource` | Read/write splitting |
| [Distributed Lock](/en/modules/distribute-lock) | `spring-boot-nebula-distribute-lock` | Declarative Redisson locks |
| [Excel](/en/modules/excel) | `spring-boot-nebula-excel` | FastExcel import/export |
| [Region-Routing JOIN](/en/modules/join) | `spring-boot-nebula-join` | Auto-assembled routing-table JOIN |
| [Feign Unwrapping](/en/modules/feign) | `spring-boot-nebula-feign` | Automatic NebulaResponse unwrapping |
| [Aggregate Root DDD](/en/modules/aggregate) | `spring-boot-nebula-aggregate` | Aggregate change tracking |
| [All-in-One](/en/modules/all) | `spring-boot-nebula-all` | All common modules in a single dependency |

## Tech Stack

| Item | Version |
|------|---------|
| Java | 17+ |
| Spring Boot | 3.4.x |
| Current Version | `3.0.6` |
| License | Apache 2.0 |

<footer class="vp-flex" style="margin-top:40px">
  Non-intrusive, annotation-driven. Join us on [GitHub](https://github.com/weihubeats/spring-boot-nebula) for the full documentation.
</footer>
