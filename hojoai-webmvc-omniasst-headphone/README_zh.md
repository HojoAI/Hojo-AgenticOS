# hojoai-webmvc-omniasst-headphone

**Omniasst 耳机设备 Agent 服务**：作为设备端与云端编排系统的中间层，负责耳机设备的会话管理、意图路由与流式响应，支持耳机场景下的语音交互与多轮对话。

## 能力列表

- 耳机设备会话管理：基于业务日的会话绑定与状态维护
- 耳机端 ASR 处理：WebSocket 实时音频流接收与 VAD 超时控制
- 流式响应：SSE 流式返回，支持语音播报与气泡同步
- 耳机模式路由：识别 `headphoneMode` 上下文，转发至对应场景 Agent
- 多 Agent 协作：翻译、会议、专注、音乐、天气、播客等场景联动

## 技术栈

- Java 21、Spring Boot Webflux
- 继承 [hojoai-parent](../hojoai-parent)
- Nacos 服务发现与配置管理

## 运行约定

| 项目 | 说明 |
|------|------|
| 可运行模块 | `hojoai-webmvc-omniasst-headphone-server` |
| 默认 HTTP 端口 | `12011` |
| Servlet context-path | `/omniasst-headphone` |
| Spring 应用名 | `hojoai-webmvc-omniasst-headphone` |
| 健康检查 | `GET /omniasst-headphone/health` |
| 经网关 | [hojoai-gateway-omniasst](../hojoai-gateway-omniasst) `/omniasst-headphone/**` |

## 构建与运行

```bash
cd hojoai-webmvc-omniasst-headphone
mvn clean install
cd hojoai-webmvc-omniasst-headphone-server && mvn spring-boot:run
```

## 文档

- 健康检查接口：`GET /omniasst-headphone/health`
- 更多接口文档待补充