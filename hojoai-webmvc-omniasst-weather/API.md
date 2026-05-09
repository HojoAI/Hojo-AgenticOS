# hojoai-webmvc-omniasst-weather — API Documentation

## Base URL

- **context-path**: `/omniasst-weather`
- Via gateway: `{gateway}/omniasst-weather/...`

## Weather Agent — `/weather-agent`

| Method | Path | Content-Type | Description |
|--------|------|--------------|-------------|
| POST | `/weather-agent/chat` | Request `application/json`, response **SSE** `text/event-stream` | Weather agent **v2** (slots, Caiyun Weather, card orchestration etc.) |
| POST | `/weather-agent/chat/v1` | Same as above | Weather agent **v1** (Dify streaming conversation) |

### Request Body (`SAAgentRequest`)

- Required: `userId`, `query`
- Optional: `conversationId`, `inputs`

Implementation class: `WeatherAgentController`.