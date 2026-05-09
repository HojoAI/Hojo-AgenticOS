# hojoai-parent — API 说明

## 概述

`hojoai-parent` 为 **Maven 父 POM（BOM）**，用于统一依赖版本与插件配置，**不包含可运行应用，也不提供任何网络 API**。

## 说明

- 不参与运行时部署；无 HTTP、gRPC、WebSocket 端点。
- 各子工程通过继承该 POM 获得统一的 Spring Boot、Spring Cloud、gRPC 等版本管理。
