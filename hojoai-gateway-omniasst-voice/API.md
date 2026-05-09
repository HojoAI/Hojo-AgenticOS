# hojoai-gateway-omniasst-voice — API Documentation

## Base URL

- **Servlet context-path**: `/omniasst-voice` (see `application.properties`)
- When accessed through the Omniasst gateway, the full prefix is generally: `{gateway_host}/omniasst-voice`

All **HTTP** paths below are relative to the **context-path** (e.g., `POST /omniasst-voice/v1/asr`).

**WebSocket** upgrade URLs are also under the context-path (e.g., `ws://{host}:{port}/omniasst-voice/asr/v1`).

---

## WebSocket (Persistent Connection)

Registration paths are defined in `WebSocketConfig`; handshake is handled by `WebSocketHandshakeInterceptor`, which can write device and user information into session attributes (**not intercepted**, always passes through).

| Purpose | Path (Relative to Context-Path) | Description |
|---------|--------------------------------|-------------|
| General TTS | `/common/tts` | Feign TTS, Doubao long connection, queue and OSS branches, see `TtsWebSocketHandler` |
| ASR Translation | `/asr/translate` | Translation long connection |
| ASR v1 | `/asr/v1` | Event-based ASR: selects service registered in `AsrServiceFactory` by `platform` |
| Headphone ASR v1 | `/asr/v1/headphone` | Same event model as v1, recognition always uses `HOJO_HEADPHONE` (platform code `1`), includes empty text window and `RESP_VAD_TIMEOUT` logic, see `HeadphoneAsrWebSocketHandler` |

### Handshake Parameters (Optional)

- **Headers (Priority)**: `X-Device-Id`, `X-User-Id` (constants in `WebSocketConstants`)
- **Query**: `deviceId`, `userId` (supplemented when Headers are missing)

### ASR v1 / Headphone v1 Message Envelope

Text frames are JSON with root fields consistent with `IMessage`:

- `event`: Event name string (see `AsrMessageEvent` table below)
- `payload`: Business outer layer; when parsing, read specific DTO from `payload.pdata` (see `IMessageCustom.parseBusinessRequest`)

Downstream business body is also wrapped in `payload.pdata` (`IMessageCustom.newInstanceCustom`).

### ASR Events (`AsrMessageEvent`)

| Direction | `event` Value | Description |
|-----------|---------------|-------------|
| Upstream | `REQ_HEARTBEAT` | Heartbeat request, `pdata` is `HeartbeatRequest` |
| Upstream | `REQ_ASR_CONFIG` | ASR configuration, `pdata` is `AsrConfigRequest` |
| Upstream | `REQ_ASR_AUDIO_DATA` | Audio data, `pdata` is `AsrAudioDataRequest` |
| Downstream | `BACK_HEARTBEAT` | Heartbeat response, `pdata` is `HeartbeatResponse` |
| Downstream | `BACK_ASR_CONFIG_ACK` | Configuration acknowledgment, `pdata` is `AsrConfigResponse` |
| Downstream | `RESP_ASR_RESULT` | Recognition result, `pdata` is `AsrResult` (fields include `fixedText`, `tmpText`, `text`, `isEnd`, `custom`) |
| Downstream | `RESP_VAD_TIMEOUT` | Headphone channel: sent after empty text detection window timeout, `pdata` contains `message` (e.g., "No valid speech detected within 5 seconds"); window duration and strategy see constants in `HeadphoneAsrWebSocketHandler` |
| Downstream | `BACK_ERROR` | Error, `pdata` is `ErrorResponse` (`errorCode`, `errorMessage`, `stage`) |

### Configuration and Platform (`AsrConfigRequest` / `AsrPlatform`)

`AsrConfigRequest` fields:

- `platform`: Integer, default `0`. Currently registered: `0` = HOJO (`HojoAsrServiceImpl`), `1` = HOJO_HEADPHONE (`HojoHeadphoneAsrServiceImpl`)
- `language`: Optional; defaults to `chinese` when `platform == 0`, otherwise defaults to `zh-CN`
- `type`: Audio type, default `pcm`

`BACK_ASR_CONFIG_ACK` corresponds to `AsrConfigResponse`: `status`, `message`, `platform`, `language`, `type`, and **`headphoneMode`**:

- Always `false` on `/asr/v1`
- Always `true` on `/asr/v1/headphone`

### Audio Packet (`AsrAudioDataRequest`)

- `audioData`: Audio payload string (encoding depends on ASR implementation and client agreement, commonly Base64)
- `custom`: Optional passthrough
- `isEnd`: Whether this is the end of the current round, default `false`

You must receive a successful `BACK_ASR_CONFIG_ACK` before sending `REQ_ASR_AUDIO_DATA`, otherwise `BACK_ERROR` will be returned (stage `asr`, prompting to configure first).

### `/asr/v1` vs `/asr/v1/headphone` Summary

| Item | `/asr/v1` | `/asr/v1/headphone` |
|------|-----------|---------------------|
| Recognition Service | `AsrServiceFactory.getService(platform)`, resolves by configured platform | Fixed `getService(1)`, i.e., `HOJO_HEADPHONE` |
| `headphoneMode` | `false` | `true` |
| Additional Downstream | None | May send `RESP_VAD_TIMEOUT` (empty text window logic, see `HeadphoneAsrWebSocketHandler`) |

### Headphone Channel ASR (`/asr/v1/headphone`) Detailed Description

#### Relationship with General `/asr/v1`

- **Event names, `payload.pdata` structure** are **completely identical** to **`/asr/v1`**, clients can reuse the same parsing code; differences are that **recognition implementation is fixed for headphone platform**, **`BACK_ASR_CONFIG_ACK.headphoneMode` is always `true`**, and headphone side **empty text detection** may send **`RESP_VAD_TIMEOUT`**.
- Upgrade URL example (direct connection, default port): `ws://{host}:12002/omniasst-voice/asr/v1/headphone`; when accessing through Omniasst gateway, append **`/omniasst-voice`** prefix after the gateway host.

#### Recommended Message Sequence (Client State Machine)

1. (Optional) Upstream **`REQ_HEARTBEAT`**, downstream **`BACK_HEARTBEAT`**, used for connection keep-alive.
2. Upstream **`REQ_ASR_CONFIG`** (`pdata`: `AsrConfigRequest`). The server still parses `language`, `type`, etc.; **actual recognition** always uses **`AsrPlatform.HOJO_HEADPHONE`** (platform code **`1`**), not strongly coupled with the `platform` field in the request (platform in config ACK is based on session state).
3. After receiving **`BACK_ASR_CONFIG_ACK`** and determining business success, send upstream **`REQ_ASR_AUDIO_DATA`**; otherwise the server returns **`BACK_ERROR`** (stage **`asr`**, prompting to configure first).
4. Continuously send business audio upstream; downstream is mainly **`RESP_ASR_RESULT`** (`pdata`: `AsrResult`, focus on `text` / `tmpText` / `fixedText`, `isEnd`, `custom`).
5. Handle **`RESP_VAD_TIMEOUT`**: indicates a prompt triggered when accumulated recognition text in the **empty text detection window** remains empty after multiple callbacks (see next section); client can use this to end the current round of voice capture, prompt user to speak again, or return to standby UI. **Current implementation** does **not automatically close** the WebSocket after sending this event; whether to reconfigure or disconnect is determined by client strategy.

#### `RESP_VAD_TIMEOUT` and Source Code Constants (Implementation Details)

Logic in **`HeadphoneAsrWebSocketHandler`**:

- **`EMPTY_ASR_TEXT_TIMEOUT_MS`**: Single scheduling interval for empty text detection (current source code is **`10_000`** ms). Within the window, accumulates length of `AsrResult.getText()` after trim; if accumulated length is still 0 when window expires, **`RESP_VAD_TIMEOUT`** is sent; if non-empty text has appeared, reset to zero and **restart** the next window timer.
- Downstream **`pdata`** is currently **`{"message":"No valid speech detected within 5 seconds"}`** (fixed message; literal value may not equal the millisecond constant above, refer to product display).
- There are also **`VAD_TIMEOUT_WINDOW_MS` (30s)** and other timeout judgments related to first packet audio within the class; **some versions of the source code may have these commented out**; refer to the actual logic in `handleAsrAudioData` on the current branch.

#### Integration with sysagent "Headphone Agent"

The voice side is only responsible for **streaming recognition**; dialogue memory and "daily single-session" are handled by the **sysagent** embedded headphone agent under HTTP **`headphoneMode: true`**. Integration recommendations:

- Handshake carries **`X-User-Id`** (or Query `userId`), consistent with sysagent **`X-User-Id` / `inputs.user_id`**.
- Recognition text is used as one round of user input to call **`POST /omniasst-sysagent/v0/conversation/chat`**, with **`headphoneMode: true`** in the body, and consistent **`inputs.user_now_date`** (if using daily session strategy).

See **[hojoai-websocket-omniasst-sysagent/API.md](../hojoai-websocket-omniasst-sysagent/API.md)** "Headphone Agent" section.

---

## Text-to-Speech TTS — `/v1/tts`

| Method | Path | Content-Type / Description |
|--------|------|---------------------------|
| POST | `/v1/tts/file` | Synthesize audio file |
| POST | `/v1/tts/oss` | Synthesize and upload to OSS |
| POST | `/v1/tts/stream` | SSE streaming synthesis (`text/event-stream`) |
| POST | `/v1/tts/pcm/stream` | PCM binary stream (`application/octet-stream`) |

---

## Automatic Speech Recognition ASR — `/v1/asr`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/asr` | Default recognition |
| POST | `/v1/asr/stream` | SSE streaming recognition |
| POST | `/v1/asr/file` | `multipart/form-data` file upload recognition |
| POST | `/v1/asr/realtime` | Real-time recognition |

Streaming/realtime capabilities can also be accessed via WebSocket **`/asr/v1` or `/asr/v1/headphone`** using the event protocol described above.

---

## Alibaba Cloud ASR — `/v1/asr/aliyun`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/asr/aliyun/realtime` | Alibaba Cloud real-time ASR |
| POST | `/v1/asr/aliyun/wav` | WAV and other format recognition |

---

## VAD — `/v1/vad`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/vad` | Voice Activity Detection |

---

## Voiceprint — `/v1/voiceprint`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/voiceprint/register` | `multipart/form-data` registration |
| POST | `/v1/voiceprint/list` | Voiceprint list |
| DELETE | `/v1/voiceprint/{userId}` | Delete |
| POST | `/v1/voiceprint/recognize` | Recognize |
| POST | `/v1/voiceprint/recognize/file` | `multipart/form-data` file recognition |

---

## Translation / NLU — `/v1`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/translate` | Translation |
| POST | `/v1/nlu/translate` | NLU translation |

---

## Voice Cloning — `/v1/voice`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/voice/clone` | `multipart/form-data` cloning |

---

## Downstream Feign (Not External Paths of This Service)

This service uses Feign to call external ASR/TTS etc. (e.g., `AsrAPI`'s `/asr_stream`, `/asr_trans`, `/vad`), specified by config properties `ai.asr.host` etc., **clients should not call directly**.