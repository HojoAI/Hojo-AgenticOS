# hojoai-webmvc-omniasst-meeting — API Documentation

## Base Address

- **context-path**: `/omniasst-meeting`
- **Default port**: `12007`
- Via gateway: `{gateway}/omniasst-meeting/...`

All paths below are relative to **context-path**.

---

## Meeting Management — `/v0/meeting`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v0/meeting/list` | Paginated meeting list query (requires `userId`) |
| GET | `/v0/meeting/{meetingId}` | Meeting details aggregation (includes participants, segments, etc.) |
| POST | `/v0/meeting/todo/update-status` | Update todo item status |
| POST | `/v0/meeting/todo/complete` | Todo complete/uncomplete (`completed` 0 or 1) |

Implementation class: `MeetingController`.

---

## Meeting Editing — `/v1/meeting`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/meeting/detail/mark` (alias `/v1/meeting/segment/mark`) | Timeline marking |
| POST | `/v1/meeting/speaker/rename` | Batch rename speaker display names |

Implementation class: `MeetingV1Controller`. See [Participant and Speaker API说明](document/会议REST-参会人与发言人接口说明.md), [Marking说明](document/打标.md).

---

## Meeting Import — `/v1/meeting/import`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/meeting/import/register-from-oss` | Register existing audio in OSS, start async transcription |
| POST | `/v1/meeting/import/batch-init` | Batch create import tasks, return OSS direct upload presigned URL (max 5 files) |
| POST | `/v1/meeting/import/upload-complete` | Client direct upload OSS completion callback |
| POST | `/v1/meeting/import/tasks/list` | Query processing import task queue |
| GET | `/v1/meeting/import/processing-count` | Processing task count (homepage Banner) |
| POST | `/v1/meeting/import/read` | Mark task as read |

Implementation class: `MeetingImportController`. See [Meeting Import API说明](document/会议导入-v1-register-from-oss-接口说明.md).

---

## Voice Cloning — `/v1/meeting/voice`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/v1/meeting/voice/profile` | Query voice/voiceprint profile (`type`: 0=voice, 1=voiceprint) |
| POST | `/v1/meeting/voice/clone` | Upload WAV for voice cloning (`multipart/form-data`) |
| POST | `/v1/meeting/voice/save` | Save cloned voice (`application/json`) |

Implementation class: `MeetingVoiceController` (BFF, forwards to Management). See [Voice Cloning API文档](document/音色克隆接口文档.md).

---

## Voiceprint Registration — `/v1/meeting/voice/register`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/meeting/voice/register/prepare` | Two-phase registration - Phase 1: Upload audio |
| POST | `/v1/meeting/voice/register/commit` | Two-phase registration - Phase 2: Submit nickname to complete registration |

Implementation class: `VoiceRegisterBffController` (BFF, forwards to Management). See [Personal Voiceprint Registration API文档](document/个人声纹注册接口文档.md).

---

## WebSocket — Real-time Meeting Transcription

| Item | Description |
|------|-------------|
| Endpoint | `ws://{host}:12007/omniasst-meeting/meeting` |
| Connection Parameters | Request headers `X-Device-Id` (required), `X-User-Id`; or URL query parameters `deviceId`, `userId` |
| Resume Meeting | Request header `X-Meeting-Id` or query parameter `meetingId` |
| Heartbeat | Client sends `REQ_HEARTBEAT` every 10s; disconnected after 30s without heartbeat |
| Message Format | `{"event": "...", "payload": {"pdata": {...}}}` |

### Client Events

| Event | Description |
|-------|-------------|
| `REQ_HEARTBEAT` | Heartbeat |
| `REQ_MEETING_AUDIO_DATA` | Audio frame (Base64 PCM 16kHz/16bit/mono) |
| `REQ_MEETING_PAUSE` | Pause |
| `REQ_MEETING_RESUME` | Resume |
| `REQ_MEETING_STOP` | Stop (`needSave`: 0=do not save, 1=save) |
| `REQ_MEETING_MARK` | Timeline marking |

### Server Events

| Event | Description |
|-------|-------------|
| `BACK_MEETING_ID` | Meeting ID and start time |
| `BACK_MEETING_ASR_TEXT` | ASR real-time recognition result |
| `BACK_MEETING_SEGMENT_COMPLETE` | Segment complete (includes speaker, timeline) |
| `BACK_HEARTBEAT` | Heartbeat response |
| `BACK_MEETING_PAUSE_ACK` | Pause acknowledgment |
| `BACK_MEETING_RESUME_ACK` | Resume acknowledgment |
| `BACK_MEETING_STOP_ACK` | Stop acknowledgment |
| `BACK_MEETING_MARK_ACK` | Mark acknowledgment |
| `BACK_ERROR` | Error (includes errorCode, stage) |

See [WebSocket API文档](document/会议场景WebSocket接口文档.md).

---

## Unified Response Format

All REST interfaces return `Response<T>`:

| Field | Type | Description |
|-------|------|-------------|
| resCode | String | `OK` indicates success |
| resMessage | String | Description |
| resContent | T | Business data |

---

## Inter-Service Calls (Non-External APIs)

| Called Service | Description |
|----------------|-------------|
| Voice Base Service | ASR, VAD, voiceprint recognition |
| Management Service | Voice cloning/saving, voiceprint registration, display name management |