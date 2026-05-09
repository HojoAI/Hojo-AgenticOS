# hojoai-gateway-central

**中央域 API 网关**：基于 Spring Cloud Gateway，负责将外部请求按路径转发到下游微服务（如中央账户服务），提供统一入口与基础过滤能力。

## 能力列表

- 路由转发：将请求转发到中央域微服务
- 健康检查：Spring Boot Actuator 健康端点

## 技术栈

- Java 21、Spring Boot（版本见 [hojoai-parent](../hojoai-parent)）
- Spring Cloud Gateway、Nacos（服务发现与配置，按环境启用）

## 运行约定

| 项目 | 说明 |
|------|------|
| 模块结构 | 聚合工程，可运行模块为 `hojoai-gateway-central-server` |
| 默认 HTTP 端口 | `11001`（见 `hojoai-gateway-central-server/.../application.properties`） |
| Spring 应用名 | `hojoai-gateway-central` |

## 构建与运行

```bash
cd hojoai-gateway-central
mvn clean install
cd hojoai-gateway-central-server && mvn spring-boot:run
```

## 文档

- [API.md](./API.md) — 路由表、过滤器说明及下游文档索引。
