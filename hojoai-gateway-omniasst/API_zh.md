# hojoai-gateway-omniasst — API 文档

## 概述

Omniasst 业务网关（Spring Cloud Gateway），默认端口见 `application.properties`（如 `12001`）。职责包括：**路由转发**、**部分路径的用户 Token 校验**（`UserTokenGlobalFilter`）、以及 **Digital Asset Links** 静态声明。

## 网关自身暴露的 HTTP

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/.well-known/assetlinks.json` | Android App Links 声明，内容由 classpath `static/.well-known/assetlinks.json` 提供；缺失时返回 `[]` |

实现类：`WellKnownController`。

## 路由表（与 `application-*.properties` 一致）

| 路由 ID | 匹配路径 | 下游 |
|---------|----------|------|
| `hojoai-websocket-omniasst-sysagent` | `/omniasst-sysagent/**` | `lb:ws://hojoai-websocket-omniasst-sysagent`（WebSocket） |
| `hojoai-webmvc-omniasst-account` | `/omniasst-account/**` | `lb://hojoai-webmvc-omniasst-account` |
| `hojoai-webmvc-omniasst-management` | `/omniasst-management/**` | `lb://hojoai-webmvc-omniasst-management` |
| `hojoai-webmvc-omniasst-voice` | `/omniasst-voice/**` | `lb://hojoai-webmvc-omniasst-voice` |
| `hojoai-webmvc-omniasst-translate` | `/omniasst-translate/**` | `lb://hojoai-webmvc-omniasst-translate` |
| `hojoai-webmvc-omniasst-meeting` | `/omniasst-meeting/**` | `lb://hojoai-webmvc-omniasst-meeting` |
| `hojoai-webmvc-omniasst-focus` | `/omniasst-focus/**` | `lb://hojoai-webmvc-omniasst-focus` |
| `hojoai-webmvc-omniasst-rtc` | `/omniasst-rtc/**` | `lb://hojoai-webmvc-omniasst-rtc` |
| `hojoai-webmvc-omniasst-music` | `/omniasst-music/**` | `lb://hojoai-webmvc-omniasst-music` |
| `hojoai-websocket-omniasst-news` | `/omniasst-news/**` | `lb://hojoai-websocket-omniasst-news` |
| `hojoai-webmvc-omniasst-knowledge` | `/omniasst-knowledge/**` | `lb://hojoai-webmvc-omniasst-knowledge` |
| `hojoai-webmvc-omniasst-model` | `/omniasst-model/**` | `lb://hojoai-webmvc-omniasst-model` |

> 部分环境配置文件（如 `application-dev.properties`）可能仅包含路由子集，以当前激活的 profile 为准。

## Token 校验说明（摘要）

`UserTokenGlobalFilter` 对 **配置表内** 的部分路径与方法要求请求头携带 `X-User-Id`、`X-User-Token`、`X-Platform-Type`，并调用账户服务校验。未列入需校验路由表的路由 **不** 经该过滤器校验。细节见源码 `UserTokenGlobalFilter.USER_TOKEN_REQUIRED_ROUTES`。

## 相关文档

各下游服务的 REST/WebSocket 定义见对应模块的 `API.md`。
