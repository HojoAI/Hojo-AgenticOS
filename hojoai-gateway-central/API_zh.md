# hojoai-gateway-central — API 文档

## 概述

基于 **Spring Cloud Gateway（WebFlux）** 的中央网关，默认监听端口见 `application.properties`（如 `11001`）。自身几乎不实现业务 REST，主要将请求 **按路径转发** 到下游服务。

## 下游路由（节选）

配置见 `hojoai-gateway-central-server/src/main/resources/config/application-*.properties`。

| 路由 ID | 匹配路径 | 下游服务（示例） |
|---------|----------|-------------------|
| `hojoai-webmvc-central-account` | `/central-account/**` | `lb://hojoai-webmvc-central-account` |

> 实际主机与端口以部署环境为准；客户端经网关访问时，路径前缀与上表一致。

## 过滤器

- `CentralGlobalFilter`：全局前置逻辑（当前主要为示例日志），可扩展鉴权、埋点等。

## 相关文档

中央账户等业务接口详见 **hojoai-webmvc-central-account** 的 `API.md`。
