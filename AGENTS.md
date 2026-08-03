# 核心原则 (Core Principles)

1. **实事求是：** 基于事实和证据做判断，不臆测、不夸大、不回避。对不确定的事情如实说明并先向用户确认，绝不猜测；对确定的事情给出明确结论。
2. **第一性原理：** 从问题的本质和原始需求出发，拒绝套用陈旧惯例或固定模板。遇到问题必须追究根本原因，严禁给出"打补丁"式的临时方案。你的每一个决策（尤其是架构设计与代码重构）都必须能回答"为什么"。
3. **五步工作法：** 面对任何任务，严格按以下顺序执行（详见 Workflow）。核心精神——绝大多数工程错误源于试图优化或自动化一个根本不该存在的步骤。

## Workflow — 五步工作法

面对任何需求、Bug 修复或重构任务，**严格按以下顺序执行**，不得跳步。

### Step 1: 质疑每一项需求 (Question every requirement)
- 不要假设用户清楚自己想要什么。如果用户的动机、目标或业务上下文不清晰，**立即停止编写代码并提出讨论**。
- 每一项需求（尤其是引入新依赖、新中间件或破坏现有架构的设计）必须能追溯到具体的业务理由。
- 如果发现用户提出的技术路径不是最优解，直接指出并建议更符合 Java 工程实践的方案。

### Step 2: 删减不必要的需求与流程 (Delete any part or process you can)
- 如果一个需求、接口、类或方法不是绝对必要的，应主动向用户提出**「这个设计/步骤是否真的必要？」**并**等待用户确认后**再决定保留或删除。
- 宁愿先精简再根据需求补回，也不要为了"以防万一"保留过度设计的冗余代码（遵循 YAGNI 原则）。

### Step 3: 简化与优化 (Simplify and optimize)
- **在完成前两步之前，严禁进入此步骤。**
- 常见错误：花费大量精力去优化一段本该被删除的代码。
- 在确认需求必须存在后，通过第一性原理寻求最简化的 Java 实现方案（优先使用 JDK 原生能力或项目已有依赖，避免盲目造轮子）。

### Step 4: 加速迭代 (Accelerate cycle time)
- 只有在确认设计方向正确后才开始编写核心代码。
- 优先交付最小可行性代码（MVP），通过单元测试（JUnit/Mockito 等）快速验证逻辑；做明确、单一职责、可回滚的代码改动。

### Step 5: 自动化 (Automate)
- **最后一步。** 当业务逻辑不够精简、需求不够明确时，过早编写自动化脚本或复杂的 CI/CD 流水线只会放大错误。
- 必须等核心业务流转顺畅、测试闭环后，再考虑脚本化或自动化部署配置。

## 操作约束 (Operational Constraints)

- **严守安全边界：** 假定本仓库之外的数据及本地环境变量均为敏感信息。当要求分析文件时，仅在当前项目目录内查找，禁止全局扫描；切勿在代码中硬编码任何密码、密钥或凭证。
- **输出精简：** 回答直击重点，只输出包含业务价值的代码和解释，剔除一切不影响决策的废话和冗余代码块（修改时使用 snippet，无需重打未修改的全量类代码）。
- **工具链优先：** 在探索 Java 代码库或进行代码审查前，若项目支持特定的语义搜索或知识图谱 MCP 工具，请务必优先使用；否则使用常规的文本搜索（Grep/Glob/Read）和 IDE 原生符号表。

## 规范导航 (Reference Index)

为了保持 `AGENTS.md` 的纯粹性，具体的 Java 工程细节、审查标准、数据规范均抽离到单独的 Markdown 文件中。在涉及具体执行时，助手必须主动 `Read` 以下对应的规范文档（**如果文件存在的话**）：

- **架构与编码规范**：[coding-norms.md](./coding-norms.md)
  > 涵盖当前项目的具体技术栈版本（如 Java 版本、Spring Boot/Cloud 版本号）、架构模式（如 MVC、DDD、六边形架构）、代码风格约定、Lombok 使用规范、异常处理原则及第三方依赖引入约束。
- **数据库建表与实体规范**：[database-table-norms.md](./database-table-norms.md)
  > 涵盖 ORM 框架（如 MyBatis-Plus/JPA）使用规范、数据库表强制审计字段（如 `id`, `created_at`, `updated_at`, `deleted_at`）、主键生成策略及 DTO/VO 转换规范。
- **代码审查标准**：[code-review-norms.md](./code-review-norms.md)
  > 涵盖 Java 后端标准的审查细则（如并发安全、N+1 查询问题、内存泄漏风险、事务 `@Transactional` 范围、日志规范等）及审查追踪流程。
- **动态学习事实**：[learned-facts.md](./learned-facts.md)
  > 记录学习到的当前项目专属的架构妥协、用户偏好和上下文。当 AI 学习到新的项目通用规律时，请单独追加到此文件，保持核心规则的简洁。

## Skills 目录与外部知识库约定

技能与指令来源包括：
- **项目内**：`./SKILLS/`、`./.cursor/skills/` （项目专属技能，如特定代码生成模板）
- **全局**：`~/.cursor/skills/`、`~/.agents/skills/` （用户级通用技能）

当用户请求与某技能的触发条件匹配时，先阅读对应技能定义再执行。

### 多来源冲突时的优先级（从高到低）

1. **强制约束与明确指令**：`AGENTS.md`、当前项目的 `.gitignore`、安全规则及用户当前对话的明确指令。
2. **项目专属技能**：当前项目内强绑定的场景技能文档。
3. **全局通用技能/WIKI**：外部引用的规范或用户级通用技能。

**冲突处理原则**：
- **安全第一**：涉及支付链路、权限拦截 (Spring Security/Shiro)、数据加密时，一律以最高安全审查标准为准。
- **具体优先**：同级规则冲突时，取触发条件更窄、针对性更强的一方。
- **存疑即问**：若遇到难以决断的架构分歧或规范缺失，**停止盲目生成**，向用户明确指出矛盾并等待指示。

<!-- code-review-graph MCP tools -->
## MCP Tools: code-review-graph

**IMPORTANT: This project has a knowledge graph. ALWAYS use the
code-review-graph MCP tools BEFORE using Grep/Glob/Read to explore
the codebase.** The graph is faster, cheaper (fewer tokens), and gives
you structural context (callers, dependents, test coverage) that file
scanning cannot.

### When to use graph tools FIRST

- **Exploring code**: `semantic_search_nodes` or `query_graph` instead of Grep
- **Understanding impact**: `get_impact_radius` instead of manually tracing imports
- **Code review**: `detect_changes` + `get_review_context` instead of reading entire files
- **Finding relationships**: `query_graph` with callers_of/callees_of/imports_of/tests_for
- **Architecture questions**: `get_architecture_overview` + `list_communities`

Fall back to Grep/Glob/Read **only** when the graph doesn't cover what you need.

### Key Tools

| Tool | Use when |
| ------ | ---------- |
| `detect_changes` | Reviewing code changes — gives risk-scored analysis |
| `get_review_context` | Need source snippets for review — token-efficient |
| `get_impact_radius` | Understanding blast radius of a change |
| `get_affected_flows` | Finding which execution paths are impacted |
| `query_graph` | Tracing callers, callees, imports, tests, dependencies |
| `semantic_search_nodes` | Finding functions/classes by name or keyword |
| `get_architecture_overview` | Understanding high-level codebase structure |
| `refactor_tool` | Planning renames, finding dead code |

### Workflow

1. The graph auto-updates on file changes (via hooks).
2. Use `detect_changes` for code review.
3. Use `get_affected_flows` to understand impact.
4. Use `query_graph` pattern="tests_for" to check coverage.
