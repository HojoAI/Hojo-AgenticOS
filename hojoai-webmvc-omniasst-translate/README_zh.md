# hojoai-webmvc-omniasst-translate

**Omniasst 翻译服务**：维护翻译会话与消息列表，提供翻译会话管理能力，并通过内部客户端调用语音基础能力、管理域与标题生成等下游服务。

## 能力列表

- 翻译会话：翻译会话创建、消息列表维护
- 实时翻译：WebSocket 实时语音翻译流
- ASR 集成：语音识别集成
- VAD 集成：语音活动检测
- 标题生成：LLM 驱动的会话标题生成
- 语音克隆：可选的语音克隆能力

## 技术栈

- Java 21、Spring Boot Web
- 继承 [hojoai-parent](../hojoai-parent)

## 运行约定

| 项目 | 说明 |
|------|------|
| 可运行模块 | `hojoai-webmvc-omniasst-translate-server` |
| 默认 HTTP 端口 | `12006` |
| Servlet context-path | `/omniasst-translate` |
| Spring 应用名 | `hojoai-webmvc-omniasst-translate` |
| 经网关 | [hojoai-gateway-omniasst](../hojoai-gateway-omniasst) `/omniasst-translate/**` |

## 构建与运行

```bash
cd hojoai-webmvc-omniasst-translate
mvn clean install
cd hojoai-webmvc-omniasst-translate-server && mvn spring-boot:run
```

## 文档

- [API.md](./API.md) — 对外 REST 与内部 Feign 说明。
