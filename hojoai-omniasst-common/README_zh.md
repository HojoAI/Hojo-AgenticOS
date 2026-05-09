# hojoai-omniasst-common

**Omniasst 共享库**：Omniasst 业务域内的 Java 共享库，提供会话与消息 DTO、形象配置、Memory 服务客户端、领域常量与工具类等。

## 职责

- **会话与消息 DTO**：`com.hojoai.omniasst.common.api.conversation` 下推送、加消息、自由对话、消息扩展等请求/响应与内容项类型。
- **形象配置**：`com.hojoai.omniasst.common.api.avatar`（如 `AvatarItem`）。
- **记忆（Memory）服务客户端**：基于 **Spring Cloud OpenFeign** 的 `MemoryApiClient`（`/api/v1/memories`），及请求/响应 DTO、独立 Feign 配置与 **Spring Boot 3** 自动配置（`MemoryAutoConfiguration`）。
- **领域常量**：`com.hojoai.omniasst.common.constans.conversation` 下的消息类型、角色、Agent 类型等枚举（包名以源码为准）。
- **工具类**：`MemoryUtils`、`SpringContextHolder` 等（`com.hojoai.omniasst.common.utils`）。

本模块为**类库**，**非**独立 Spring Boot 可执行应用；不对外监听端口。使用方需已引入 OpenFeign 与 Web 相关依赖（本库中多为 `provided` 作用域）。

## 使用方式

1. 在业务服务 `pom.xml` 中依赖本构件（`groupId` / `artifactId` / `version` 与各 `*-server` 模块保持一致）。
2. 确保宿主应用启用 Feign（如 `@EnableFeignClients` 或由本库自动配置覆盖）。
3. Memory 能力：在 `application.yml` 中配置 `hojoai.memory.api.*`；完整键说明见 [application-example.yml](./application-example.yml) 与 [API.md](./API.md)。

## 文档

- [API.md](./API.md) — 包结构、自动配置与配置前缀说明（**以源码为准**）。

## 构建

须先安装父 BOM **`hojoai-parent`**，再在本目录执行：

```bash
mvn clean install -DskipTests
```

全仓多模块顺序见 [docs/BUILD.md](../docs/BUILD.md)。
