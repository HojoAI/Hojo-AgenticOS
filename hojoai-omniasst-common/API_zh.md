# hojoai-omniasst-common — API 说明

## 概述

`hojoai-omniasst-common` 为 **Omniasst 域共享类型与客户端库**（非独立进程），**不对外暴露 HTTP 端口**。对外复用内容以 Java 包与 Spring Boot 自动配置为主。

## Spring Boot 自动配置

| 项 | 说明 |
|----|------|
| 注册入口 | `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册 `MemoryAutoConfiguration` |
| 生效条件 | classpath 存在 OpenFeign（`FeignClient`），且 `hojoai.memory.api.enabled` 不为 `false`（未配置时默认启用） |
| 行为 | 启用 `MemoryApiProperties`、注册 `MemoryApiClient` Feign 客户端、扫描 `com.hojoai.omniasst.common.utils`（含 `SpringContextHolder` 等 Bean） |

`MemoryUtils` 为静态工具类，**不由**自动配置注册为 Bean。

## 配置前缀：`hojoai.memory.api`

与 `MemoryApiProperties` 及 `MemoryApiClient` 的 `url` 占位一致，常用项包括：

| 配置项 | 说明 |
|--------|------|
| `url` | Memory 服务基地址（Feign 默认占位与属性默认多为 `http://localhost:8001`） |
| `connect-timeout` / `read-timeout` | Feign 超时（毫秒） |
| `log-enabled` / `log-level` | Feign 日志开关与级别 |
| `organization-id` / `space-id` / `hash-key` | 多租户相关请求头（由 `MemoryFeignConfig` 使用） |

示例见仓库内 [application-example.yml](./application-example.yml)。

## Memory Feign：`MemoryApiClient`

声明在 **`${hojoai.memory.api.url}`** 下的 **`/api/v1/memories`**，主要方法包括（路径相对 `path`）：

| 方法 | HTTP | 说明 |
|------|------|------|
| `storeMessage` | `POST /` | 存储消息，支持 `sync_mode` 查询参数 |
| `fetchMemories` | `GET /` | 按用户/群组/类型等条件拉取记忆 |
| `searchMemories` | `GET /search` | 关键词/向量等检索 |
| `getConversationMeta` / `saveConversationMeta` / `updateConversationMeta` | `GET|POST|PATCH /conversation-meta` | 对话元数据 |
| `deleteMemories` | `DELETE /` | 按条件删除 |
| `initTenantDatabase` | `POST /internal/tenant/init-db` | 租户库初始化（内部） |

统一返回包装类型见 `com.hojoai.omniasst.common.api.memory.dto.response.ApiResponse`。

## 其他可复用类型（摘要）

| 包路径 | 用途 |
|--------|------|
| `com.hojoai.omniasst.common.api.conversation` | 会话推送、加消息、自由聊天、更新消息扩展等 DTO |
| `com.hojoai.omniasst.common.api.avatar` | 头像/形象项 |
| `com.hojoai.omniasst.common.api.memory.dto` 及子包 | Memory 请求/响应体与领域对象 |
| `com.hojoai.omniasst.common.constans.conversation` | 消息类型、分类、角色、Agent 类型等枚举 |

字段名、校验规则与枚举取值**以源码与 Jackson 注解为准**。
