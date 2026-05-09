# hojoai-webmvc-omniasst-management — API 文档

## 基址

- **context-path**：`/omniasst-management/api`
- 经 **hojoai-gateway-omniasst** 访问时，完整 URL 需包含 context-path，例如：`{网关}/omniasst-management/api/v0/conversations/...`（网关匹配 `/omniasst-management/**`）

下表路径为 **去掉 context-path 之后** 的 Servlet 路径（与 `@RequestMapping` 一致）。

---

## 会话 — `/v0/conversations`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v0/conversations/id` | 创建/获取会话 ID |
| POST | `/v0/conversations/message` | 单条消息 |
| POST | `/v0/conversations/messages` | 批量消息 |
| POST | `/v0/conversations/list` | 会话列表 |
| POST | `/v0/conversations/messages/list` | 消息列表 |
| POST | `/v0/conversations/message/replace` | 替换消息 |
| POST | `/v0/conversations/message/delete` | 删除消息 |
| POST | `/v0/conversations/conversation/delete` | 删除会话 |
| GET | `/v0/conversations/conversation/title` | 会话标题 |
| POST | `/v0/conversations/conversation/message/push` | 消息推送 |
| GET | `/v0/conversations/all-read` | 全部已读 |
| POST | `/v0/conversations/message/extra/update` | 扩展字段更新 |

## 虚拟形象 — `/v0/avatars`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/v0/avatars/list` | 列表 |
| GET | `/v0/avatars/current` | 当前形象 |
| GET | `/v0/avatars/switch/{avatarId}` | 切换 |
| GET | `/v0/avatars/{avatarId}` | 详情 |

## 调试 — `/v0/debug`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v0/debug/fcm-push` | FCM 推送调试 |

## 语音配置 v1 — `/v1/voice`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/v1/voice/profile` | 语音配置档案 |
| DELETE | `/v1/voice/deregister` | 注销（`application/json`） |

## 声纹 v0 — `/v0/voiceprint`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v0/voiceprint/match` | 匹配 |
| POST | `/v0/voiceprint/register` | `multipart/form-data` 注册 |
| POST | `/v0/voiceprint/list` | 列表 |
| DELETE | `/v0/voiceprint/{userId}` | 删除 |
| GET | `/v0/voiceprint/username` | 用户名 |
| POST | `/v0/voiceprint/display-name` | 显示名 |

## 语音 v0 — `/v0/voice`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v0/voice/clone` | `multipart/form-data` 克隆 |
| GET | `/v0/voice/profile` | 档案 |
| DELETE | `/v0/voice/profile` | 删除档案 |
| POST | `/v0/voice/save` | 保存配置 |

## 会议声纹 — `/v1/meeting/voiceprint/register`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v1/meeting/voiceprint/register/copy-voice` | JSON |
| POST | `/v1/meeting/voiceprint/register/prepare` | `multipart/form-data` |
| POST | `/v1/meeting/voiceprint/register/commit` | JSON |

## 会议 — `/v0/meeting`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v0/meeting/list` | 列表 |
| GET | `/v0/meeting/{meetingId}` | 详情 |

## 推送 — `/v0`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v0/push` | 推送 |
| POST | `/v0/device-token` | 设备 Token 上报 |

## 对象存储 — `/v0/storage`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/v0/storage/presign` | 预签名 |
| GET | `/v0/storage/extend-sign` | 延期签名 |
| GET | `/v0/storage/append-upload` | 追加上传 |

## 消息中心 — `/v0/message-center`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/v0/message-center/messages` | 消息列表 |
| GET | `/v0/message-center/messages/unread-count` | 未读数 |
| POST | `/v0/message-center/messages/{messageId}/read` | 单条已读 |
| POST | `/v0/message-center/messages/read` | 批量已读 |
| POST | `/v0/message-center/messages/clear` | 清空 |
| GET | `/v0/message-center/tasks/in-progress` | 进行中任务 |
| GET | `/v0/message-center/tasks/in-progress/count` | 任务数 |
| POST | `/v0/message-center/messages/create` | 创建消息 |
| POST | `/v0/message-center/tasks/create` | 创建任务 |
| POST | `/v0/message-center/tasks/{taskId}/complete` | 完成任务 |

## 翻译会话 — `/v0/translation/conversations`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v0/translation/conversations/list` | 会话列表 |
| POST | `/v0/translation/conversations/messages/list` | 消息列表 |

## 推荐 — `/v0/recommendations`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/v0/recommendations` | 获取推荐 |
| POST | `/v0/recommendations` | 提交推荐相关 |
| POST | `/v0/recommendations/{recommendationId}/ignore` | 忽略 |

> 另：服务内含对 **语音网关** 的 Feign 客户端（如 `/v1/voice/clone`），为内部调用，非本进程对外路径。
