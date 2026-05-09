# hojoai-webmvc-omniasst-weather

**Omniasst Weather Service**: Returns weather-related conversation via SSE streaming, supports multi-turn dialogue and location/date input, suitable for voice assistant weather query scenarios.

## Capabilities

- Weather Query: Natural language weather query, supports location/date parsing
- Weather Forecast: Real-time weather, today's forecast, hourly forecast
- Life Index: UV index, clothing recommendations, etc.
- Air Quality Index: Air quality information
- Card-style Responses: Multiple card types including weather overview, details, rainfall, wind, life index, etc.
- SSE Streaming: Server-Sent Events streaming, supports multi-turn dialogue

## Tech Stack

- Java 21, Spring Boot Web
- Inherits [hojoai-parent](../hojoai-parent); backend integrates Dify/orchestration logic per environment and configuration

## Runtime Conventions

| Item | Description |
|------|------|
| Runnable Module | `hojoai-webmvc-omniasst-weather-server` |
| Default HTTP Port | `12010` |
| Servlet context-path | `/omniasst-weather` |
| Spring Application Name | `hojoai-webmvc-omniasst-weather` |
| Via Gateway | [hojoai-gateway-omniasst](../hojoai-gateway-omniasst) `/omniasst-weather/**` |

## Build and Run

```bash
cd hojoai-webmvc-omniasst-weather
mvn clean install
cd hojoai-webmvc-omniasst-weather-server && mvn spring-boot:run
```

## Documentation

- [API.md](./API.md) — Interface summary for `/weather-agent/chat` and `/weather-agent/chat/v1`; request body fields and SSE event format are based on implementation class `WeatherAgentController` and `SAAgentRequest`.