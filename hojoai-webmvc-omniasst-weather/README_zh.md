# hojoai-webmvc-omniasst-weather

**Omniasst 天气服务**：基于 SSE 流式返回天气相关对话，支持多轮会话与地理位置等输入，适用于语音助手等天气查询场景。

## 能力列表

- 天气查询：自然语言天气查询，支持位置/日期解析
- 天气预报：实时天气、当日预报、逐小时预报
- 生活指数：紫外线、穿衣建议等生活指数
- 空气指数：空气质量信息
- 卡片式回复：天气概览、详情、降雨、风力、生活指数等多类型卡片
- SSE 流式：Server-Sent Events 流式返回，支持多轮会话

## 技术栈

- Java 21、Spring Boot Web
- 继承 [hojoai-parent](../hojoai-parent)；后端对接 Dify/编排逻辑以环境与配置为准

## 运行约定

| 项目 | 说明 |
|------|------|
| 可运行模块 | `hojoai-webmvc-omniasst-weather-server` |
| 默认 HTTP 端口 | `12010` |
| Servlet context-path | `/omniasst-weather` |
| Spring 应用名 | `hojoai-webmvc-omniasst-weather` |
| 经网关 | [hojoai-gateway-omniasst](../hojoai-gateway-omniasst) `/omniasst-weather/**` |

## 构建与运行

```bash
cd hojoai-webmvc-omniasst-weather
mvn clean install
cd hojoai-webmvc-omniasst-weather-server && mvn spring-boot:run
```

## 文档

- [API.md](./API.md) — `/weather-agent/chat` 与 `/weather-agent/chat/v1` 的接口摘要；请求体字段与 SSE 事件格式以实现类 `WeatherAgentController` 及 `SAAgentRequest` 为准。
