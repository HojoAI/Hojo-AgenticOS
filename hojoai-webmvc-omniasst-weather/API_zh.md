# hojoai-webmvc-omniasst-weather — API 文档

## 基址

- **context-path**：`/omniasst-weather`
- 经网关：`{网关}/omniasst-weather/...`

## 天气智能体 — `/weather-agent`

| 方法 | 路径 | Content-Type | 说明 |
|------|------|--------------|------|
| POST | `/weather-agent/chat` | 请求 `application/json`，响应 **SSE** `text/event-stream` | 天气智能体 **v2**（槽位、彩云天气、卡片编排等） |
| POST | `/weather-agent/chat/v1` | 同上 | 天气智能体 **v1**（Dify 流式对话） |

### 请求体（`SAAgentRequest`）

- 必填：`userId`、`query`
- 可选：`conversationId`、`inputs`

实现类：`WeatherAgentController`。
