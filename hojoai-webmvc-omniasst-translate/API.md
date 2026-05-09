# hojoai-webmvc-omniasst-translate — API Documentation

## Base Address

- **context-path**: `/omniasst-translate`
- Via gateway: `{gateway}/omniasst-translate/...`

---

## Translation Conversation — `/v0/translation/conversations`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v0/translation/conversations/list` | Translation conversation list |
| POST | `/v0/translation/conversations/messages/list` | Message list within conversation |

Implementation class: `TranslationController`.

---

## Inter-Service Calls (Feign, Non-External APIs)

The following interfaces are **called by this service** to other microservices and generally **should not** be accessed directly from the terminal through this module to replace the original service:

| Client | Method Path (relative to called service) | Description |
|--------|------------------------------------------|-------------|
| `VoiceBaseServiceClient` | POST `/v1/asr/realtime`, `/v1/vad`, `/v1/translate`, `/v1/nlu/translate` | Voice base capabilities |
| `ManagementServiceClient` | POST `/v0/voice/clone`, `/v0/push`, `/v0/conversations/conversation/message/push` | Management domain |
| `TitleGenerationModelClient` | POST `/v1/generateTitle` | Title generation model |