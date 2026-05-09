# hojoai-websocket-omniasst-sysagent

**Omniasst System Assistant Service**: Provides HTTP dialogue (SSE streaming) and WebSocket control plane, supports intent routing to multi-agent chains such as chat, web search, music, weather, calendar, podcast, etc., and includes core capabilities like headphone agent and session governance.

## Capabilities

### Dialogue and Intent Routing
- chat: General chat via model API
- chat_websearch: Web search chat
- doc: Image and text content processing
- translator: Translation
- meeting: Meeting scenario
- event/focus: Calendar service (calls focus microservice)
- music: Music service (calls music microservice)
- weather: Weather service (calls weather microservice)
- podcast: Podcast/news recommendation (calls news microservice)

### SSE Event Protocol
- ping: Heartbeat (~10 second interval)
- loading: Loading indicator (thinking..., searching web...)
- message: Core business response (JSON, contains conversationId, messageId, data array)
- error: Error message
- agentType: Sub-agent type announcement (chat, music, weather, etc.)

### WebSocket Control Plane
- BUILD_RTC_TOKEN: Request RTC Token issuance
- TICKET_RESULT: Movie ticket session binding

### Headphone Agent
- Reuse by business day: userId + business day binds to a single session
- Bubble复制为Speech: Vertical agent copies bubble content as speech for TTS
- agentType event suppression: Suppresses main chat agentType events when headphoneMode=true

### Session Governance
- Stop generation: Abort SSE stream
- Delete message: Delete single message and reload context
- Delete session: Complete session cleanup (including Redis cache)
- Context management: Redis cache + management sync

### Extended Business APIs
- Movie query: Movies, theaters, showtimes, seats, orders
- Payment callback: Alipay, Panhe payment async notification
- Push: Youmeng push integration
- OAuth: Coze platform token acquisition

For external APIs, field definitions, SSE events, and WebSocket protocol, see **[API.md](./API.md)**.

## Tech Stack

- Java 21, Spring Boot 3.x (Web, **WebSocket**, **WebFlux**, Actuator, AOP)
- **Spring Data Redis**, **OpenFeign** + LoadBalancer
- Inherits from [hojoai-parent](../hojoai-parent)

## Modules and Source Code Entry Points

| Module | Description |
|--------|-------------|
| `hojoai-websocket-omniasst-sysagent-server` | Runnable application: configuration in `src/main/resources`, main code under `.../sysagent/`. |

Common packages:

- `controller/conversation` — SSE dialogue and delete/stop endpoints
- `service/conversation` — Dialogue pipeline, intent and various `*AgentServiceImpl`
- `interaction/websocket` — WebSocket handling and `IMessage` / `IMessageEvent`
- `service/api/ConversationAPI` — Feign client for calling management

## Quick Start

```bash
cd hojoai-websocket-omniasst-sysagent
mvn clean package -DskipTests
java -jar hojoai-websocket-omniasst-sysagent-server/target/hojoai-websocket-omniasst-sysagent-server-1.0.0-SNAPSHOT.jar
```

For complete deployment steps, environment variables, and K8s template usage, see **[deploy.md](./deploy.md)**.

## Documentation

- **Recommended reading order**: First read `README.md` (module and capabilities) → then `API.md` (integration and protocol) → finally `deploy.md` (running and deployment).
- **[API.md](./API.md)** — HTTP/SSE APIs, request body fields, headphone agent semantics, SSE events, and WebSocket protocol.
- **[deploy.md](./deploy.md)** — Runtime conventions, environment variables, Maven/Docker/Kubernetes deployment instructions.