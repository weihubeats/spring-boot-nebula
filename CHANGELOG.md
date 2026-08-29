# CHANGELOG

## 3.0.6

- **新增 spring-boot-nebula-archetype 项目脚手架**：基于 Maven Archetype 一键生成多模块 DDD 项目骨架
- **新增 spring-boot-nebula-i18n 国际化模块**：多语言响应、远程文案加载
- **excel**：增强导入校验能力，新增行级校验（`RowValidator`）与导入结果模型（`ImportResult`）
- **common**：分页模型迁移至 `com.nebula.base.pagination` 包，清理 model 包遗留
- **依赖治理**：pagehelper 依赖收敛至 BOM 统一管理；mybatis-spring 升级至 3.0.6
- **修复**：
  - join：修复区域改写 scope 泄漏与不可改写 SQL 静默放行
  - distribute-lock：修复 SpEL 锁名缓存错乱与自动配置启动失败
  - web/web-common/alert/feign：并发安全、请求体保护与日志脱敏
  - common：资源泄漏、敏感日志与工具类缺陷修复
  - 脱敏规则：`SecretKeyDesensitizeRule` 支持 JSON 格式脱敏

## 3.0.5

- 优化 Maven Central 发布流程：deploy 脚本轮询发布状态、`waitUntil=UPLOADED`、integration-test 阶段自动退出发布
- 日志模块（spring-boot-nebula-log）完善

## 3.0.4

新增多数据源支持
