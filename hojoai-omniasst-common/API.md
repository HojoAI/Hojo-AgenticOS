# hojoai-omniasst-common — API Documentation

## Overview

`hojoai-omniasst-common` is **Omniasst domain shared types and client library** (not a standalone process), **does not expose HTTP ports**. External reuse is primarily via Java packages and Spring Boot auto-configuration.

## Spring Boot Auto-Configuration

| Item | Description |
|------|-------------|
| Registration entry | `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` registers `MemoryAutoConfiguration` |
| Activation condition | OpenFeign (`FeignClient`) exists on classpath, and `hojoai.memory.api.enabled` is not `false` (enabled by default when not configured) |
| Behavior | Enables `MemoryApiProperties`, registers `MemoryApiClient` Feign client, scans `com.hojoai.omniasst.common.utils` (includes `SpringContextHolder` and other Beans) |

`MemoryUtils` is a static utility class, **not registered as a Bean via auto-configuration**.

## Configuration Prefix: `hojoai.memory.api`

Aligned with `MemoryApiProperties` and `MemoryApiClient`'s `url` placeholder. Common items include:

| Config Item | Description |
|-------------|-------------|
| `url` | Memory service base address (Feign default placeholder and property default is `http://localhost:8001`) |
| `connect-timeout` / `read-timeout` | Feign timeout (milliseconds) |
| `log-enabled` / `log-level` | Feign log switch and level |
| `organization-id` / `space-id` / `hash-key` | Multi-tenant related request headers (used by `MemoryFeignConfig`) |

See [application-example.yml](./application-example.yml) in the repository for examples.

## Memory Feign: `MemoryApiClient`

Declared at **`${hojoai.memory.api.url}`** under **`/api/v1/memories`**, main methods include (paths relative to `path`):

| Method | HTTP | Description |
|--------|------|-------------|
| `storeMessage` | `POST /` | Store message, supports `sync_mode` query parameter |
| `fetchMemories` | `GET /` | Fetch memories by user/group/type conditions |
| `searchMemories` | `GET /search` | Keyword/vector search |
| `getConversationMeta` / `saveConversationMeta` / `updateConversationMeta` | `GET|POST|PATCH /conversation-meta` | Conversation metadata |
| `deleteMemories` | `DELETE /` | Delete by conditions |
| `initTenantDatabase` | `POST /internal/tenant/init-db` | Tenant database initialization (internal) |

Unified return wrapper type is in `com.hojoai.omniasst.common.api.memory.dto.response.ApiResponse`.

## Other Reusable Types (Summary)

| Package Path | Purpose |
|--------------|---------|
| `com.hojoai.omniasst.common.api.conversation` | Conversation push, add message, free chat, update message extension, etc. DTOs |
| `com.hojoai.omniasst.common.api.avatar` | Avatar/profile items |
| `com.hojoai.omniasst.common.api.memory.dto` and sub-packages | Memory request/response bodies and domain objects |
| `com.hojoai.omniasst.common.constans.conversation` | Message types, categories, roles, Agent types, etc. enums |

Field names, validation rules, and enum values **are based on source code and Jackson annotations**.