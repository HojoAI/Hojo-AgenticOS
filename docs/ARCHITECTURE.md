# Architecture Details

<div align="center">

<img src="../assets/images/hojoai-architecture.png" alt="Hojo-Agentic-OS Architecture" width="841" />

</div>

Terminology definitions, service boundaries, routing key points, and **Earphone Agent** (`headphoneMode` orchestration and planned access governance). Components and ports reference in [README](../README.md#architecture--components).

---

## Terminology and Boundaries

| Term | Description |
|------|-------------|
| **Central** | `hojoai-gateway-central`, `hojoai-webmvc-central-account`, `hojoai-grpc-central-provider` |
| **Omniasst** | Hojo business domain processes; clients primarily access via Omniasst gateway |
| **Voice** | Module directory is `hojoai-gateway-omniasst-voice`, Nacos registration name is often `hojoai-webmvc-omniasst-voice` (no same-name `webmvc` directory); see **Voice** entry below |
| **Client** | Does **NOT** go through `gateway-central`; externally primarily via **Omniasst Business Gateway** (`hojoai-gateway-omniasst`). Central account is accessed by server side via **`central.account.base.url`**, Nacos + Feign, etc. to **`hojoai-webmvc-central-account`**. |
| **To Central** | Business side mostly via Feign / gRPC / configured URLs on service discovery |

---

## Implementation and Routing Key Points

- **Interface Definition & Routing**: Nacos registration names, complete routes, and interfaces are based on **dev** configuration, [hojoai-gateway-omniasst/API.md](../hojoai-gateway-omniasst/API.md), and each module's documentation. Downstream services like rtc, news, knowledge, model in `hojoai-gateway-omniasst` may have configuration only with **no** source code in this repository.
- **Voice**: This repository only contains `hojoai-gateway-omniasst-voice`, registered in Nacos as `hojoai-webmvc-omniasst-voice`, called by other services via Feign; there is no separate `hojoai-webmvc-omniasst-voice` directory module.
- **Meeting**: This repository contains `hojoai-webmvc-omniasst-meeting`; paths exposed via gateway are based on the above API documentation and **dev** configuration.
- **Weather**: `hojoai-webmvc-omniasst-weather` is in this repository, but Omniasst gateway default routing usually does **not** include `/omniasst-weather/**`, mostly exposed via direct connection or other entry points.
- **Schedule Service (`hojoai-webmvc-omniasst-focus`)**: Gateway `lb://` target and Nacos registration name are both `hojoai-webmvc-omniasst-focus`.

---

## Earphone Agent

Targeting earphone, car, phone multi-device collaboration: **Device Collaboration** (same session multi-device input/output/control) and **Protocol Governance** (long-term goal, access side based on planned **ProtocolGuard** description).

### Implementation Status (Summary)

| Status | Location | Content |
|--------|----------|---------|
| **Planned** | Outermost layer outside Client | **ProtocolGuard**: **Authentication**; **Schema & State Machine Validation**; **Compatibility Matrix Check**; **Anti-Replay**. **This repository does not yet have an independent implementation**; cross-reference with the outermost ring of [README architecture diagram](../README.md#architecture), **live production still directly connects to two gateways and existing authentication**. |
| **Implemented** | `sysagent` | **HeadphoneAgent** (or named `HeadphoneAgentOrchestrator`, etc. in source code) under **`headphoneMode=true`**, along with **DeviceCoordinator**, **IntentPolicy & ResponsePolicy**, **SessionGovernor**, **SSE Strategy**; **no runtime dependency** on ProtocolGuard, future product alignment of strategy/error codes possible. |

**Current Layering (cross-reference with diagram)**: Device roles **Earbud / Phone / Car / Watch** (earphone primarily uses audio stream, other endpoints use control and display) → **Access** is `hojoai-gateway-omniasst-voice` and `hojoai-gateway-omniasst` → **Orchestration** is the HeadphoneAgent system above, where **intent routing engine (`IntentionServiceImpl`) is closed-source**, decoupled from each Agent via `AgentRegistry` SPI → **Capabilities** are `management`, **Redis**, music/weather/focus/meeting and other vertical services.

### Current Data Path (without ProtocolGuard)

1. **Earbud** → `hojoai-gateway-omniasst-voice` (ASR/TTS); **Phone / Car / Watch** → `hojoai-gateway-omniasst` (HTTP/WebSocket).
2. **ASR text** → via Omniasst routing enters **`sysagent` `/conversation/chat` (`headphoneMode=true`)** for intent orchestration (see [sysagent API](../hojoai-websocket-omniasst-sysagent/API.md)).
3. **HeadphoneAgent** → **Intent routing engine** (closed-source, `ROUTED_NON_CHAT_AGENTS` / `HEADPHONE_BUBBLE_TO_SPEECH_AGENTS` dispatch) → **Redis** (e.g., `userId-sessionId-deviceId-role`), **`management`**, each **vertical Agent**; **SSE Strategy** controls streaming output and device-side routing.

### Master-Slave and Handoff

- **Master device** (usually phone): identity, main session context, and network channel.
- **Slave device** (earphone/watch/car): audio capture, playback, buttons, and device events.
- **Strategy**: Same `sessionId` with multiple `deviceId`, `role` (input/output/control) determines speech, display, and takeover.
- **Handoff example**: Earphone initiates voice → phone displays card → car announces/controls.

### Planned: ProtocolGuard, Protocol Goals, and Third-Party Access

**Note**: All below are **product and protocol goals**, not automated pipelines already delivered by this repository.

- **Division with HeadphoneAgent**: Future ProtocolGuard will take over access-side strong constraints; orchestration side already implements session and interaction strategy via `headphoneMode`, `inputs.user_now_date`, SSE, `/stop`, etc. (see [sysagent API](../hojoai-websocket-omniasst-sysagent/API.md)).
- **Protocol Goals (Summary)**: Core field semantics are fixed; Extensions only allow `extensions.vendorNamespace.*`; planned signature/anti-replay, version and capability set negotiation, state machine illegal sequence interception.
- **Third-Party Access (Narrative Alignment)**: Registration → Official SDK/protocol profile → Sandbox → **SchemaGate** (Schema/state machine) → **SecurityGate** (authentication, anti-replay) → **CompatGate** (compatibility matrix) → Production approval; if ProtocolGuard is implemented after launch, do runtime consolidation. **Currently no independent ProtocolGuard process.**

### `sysagent` Submodules

| Module | Responsibility | Connections |
|--------|---------------|-------------|
| **HeadphoneAgent** | `headphoneMode` orchestration core | `/chat`, `/rechat`, `/stop` |
| **DeviceCoordinator** | Master-slave, binding, handoff | Redis, device events |
| **SessionGovernor** | Business-day sessions, locks, cross-day | management, Redis |
| **IntentPolicy & ResponsePolicy** | Intent and response forms | Each vertical Agent |
| **SSE Strategy** | Voice-first, device-side routing, cooperation with retry/stop | management, SSE |
| **Intent routing engine** (`hojoai-routing-engine` JAR, **closed-source**) | `ROUTED_NON_CHAT_AGENTS` / `HEADPHONE_BUBBLE_TO_SPEECH_AGENTS` collection definitions, switch dispatch logic, `agentType` SSE event sending | Injected by `SysagentAgentRegistry` (in `sysagent`) via `AgentRegistry` SPI with each Agent implementation; checks stop signals via `StopSignalProvider` (`SseEmitterManager` implementation); interface definitions in `hojoai-routing-engine-contract` (**open-source**, in this repository) |
| **Agent Registry** (`SysagentAgentRegistry`) | Registers all 9 Agent instances (chat / doc / translator / meeting / focus / music / weather / podcast / websearch) to the routing engine in `@PostConstruct` | Injects each `AgentService` implementation and registers with string type |

### API Index

Fields, events, and access details: **[hojoai-gateway-omniasst-voice/API.md](../hojoai-gateway-omniasst-voice/API.md)** (earphone ASR, etc.) · **[hojoai-websocket-omniasst-sysagent/API.md](../hojoai-websocket-omniasst-sysagent/API.md)** (`headphoneMode`, `inputs.user_now_date`, SSE).
