# Best Practices

1. **Prefer the BOM**: import `spring-boot-nebula-dependencies` in the parent POM so submodules never hand-write version numbers, avoiding dependency drift across projects (e.g. Redisson behavioral differences).

2. **Add modules on demand**: don't pull in `spring-boot-nebula-all` for capabilities you don't use. Per-module dependencies keep things clearer, with faster startup and fewer conflicts.

3. **Follow the samples**: every capability has a runnable sample module under `spring-boot-nebula-samples` — use them as blueprints for integration.

4. **DDD in practice**: [ddd-example](https://github.com/weihubeats/ddd-example) demonstrates a full integration of aggregate roots with Nebula components.

## Related Links

- GitHub: [weihubeats/spring-boot-nebula](https://github.com/weihubeats/spring-boot-nebula)
- DeepWiki: [deepwiki-spring-boot-nebula](https://deepwiki.com/weihubeats/spring-boot-nebula)
- Changelog: [CHANGELOG.md](https://github.com/weihubeats/spring-boot-nebula/blob/main/CHANGELOG.md)
