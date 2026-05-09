# hojoai-template — API 文档

## 基址

- 默认 **无** `server.servlet.context-path`（根路径 `/`）
- 默认端口见 `application.properties`（如 `12000`）

## REST 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/template/helloWorld` | 示例问候接口；请求体 `HelloWorldRequest`（需非空 `asking`）；响应为 `Response<HelloWorldResponse>` |

实现类：`HelloWorldController`。

## 说明

本模块为 **工程模板/示例**，生产环境请按业务替换路径与实现。
