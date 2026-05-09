# hojoai-webmvc-omniasst-translate

**Omniasst Translation Service**: Maintains translation sessions and message lists, provides translation session management capabilities, and calls downstream services such as voice foundation, management domain, and title generation via internal clients.

## Capabilities

- Translation session: Translation session creation, message list maintenance
- Real-time translation: WebSocket real-time voice translation stream
- ASR integration: Speech recognition integration
- VAD integration: Voice activity detection
- Title generation: LLM-driven session title generation
- Voice cloning: Optional voice cloning capability

## Tech Stack

- Java 21, Spring Boot Web
- Inherits from [hojoai-parent](../hojoai-parent)

## Runtime Conventions

| Item | Description |
|------|-------------|
| Runnable module | `hojoai-webmvc-omniasst-translate-server` |
| Default HTTP port | `12006` |
| Servlet context-path | `/omniasst-translate` |
| Spring application name | `hojoai-webmvc-omniasst-translate` |
| Via gateway | [hojoai-gateway-omniasst](../hojoai-gateway-omniasst) `/omniasst-translate/**` |

## Build and Run

```bash
cd hojoai-webmvc-omniasst-translate
mvn clean install
cd hojoai-webmvc-omniasst-translate-server && mvn spring-boot:run
```

## Documentation

- [API.md](./API.md) — External REST and internal Feign documentation.