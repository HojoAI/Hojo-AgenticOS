# hojoai-grpc-central-provider

**中央域 gRPC 服务**：对外提供订单、电影、航班、火车、推送等 RPC 能力，附带 Spring Boot HTTP 端口用于健康检查与管理。

## 能力列表

- 订单 RPC：订单查询、创建、状态更新
- 电影 RPC：影片、影院、场次、座位查询
- 航班 RPC：航班查询与预订
- 火车 RPC：火车票查询与预订
- 推送 RPC：消息推送能力
- 健康检查：HTTP 端口提供健康检查与管理端点

## 模块结构

| 模块 | 说明 |
|------|------|
| `hojoai-grpc-central-provider-api` | `.proto` 与生成代码（供服务端与客户端依赖） |
| `hojoai-grpc-central-provider-server` | 可运行的 gRPC 服务端 |

## 技术栈

- Java 21、gRPC、Protobuf
- Spring Boot、Nacos（含 `grpc-port` 等 metadata）

## 运行约定

| 项目 | 说明 |
|------|------|
| 默认 HTTP 端口 | `11003`（`server.port`） |
| gRPC 端口 | `config/application-dev.properties` 中 `grpc.port`（如 **11103**） |
| Spring 应用名 | `hojoai-grpc-central-provider` |

客户端应访问 **`主机:grpc.port`**；HTTP 端口一般不作为业务 API。

## 构建与运行

```bash
cd hojoai-grpc-central-provider
mvn clean install
cd hojoai-grpc-central-provider-server && mvn spring-boot:run
```

## 文档

- [API.md](./API.md) — 各 Service/RPC 一览与 proto 位置说明。
- 子模块 [hojoai-grpc-central-provider-api/README.md](./hojoai-grpc-central-provider-api/README.md)（若需单独说明 api 包可继续维护）。
