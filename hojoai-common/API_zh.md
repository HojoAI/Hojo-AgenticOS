# hojoai-common — API 说明

## 概述

`hojoai-common` 为 **Java 共享工具库**（非独立进程），**不对外暴露 HTTP、gRPC 或 WebSocket 服务**。

## 对外接口（供其他服务复用）

其他微服务在实现 REST 接口时，常使用本模块中的类型作为统一响应包装或工具：

| 类型 | 说明 |
|------|------|
| `com.hojoai.common.response.Response` | 统一 HTTP 响应包装 |
| `com.hojoai.common.response.ResponseStatus` / `ResponseStatusBasic` | 响应状态枚举 |
| `com.hojoai.common.page.IPage` | 分页结构 |
| `com.hojoai.common.asserts.Asserts` | 参数断言 |
| `com.hojoai.common.jackson.JacksonUtil` | JSON 工具 |
| `com.hojoai.common.date.DateUtil` | 日期工具 |

具体字段与用法以源码为准。
