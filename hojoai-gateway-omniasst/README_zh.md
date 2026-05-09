# hojoai-gateway-omniasst

**Omniasst API 网关**：基于 Spring Cloud Gateway，负责路由转发、用户 Token 校验及 Digital Asset Links 等网关侧能力，为 Omniasst 全向助手业务线提供统一入口。

## 能力列表

- 路由转发：按路径将请求转发到下游 12 个微服务（含 HTTP 与 WebSocket）
- Token 校验：全局用户 Token 校验过滤器，保护登录/注册等敏感路径
- Digital Asset Links：提供 `/.well-known/assetlinks.json` 支持 Android App Links 验证
- 品牌响应头：注入品牌相关响应头
- 访问日志：记录所有请求访问日志

## 技术栈

- Java 21、Spring Cloud Gateway（WebFlux）
- Nacos；全局过滤器如 `UserTokenGlobalFilter`（详见源码与 [API.md](./API.md)）

## 运行约定

| 项目 | 说明 |
|------|------|
| 可运行模块 | `hojoai-gateway-omniasst-server` |
| 默认 HTTP 端口 | `12001` |
| Spring 应用名 | `hojoai-gateway-omniasst` |

## 构建与运行

```bash
cd hojoai-gateway-omniasst
mvn clean install
cd hojoai-gateway-omniasst-server && mvn spring-boot:run
```

## 文档

- [API.md](./API.md) — 网关自身路径、路由表、Token 校验摘要及各下游 `API.md` 索引。
