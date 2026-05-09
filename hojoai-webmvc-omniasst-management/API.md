# hojoai-webmvc-omniasst-management — API Documentation

## Base Address

- **context-path**: `/omniasst-management/api`
- When accessed through **hojoai-gateway-omniasst**, the full URL must include the context-path, e.g., `{gateway}/omniasst-management/api/v0/conversations/...` (gateway matches `/omniasst-management/**`)

The paths in the table below are **Servlet paths after removing context-path** (consistent with `@RequestMapping`).

---

## Conversation — `/v0/conversations`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v0/conversations/id` | Create/get conversation ID |
| POST | `/v0/conversations/message` | Single message |
| POST | `/v0/conversations/messages` | Batch messages |
| POST | `/v0/conversations/list` | Conversation list |
| POST | `/v0/conversations/messages/list` | Message list |
| POST | `/v0/conversations/message/replace` | Replace message |
| POST | `/v0/conversations/message/delete` | Delete message |
| POST | `/v0/conversations/conversation/delete` | Delete conversation |
| GET | `/v0/conversations/conversation/title` | Conversation title |
| POST | `/v0/conversations/conversation/message/push` | Message push |
| GET | `/v0/conversations/all-read` | Mark all as read |
| POST | `/v0/conversations/message/extra/update` | Update extra field |

## Virtual Avatar — `/v0/avatars`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/v0/avatars/list` | List |
| GET | `/v0/avatars/current` | Current avatar |
| GET | `/v0/avatars/switch/{avatarId}` | Switch |
| GET | `/v0/avatars/{avatarId}` | Details |

## Debug — `/v0/debug`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v0/debug/fcm-push` | FCM push debug |

## Voice Config v1 — `/v1/voice`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/v1/voice/profile` | Voice config profile |
| DELETE | `/v1/voice/deregister` | Deregister (`application/json`) |

## Voiceprint v0 — `/v0/voiceprint`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v0/voiceprint/match` | Match |
| POST | `/v0/voiceprint/register` | `multipart/form-data` registration |
| POST | `/v0/voiceprint/list` | List |
| DELETE | `/v0/voiceprint/{userId}` | Delete |
| GET | `/v0/voiceprint/username` | Username |
| POST | `/v0/voiceprint/display-name` | Display name |

## Voice v0 — `/v0/voice`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v0/voice/clone` | `multipart/form-data` clone |
| GET | `/v0/voice/profile` | Profile |
| DELETE | `/v0/voice/profile` | Delete profile |
| POST | `/v0/voice/save` | Save config |

## Meeting Voiceprint — `/v1/meeting/voiceprint/register`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/meeting/voiceprint/register/copy-voice` | JSON |
| POST | `/v1/meeting/voiceprint/register/prepare` | `multipart/form-data` |
| POST | `/v1/meeting/voiceprint/register/commit` | JSON |

## Meeting — `/v0/meeting`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v0/meeting/list` | List |
| GET | `/v0/meeting/{meetingId}` | Details |

## Push — `/v0`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v0/push` | Push |
| POST | `/v0/device-token` | Device token report |

## Object Storage — `/v0/storage`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/v0/storage/presign` | Presign |
| GET | `/v0/storage/extend-sign` | Extend sign |
| GET | `/v0/storage/append-upload` | Append upload |

## Message Center — `/v0/message-center`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/v0/message-center/messages` | Message list |
| GET | `/v0/message-center/messages/unread-count` | Unread count |
| POST | `/v0/message-center/messages/{messageId}/read` | Mark single as read |
| POST | `/v0/message-center/messages/read` | Batch mark as read |
| POST | `/v0/message-center/messages/clear` | Clear all |
| GET | `/v0/message-center/tasks/in-progress` | In-progress tasks |
| GET | `/v0/message-center/tasks/in-progress/count` | Task count |
| POST | `/v0/message-center/messages/create` | Create message |
| POST | `/v0/message-center/tasks/create` | Create task |
| POST | `/v0/message-center/tasks/{taskId}/complete` | Complete task |

## Translation Conversation — `/v0/translation/conversations`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v0/translation/conversations/list` | Conversation list |
| POST | `/v0/translation/conversations/messages/list` | Message list |

## Recommendations — `/v0/recommendations`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/v0/recommendations` | Get recommendations |
| POST | `/v0/recommendations` | Submit recommendation feedback |
| POST | `/v0/recommendations/{recommendationId}/ignore` | Ignore |

> Note: This service also contains Feign clients for **Voice Gateway** (e.g., `/v1/voice/clone`), which are internal calls and not external API paths of this service.