# hojoai-webmvc-omniasst-headphone

**Omniasst Earphone Device Agent Service**: Acts as the middleware between device and cloud orchestration system, responsible for earphone device session management, intent routing, and streaming responses, supporting voice interaction and multi-turn dialogue in earphone scenarios.

## Capabilities

- Earphone Device Session Management: Business-day based session binding and state maintenance
- Earphone ASR Processing: WebSocket real-time audio stream reception and VAD timeout control
- Streaming Response: SSE streaming, supports voice broadcast and bubble synchronization
- Headphone Mode Routing: Recognizes `headphoneMode` context, forwards to corresponding scenario agents
- Multi-Agent Collaboration: Works with translator, meeting, focus, music, weather, podcast scenario agents

## Tech Stack

- Java 21, Spring Boot Webflux
- Inherits [hojoai-parent](../hojoai-parent)
- Nacos service discovery and configuration management

## Runtime Conventions

| Item | Description |
|------|-------------|
| Runnable Module | `hojoai-webmvc-omniasst-headphone-server` |
| Default HTTP Port | `12011` |
| Servlet context-path | `/omniasst-headphone` |
| Spring Application Name | `hojoai-webmvc-omniasst-headphone` |
| Health Check | `GET /omniasst-headphone/health` |
| Via Gateway | [hojoai-gateway-omniasst](../hojoai-gateway-omniasst) `/omniasst-headphone/**` |

## Build and Run

```bash
cd hojoai-webmvc-omniasst-headphone
mvn clean install
cd hojoai-webmvc-omniasst-headphone-server && mvn spring-boot:run
```

## Documentation

- Health check endpoint: `GET /omniasst-headphone/health`
- Additional API documentation to be added