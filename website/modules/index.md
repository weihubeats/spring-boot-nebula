# 模块一览

| 模块 | ArtifactId | 说明 |
|------|------------|------|
| [项目脚手架](/modules/archetype) | `spring-boot-nebula-archetype` | 一键生成多模块 DDD 项目骨架 |
| [BOM 依赖](/modules/dependencies) | `spring-boot-nebula-dependencies` | 统一依赖版本管理 |
| [通用工具](/modules/tools) | `spring-boot-nebula-common` / `spring-boot-nebula-web-common` | 基础工具、分页模型、Bean 获取、EL 解析 |
| [Web 封装](/modules/web) | `spring-boot-nebula-web` | 统一响应、异常处理、告警 |
| [国际化](/modules/i18n) | `spring-boot-nebula-i18n` | 多语言响应、远程文案加载 |
| [日志与告警](/modules/log) | `spring-boot-nebula-logback` | 日志脱敏、ERROR 飞书报警 |
| [MyBatis-Plus](/modules/mybatis) | `spring-boot-nebula-mybatis` | 基础实体、审计填充、分页 |
| [动态数据源](/modules/dynamic-datasource) | `spring-boot-nebula-dynamic-datasource` | 读写分离 |
| [分布式锁](/modules/distribute-lock) | `spring-boot-nebula-distribute-lock` | Redisson 声明式锁 |
| [Excel](/modules/excel) | `spring-boot-nebula-excel` | FastExcel 导出 |
| [区域路由 JOIN](/modules/join) | `spring-boot-nebula-join` | 自动拼接路由表 JOIN |
| [Feign](/modules/feign) | `spring-boot-nebula-feign` | 自动解包 NebulaResponse |
| [聚合根 DDD](/modules/aggregate) | `spring-boot-nebula-aggregate` | 聚合变更追踪 |
| [一键聚合](/modules/all) | `spring-boot-nebula-all` | 常用模块一站式引入 |