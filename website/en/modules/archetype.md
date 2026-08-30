# Project Archetype `spring-boot-nebula-archetype`

A Maven Archetype scaffold that generates a **multi-module lightweight DDD** project skeleton based on Nebula with a single command — shipping with unified response, global exception handling, parameter validation, and pagination out of the box.

## One-Command Generation

**Run the command in the parent directory where you want the project placed** (any empty directory works, e.g. `~/projects`); the generator creates a new folder named after `-DartifactId` in the current directory:

```bash
cd ~/projects                 # The project will be generated at ~/projects/demo
mvn archetype:generate \
    -DarchetypeGroupId=io.github.weihubeats \
    -DarchetypeArtifactId=spring-boot-nebula-archetype \
    -DarchetypeVersion=3.0.6 \
    -DgroupId=com.example \
    -DartifactId=demo \
    -Dversion=1.0.0-SNAPSHOT \
    -Dpackage=com.example.demo \
    -DinteractiveMode=false
```

After generation the project lives in `./demo/`, i.e. `demo/pom.xml`, `demo/demo-start/...`. Note: the target directory must not already exist, otherwise generation is refused with `already exists`.

| Parameter | Description | Default |
|-----------|-------------|---------|
| `groupId` | Project groupId | `com.example` |
| `artifactId` | Project name, also used as the prefix for all submodules | Required |
| `version` | Project version | `1.0.0-SNAPSHOT` |
| `package` | Base package name | `com.example` |

## Generated Project Structure

Using `demo` as an example:

```
demo/
├── pom.xml                    # Parent pom: unified dependency management (BOM import)
├── demo-start                 # Startup module: Application, controller/vo, RPC providers, config files
├── demo-application           # Application layer: use-case orchestration (AppService), DTOs
├── demo-domain                # Domain layer: domain models (business rules), gateway interfaces
├── demo-infrastructure        # Infrastructure layer: gateway implementations, mapper/dataobject
├── demo-api                   # External contracts: Dubbo/Feign interface definitions + request/response DTOs, for other systems
├── demo-common                # Project-internal shared code: utilities, constants, no business semantics
├── Dockerfile                 # Multi-stage build, produces a runnable image
├── deploy/k8s/                # K8s deployment manifests (deployment + service)
└── .github/workflows/ci.yaml  # GitHub Actions: mvn verify
```

Dependency direction is enforced by the compiler:

```text
start ──▶ application ──▶ domain ◀── infrastructure
```

- `start` is the only executable jar (spring-boot-maven-plugin repackage)
- `domain` depends only on `nebula-common` (pagination model) — zero framework intrusion
- `infrastructure` implements the `domain/gateway` interfaces — dependency inversion
- VO conversion is confined to the interface layer in start; application returns domain objects

## Layer Responsibility Conventions

| Layer | Contains | Must not contain |
|-------|----------|------------------|
| interfaces (start) | controllers, VOs, protocol conversion, global config | business logic |
| application | use-case orchestration, transaction boundaries, DTOs | business rules |
| domain | entity factories/behaviors, business validation, gateway interfaces | framework dependencies, SQL |
| infrastructure | gateway implementations, MyBatis-Plus mappers, DOs | business rules |

The standard flow for adding a use case: `controller method → AppService method → gateway interface → gatewayImpl`.

### External RPC (api module)

- Interfaces and request/response DTOs are defined in `demo-api`, **deliberately framework-free** — external systems depend on this single jar without dragging in any framework
- Implementations go in start's `provider` package (see `SysUserApiImpl`): add `@DubboService` for Dubbo, or `@FeignClient` for Feign
- Changes to external DTO fields are contract changes; error codes exposed externally also belong in the api module
- Shared utilities and constants go in `demo-common` (project-internal only, not published)

## Built-in Capabilities

| Capability | Source | Description |
|------------|--------|-------------|
| Unified response wrapping | nebula-web | Controllers return raw types, wrapped into `NebulaResponse` automatically |
| Global exception handling | nebula-web | RestExceptionHandler auto-configured |
| Parameter validation | starter-validation + jakarta.validation | Just add `@Valid` on controllers |
| Pagination | nebula-mybatis | Composable `NebulaPageQuery` + `PageHelperUtils`, multi-column sorting |
| Logging & tracing | micrometer-tracing-bridge-otel | The logback template prints `traceId-spanId` on every line; OTLP export is opt-in |
| Sample flow | SysUser CRUD | Complete four-layer code demo including paginated queries |

### Logging and OpenTelemetry

A `logback-spring.xml` is built in; via Boot's official `%correlationId` conversion word, every log line automatically carries the trace identifiers:

```text
2026-08-26 10:00:00.123 INFO  [http-nio-8080-exec-1] [4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7] c.e.d.controller.SysUserController - ...
```

- traceId/spanId are generated automatically by micrometer-tracing (OpenTelemetry API bridge) on every HTTP request and propagate through the call chain; sampling rate via `management.tracing.sampling.probability`
- By default it **only prints the tid without exporting** (zero noise); to connect a Collector, enable `management.otlp.tracing.endpoint` in `application.yaml` to export OTLP
- The production profile adds async rolling file logs (`./logs/{app}.log`); default is console-only

## Run and Test

Defaults to an in-memory H2 database; `schema.sql` initializes two sample rows automatically:

```bash
mvn verify                      # Build + contextLoads test
mvn spring-boot:run -pl demo-start

curl "http://localhost:8080/sys-users/page?page.pageSize=1&page.pageIndex=1"
curl -X POST http://localhost:8080/sys-users -H 'Content-Type: application/json' -d '{"name":"carol","age":25}'
```

Pagination parameters are nested: `page.pageSize` / `page.pageIndex`; sorting uses `page.sorts[0].column` and `page.sorts[0].direction` (column names are validated against a whitelist to prevent SQL injection).

## Switch to MySQL

Replace the datasource in `demo-start/src/main/resources/application.yaml` and drop the H2 dependency:

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/demo
    username: root
    password: xxx
```

## Extending with More Capabilities

Versions are centrally managed by the parent pom's BOM — just add dependencies to the appropriate module as needed:

| Capability | ArtifactId | Suggested module |
|------------|------------|------------------|
| Feign unwrapping | `spring-boot-nebula-feign` | start |
| Excel import/export | `spring-boot-nebula-excel` | start |
| Distributed lock (requires Redis) | `spring-boot-nebula-distribute-lock` | start |
| Dynamic datasource / read-write splitting | `spring-boot-nebula-dynamic-datasource` | infrastructure |

## Upgrading Nebula

The generated parent pom has a dedicated property — change it in one place to upgrade the whole project:

```xml
<properties>
    <nebula.version>3.0.6</nebula.version>
</properties>
```

## Docker Deployment

```bash
docker build -t demo .
docker run -p 8080:8080 demo
```

## K8s Deployment

Manifests are in `deploy/k8s/` (deployment.yaml + service.yaml). Key design decisions:

- **Java Agent mounting**: an init-container downloads two agents into a shared `emptyDir` at startup; the main container loads them read-only via `JAVA_TOOL_OPTIONS`, keeping the image pristine — upgrading an agent only requires changing the version number in env:
  - OpenTelemetry Java Agent — automatic instrumentation with OTLP export (default address `otel-collector.observability.svc.cluster.local:4317`)
  - Alibaba TTL Agent (transmittable-thread-local) — propagates ThreadLocal and tracing context across thread pools; remove it from `JAVA_TOOL_OPTIONS` if not needed
- **Probes**: readiness/liveness use actuator health groups
- **No double reporting**: in-app SDK export is disabled by default (commented out in application.yaml) — choose either the agent mode or in-app export, not both
