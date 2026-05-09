# hojoai-websocket-omniasst-sysagent — API Documentation

## Document Navigation

- [Overview](#overview)
- [Quick Start (Minimal Path)](#quickstart)
- [HTTP: Conversation and Streaming Output](#http-conversation-and-streaming-output)
- [Headphone Agent (`headphoneMode = true`)](#headphone-mode)
- [SSE: Event Names and Payloads](#sse-event-names-and-payloads)
- [WebSocket Endpoint](#websocket-endpoint)
- [Notes](#notes)

<a id="overview"></a>
## Overview

This service provides:

- **HTTP + SSE**: Main conversation flow (`/omniasst-sysagent/v0/conversation/...`), `Content-Type: text/event-stream`.
- **WebSocket**: RTC Token, movie ticket session binding and other control plane messages (root path `/omniasst-sysagent`, consistent with gateway routing).

Default HTTP port is in `application.properties` (e.g., **`12003`**). **Without servlet context-path**, all paths above are under the application root path.

When accessed through **hojoai-gateway-omniasst**, prepend **`/omniasst-sysagent`** to the gateway `Path` prefix (same path segments as direct access to the application).

For runtime conventions, environment variables and deployment steps, see **[deploy.md](./deploy.md)**.

<a id="quickstart"></a>
## Quick Start (Minimal Path)

Recommended integration order:

1. First call `/chat` to establish the SSE main pipeline and get the first frame's `conversationId` + `messageId`.
2. Save `conversationId` on the client side; continue multi-turn or call `/chat/stop` in subsequent requests.
3. When voice-first and per-business-day session are needed, enable `headphoneMode: true` in `/chat` or `/rechat`.
4. Integrate WebSocket `eventType` protocol only when RTC Token or movie ticket session binding is required.

### Minimal SSE Example (`/chat`)

```bash
curl -N --location 'http://{host}:12003/omniasst-sysagent/v0/conversation/chat' \
  --header 'Content-Type: application/json' \
  --header 'X-User-Id: 10001' \
  --data '{
    "responseMode": "streaming",
    "contents": [{ "type": "text", "text": "Hello, introduce yourself" }],
    "inputs": { "user_id": "10001" }
  }'
```

### Minimal WebSocket Upstream Example

Client upstream payload is JSON; `eventType` must exactly match `IMessageEvent.name()`:

```json
{
  "eventType": "BUILD_RTC_TOKEN"
}
```

---

<a id="http-conversation-and-streaming-output"></a>
## HTTP: Conversation and Streaming Output

Controller: `ConversationController`, prefix **`/omniasst-sysagent/v0/conversation`**.

Except where noted, request body is **JSON**, response is **`hojoai.common.response.Response`** wrapped (for non-streaming interfaces like delete/stop).

### Request Headers

| Header | Required | Description |
|--------|----------|-------------|
| `X-User-Id` | Yes (for `/chat`, `/rechat`) | User identifier; used by server for session creation, persistence, and headphone per-day session binding. |
| `Content-Type` | Yes | `application/json` (streaming interfaces also require a valid JSON request body). |

### API Summary

| Method | Path | Response | Description |
|--------|------|----------|-------------|
| POST | `/omniasst-sysagent/v0/conversation/chat` | SSE | Initiate a conversation turn, stream model and agent output. |
| POST | `/omniasst-sysagent/v0/conversation/rechat` | SSE | Re-chat/re-generate based on a historical anchored message. |
| POST | `/omniasst-sysagent/v0/conversation/message/delete-chat` | JSON | Delete a single message (synchronously synced to management). |
| POST | `/omniasst-sysagent/v0/conversation/delete-chat` | JSON | Delete the entire conversation. |
| POST | `/omniasst-sysagent/v0/conversation/chat/stop` | JSON | Stop current generation for specified `conversationId` + `messageId`. |

Direct connection example: `http://{host}:12003/omniasst-sysagent/v0/conversation/chat`.

### Request Body: `ConversationRequest` (`/chat`)

| Field | Type | Description |
|-------|------|-------------|
| `responseMode` | string | Usually `streaming` (consistent with `ConversationConstant.RESPONSE_MODE`). |
| `conversationId` | string | Can be provided for existing conversations; can be empty for new conversations, assigned by server and sent via first SSE frame. |
| `avatarId` | string | Digital human avatar ID. |
| `contents` | array | Multi-segment input; each item contains `type` (e.g., `text`, `image`...), `text`, `url`, `extra`. |
| `inputs` | object | User and environment information; see table below. |
| `headphoneMode` | boolean | Default `false`. When `true`, enables **headphone agent pipeline**: same `user_id` within a **business day** reuses **one** session ID (see below). |

In JSON, `inputs` uses **snake_case** field names (consistent with `@JsonProperty` on `ConversationRequest.Inputs`):

| JSON Field | Description |
|------------|-------------|
| `user_id` | User ID (used with header `X-User-Id`; business day is calculated at user dimension). |
| `user_nickname` | Nickname. |
| `user_now_date` | Client **business day** string; supports `yyyy-MM-dd` or `yyyy/MM/dd`. If omitted, **server local date** is used. Used to calculate key for headphone per-day session binding. |
| `user_province` / `user_city` / `user_district` | Administrative district. |
| `user_longitude` / `user_latitude` | Longitude and latitude. |
| `location` | Coarse-grained location description. |

### Request Body: `RechatRequest` (`/rechat`)

| Field | Type | Description |
|-------|------|-------------|
| `conversationId` | string | Session ID; can align with per-day cached session in headphone agent pipeline. |
| `messageId` | string | Anchored message ID. |
| `avatarId` | string | Avatar ID. |
| `inputs` | object | Same as `ConversationRequest.Inputs` (includes `user_now_date`, etc.). |
| `headphoneMode` | boolean | Semantically consistent with `ConversationRequest.headphoneMode`. |

### Request Body: `DeleteChatRequest` (Delete Operations)

| Field | Type | Description |
|-------|------|-------------|
| `conversationId` | string | Required. |
| `messageId` | string | **Delete single message** (`/message/delete-chat`) requires this field set to the target message ID. **Delete entire conversation** (`/delete-chat`) also uses this DTO with `conversationId` as the main field; whether `messageId` participates in validation depends on **management** and integration agreements. |

### Request Body: `StopRequest` (`/chat/stop`)

| Field | Type | Description |
|-------|------|-------------|
| `conversationId` | string | Session ID. |
| `messageId` | string | Current turn assistant message ID, consistent with SSE registration key. |

<a id="headphone-mode"></a>
### Headphone Agent (`headphoneMode = true`)

#### Applicable Scenarios

When the client is a **voice-first** or **long-session** product and you want the same user to always stay in **the same conversation** within a **business day** (for continuous context, to avoid cold-start new sessions each time, and to align with management's "headphone session" statistics), set **`headphoneMode: true`** in the request body of `/chat` or `/rechat` to enter the headphone agent pipeline. This is not tightly coupled with the voice service's headphone ASR WebSocket (**`/asr/v1/headphone`** in **[hojoai-gateway-omniasst-voice/API.md](../hojoai-gateway-omniasst-voice/API.md)**), but during integration it is recommended to keep **`X-User-Id` / `inputs.user_id` / `inputs.user_now_date`** semantically consistent with the voice side handshake parameters.

#### Layering and System Position

- **Layer Attribution**: Headphone Agent belongs to the **Omniasst business domain application orchestration layer**, not a standalone gateway or infrastructure component.
- **System Position**: Runs embedded within the **`hojoai-websocket-omniasst-sysagent`** process, positioned between the `/conversation/chat` entry point and the intent/session execution layer.
- **Upstream/Downstream Relationships**: Upstream handles client SSE requests; downstream orchestrates session governance (Redis + management) and intent services, and coordinates with voice gateway headphone ASR channel.

#### Specific Responsibilities

| Responsibility Area | Description |
|---------------------|-------------|
| Session Governance | Per-business-day single session, concurrent first-connection chain protection, cross-day switching and exception fallback. |
| Interaction Strategy | Controls SSE behavior in headphone scenarios (including `agentType` event strategy) and voice-first output format. |
| Intent Strategy | Applies headphone scenario rules to vertical agent pipelines (e.g., duplicating `bubble -> speech` entries). |
| Pipeline Consistency | Ensures `X-User-Id`, `inputs.user_id`, `inputs.user_now_date`, `headphoneMode` and persistence flag semantics are consistent. |

#### Server Mechanism Summary

| Mechanism | Description |
|-----------|-------------|
| Per-Day Single Session | **`HeadphoneDailyConversationService`**: In Redis, binds **`userId` + business day** to a **single unique** `conversationId` for that day; when concurrent multiple connections arrive first, uses a short-term distributed lock to prevent creating multiple sessions on the same day. |
| Business Day | Taken from **`inputs.user_now_date`** (JSON field name **`user_now_date`**), supports **`yyyy-MM-dd`** or **`yyyy/MM/dd`**; if parsing fails or not provided, uses **server local date** under **`ZoneId.systemDefault()`**. |
| Empty `userId` | When `userId` is empty, the per-day cache branch is **not** entered; falls back to the same **`ConversationManagement#createConversationAndMessageId`** behavior as normal mode. |
| Management | When creating a session, **`CreateConversationIdRequest`** carries **`headphoneMode`**; bulk persistence via Feign **`saveConversationMessages(..., headphoneMode)`** syncs the flag (see **`ConversationAPI`**). |
| Redis Keys (Implementation Details) | Binding value: `Omniasst:Headphone:Daily:{userId}:{yyyy-MM-dd}`; Lock: `Omniasst:Headphone:Daily:Lock:{userId}:{yyyy-MM-dd}` (lock TTL ~30s, wait polling constants see source code). Binding key TTL is seconds until end of business day (with upper/lower bound trimming, see **`secondsUntilEndOfBusinessDay`**). |
| Lock Wait Exhaustion | If lock cannot be acquired for a long time, a log is written and a **new session is created** (slightly deviating from the ideal "strict per-day single session"; operations should monitor Redis latency). |

#### Intent Dispatch and SSE Behavior

- **`ConversationContext.headphoneMode`** runs through the intent pipeline; multi-agent串联, each `AgentService` call logic is consistent with normal definitions.
- **Vertical Agent `subType`**: For target agents such as translation, meeting, calendar, music, weather, podcast, etc., in headphone agent pipeline, entries with **`subType=bubble`** can be duplicated as **`speech`** entries for client-side TTS / announcement (written by **`IntentionServiceImpl`** to task sequence; **not** the original intent model JSON field).
- **`agentType` SSE Event**: When the intent target is **main chat `chat`**, or falls into **default (unknown / empty `targetAgent` goes to chat)** path, **`agentType` event is **not** sent, to reduce无效 events on voice UI side; other sub-agents will still send `agentType` (value is the string corresponding to `ConversationConstant.AGENT_*`).

#### Client-Side Integration Suggestions

1. **Request Header**: `/chat` and `/rechat` must include **`X-User-Id`**, consistent with **`inputs.user_id`** in the body (or the user primary key agreed upon with the product).
2. **Business Day**: Pass **`inputs.user_now_date`** according to product definition (e.g., calendar day in user's timezone); if relying on server date, can omit or pass a date consistent with the server.
3. **`conversationId`**: Can be empty on the first day; persist the `conversationId` from the first `message` event of SSE's first frame; when crossing business days or when the key changes, the session will switch; client should follow the server's decision.
4. **Stop Generation**: Still use **`POST .../chat/stop`**, carrying the **`conversationId`** from SSE's first frame and the current turn's **`messageId`**.
5. **Serial with Voice ASR**: After recognition completes, write the text into **`contents`** (e.g., `type: text`) and call `/chat`, keeping **`headphoneMode: true`**; voice WebSocket recommends carrying a user identifier from the same source as **`X-User-Id`** during handshake for troubleshooting and audit (see voice service **`API.md`**).

---

<a id="sse-event-names-and-payloads"></a>
## SSE: Event Names and Payloads

Server uses Spring **`SseEmitter`**, event names are consistent with constants in `ConversationConstant`:

| event (SSE `event:` line) | `data` format | Description |
|---------------------------|---------------|-------------|
| `ping` | Empty string | Heartbeat approximately every 10 seconds. |
| `loading` | Plain text | Loading text such as "Thinking...". |
| `message` | **JSON string** | Mostly `ConversationResponse` serialized result. |
| `error` | Plain text | Error message. |
| `agentType` | Plain text | Sub-agent type about to be entered; value is identifier consistent with `ConversationConstant.AGENT_*` such as `chat`, `music`, `weather`, etc. |

`ConversationResponse` main fields: `conversationId`, `messageId`, `avatarId`, `timestamp`, `finished`, `data` (array, each item contains `type`, `text`, `url`, `subType`, `contentId`). **First frame** will push a `message` event containing only **`conversationId` + `messageId`** before streaming business starts, for client persistence and subsequent `stop` calls.

When parsing `message` on the client, `data` should be JSON parsed (the entire frame is a string).

---

<a id="websocket-endpoint"></a>
## WebSocket Endpoint

| URL Pattern | Description |
|--------------|-------------|
| `ws://{host}:{port}/omniasst-sysagent` | Direct to application |
| `ws://{gateway_host}/omniasst-sysagent` | Via gateway (`lb:ws://...`) |

Registration class: `IWebSocketConfigurer`.

### Handshake and Request Headers

Handshake interceptor `IHandshakeInterceptor` reads device-related information from HTTP request headers and writes to session attributes (mapping of header fields to attributes is based on source code).

### Message Format

- Payload is **JSON**, outer structure is `IMessage` (contains fields like `eventType`; see source code for details).
- Server dispatches based on `eventType`; **illegal or unknown events** will return a unified error event frame (e.g., `ILLEGAL_ARGUMENT`, `UNSUPPORTED_OPERATION`, `GENERAL_ERROR`).

### Business Events Client Can Send

Validation logic requires the `eventType` in JSON to exactly match the enum **`IMessageEvent.name()`**.

| eventType | Behavior |
|-----------|----------|
| `BUILD_RTC_TOKEN` | Issue RTC Token; server pushes back `BUILD_RTC_TOKEN_RESULT` |
| `TICKET_RESULT` | Bind user and session for scenarios like movie tickets where server pushes downstream (see `IWebSocketSendHandler`) |

### Server Error / Push Events (`IMessageEvent` Enum Values)

Common downstream: `GENERAL_ERROR`, `ILLEGAL_ARGUMENT`, `UNSUPPORTED_OPERATION`, `BUILD_RTC_TOKEN_RESULT`. For complete enum, see source code **`IMessageEvent`** (where `TICKET_RESULT`, `BUILD_RTC_TOKEN`, etc. are **upstream** client event names).

---

<a id="notes"></a>
## Notes

- When connection closes, WebSocket implementation will **clear** session mappings in `IWebSocketSendHandler` (see `afterConnectionClosed`).
- Business also collaborates with gRPC, news WebSocket, **hojoai-webmvc-omniasst-management** (Feign `ConversationAPI`), etc.; for details, see each `*ServiceImpl` and configuration.
- This repository also has HTTP modules for movies, payment callbacks, push notifications, etc.; paths are based on `@RequestMapping` of each `*Controller`; this document focuses on **conversation SSE** and **WebSocket control plane**.