# 最佳实践

1. **优先使用 BOM**：在父 POM import `spring-boot-nebula-dependencies`，子模块不再手写版本号，避免跨项目依赖版本漂移（如 Redisson 行为差异）。

2. **按需引入**：不需要的能力不要引 `spring-boot-nebula-all`。按模块拆分依赖更清晰，启动更快、冲突更少。

3. **参考示例**：每个能力在 `spring-boot-nebula-samples` 下都有对应可运行的示例模块，作为接入蓝本。

4. **DDD 实践**：[ddd-example](https://github.com/weihubeats/ddd-example) 展示了聚合根与 Nebula 组件的完整配合。

## 相关链接

- GitHub：[weihubeats/spring-boot-nebula](https://github.com/weihubeats/spring-boot-nebula)
- DeepWiki：[deepwiki-spring-boot-nebula](https://deepwiki.com/weihubeats/spring-boot-nebula)
- 变更记录：[CHANGELOG.md](https://github.com/weihubeats/spring-boot-nebula/blob/main/CHANGELOG.md)