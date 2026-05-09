# hojoai-common

HoJoAI 体系内的 **Java 共享工具库**（`jar`），由 [hojoai-parent](../hojoai-parent) 管理依赖版本。

## 职责

- 提供统一 HTTP 响应包装、分页、断言、Jackson/日期等通用工具类型。
- **非独立进程**：不对外监听端口，不暴露 HTTP、gRPC 或 WebSocket。

## 使用方式

在其他服务的 `pom.xml` 中依赖本模块（版本与 `hojoai-parent` / `hojoai-common.version` 对齐），按需引用 `com.hojoai.common.*` 下的类型与工具类。

## 文档

- [API.md](./API.md) — 对外复用类型与接口说明（以源码为准）。

## 构建

```bash
cd hojoai-common && mvn clean install
```
