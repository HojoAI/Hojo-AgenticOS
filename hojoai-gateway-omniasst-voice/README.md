# hojoai-gateway-omniasst-voice

**Omniasst Voice Capability Service**: Provides HTTP interfaces for TTS, ASR, VAD, voiceprint, translation/NLU, voice cloning, and more. Also provides long-connection capabilities via WebSocket including general TTS, ASR translation, ASR v1 (event-based recognition), and headphone-specific ASR v1.

## Capabilities

### HTTP Interfaces
- TTS Speech Synthesis: file synthesis, SSE streaming, PCM binary stream, OSS upload
- ASR Speech Recognition: standard recognition, SSE streaming, file upload, real-time recognition
- Aliyun ASR: Alibaba Cloud real-time ASR and WAV format recognition
- VAD: Voice Activity Detection
- Voiceprint: registration, query, deletion, recognition (standard + file mode)
- Translation/NLU: translation and NLU translation interfaces
- Voice Cloning: clone voice timbre via audio file

### WebSocket Long Connections
- `/common/tts`: General TTS (Feign/Doubao long connection/queue, etc.)
- `/asr/translate`: ASR translation long connection
- `/asr/v1`: ASR v1 selects implementation by platform (HOJO/HOJO_HEADPHONE)
- `/asr/v1/headphone`: Headphone ASR, always uses HOJO_HEADPHONE, includes empty text timeout logic

### ASR v1 Event Protocol
- `REQ_ASR_CONFIG` → `BACK_ASR_CONFIG_ACK` → `REQ_ASR_AUDIO_DATA` → `RESP_ASR_RESULT`
- Headphone channel additionally supports `RESP_VAD_TIMEOUT` (empty text window timeout)

## Tech Stack

- Java 21, Spring Boot 3.x
- Spring Web / WebSocket, WebFlux (for SSE scenarios)
- Nacos; external ASR/TTS addresses specified via configuration items

## Running Conventions

| Item | Description |
|------|------|
| Runnable Module | `hojoai-gateway-omniasst-voice-server` |
| Default HTTP Port | `12002` |
| Servlet Context Path | `/omniasst-voice` |
| Via Omniasst Gateway | Prefix is generally `{gateway}/omniasst-voice` |

## WebSocket Paths (relative to context-path)

Same as HTTP, all under **`/omniasst-voice`** (upgrade URL example: `ws://{host}:12002/omniasst-voice/asr/v1`).

| Path | Description |
|------|------|
| `/common/tts` | General TTS (Feign / Doubao long connection / queue, etc.) |
| `/asr/translate` | ASR translation long connection |
| `/asr/v1` | ASR v1: selects `AsrServiceFactory` implementation by `platform` |
| `/asr/v1/headphone` | Headphone ASR: event model same as v1, recognition always uses `HOJO_HEADPHONE`; `BACK_ASR_CONFIG_ACK.headphoneMode=true`; may receive `RESP_VAD_TIMEOUT` (empty text window, see `HeadphoneAsrWebSocketHandler`) |

Handshake optionally carries `X-Device-Id`, `X-User-Id` (or Query `deviceId`, `userId`). Event names and `payload.pdata` conventions are described in [API.md](./API.md).

### Headphone Agent ASR Channel (Description)

**`/asr/v1/headphone`** and **`/asr/v1`** share the same JSON event protocol (`REQ_ASR_CONFIG` → `BACK_ASR_CONFIG_ACK` → `REQ_ASR_AUDIO_DATA` → `RESP_ASR_RESULT`), with differences in recognition chain and headphone-specific empty text timeout logic. When used with **sysagent's** headphone agent chain (`headphoneMode: true`), it is recommended to align user ID and business day (`user_now_date`) on both ends. Recommended message sequence, `RESP_VAD_TIMEOUT` behavior, and constant descriptions are in the **[API.md](./API.md)** "Headphone Channel ASR" section.

## Module Structure

```
hojoai-gateway-omniasst-voice/
├── pom.xml
├── API.md                                  # External HTTP + WebSocket paths and ASR v1 protocol summary
├── README.md
├── document/                               # Detailed protocol and scenario descriptions (Chinese)
└── hojoai-gateway-omniasst-voice-server/   # Spring Boot application
    └── src/main/java/.../voice/
        ├── controller/                     # HTTP API
        ├── configure/                      # WebSocket registration, etc.
        ├── websocket/                      # TTS / translation / ASR v1 Handler
        └── service/                      # TTS, ASR, voiceprint, and other business services
```

## Build and Run

```bash
cd hojoai-gateway-omniasst-voice
mvn clean install
cd hojoai-gateway-omniasst-voice-server && mvn spring-boot:run
```

Environment-related configuration is in `hojoai-gateway-omniasst-voice-server/src/main/resources/config/application-dev.properties`.

## Documentation

| Document | Content |
|------|------|
| [API.md](./API.md) | **HTTP** (`/v1/tts`, `/v1/asr`, etc.) and **WebSocket** paths, ASR v1 event and configuration field summary |
| [document/语音服务HTTP接口文档.md](./document/语音服务HTTP接口文档.md) | HTTP interface details |
| [document/语音服务长连接接口文档.md](./document/语音服务长连接接口文档.md) | Long connection and message body examples |