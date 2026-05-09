# hojoai-webmvc-omniasst-meeting — API 文档

## 基址

- **context-path**：`/omniasst-meeting`
- **默认端口**：`12007`
- 经网关：`{网关}/omniasst-meeting/...`

以下路径均相对于 **context-path**。

---

## 会议管理 — `/v0/meeting`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v0/meeting/list` | 分页查询会议列表（需 `userId`） |
| GET | `/v0/meeting/{meetingId}` | 会议详情聚合（含参会人、段落等） |
| POST | `/v0/meeting/todo/update-status` | 更新待办项状态 |
| POST | `/v0/meeting/todo/complete` | 待办完成/取消完成（`completed` 0 或 1） |

实现类：`MeetingController`。

---

## 会议编辑 — `/v1/meeting`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v1/meeting/detail/mark`（别名 `/v1/meeting/segment/mark`） | 时间轴打标 |
| POST | `/v1/meeting/speaker/rename` | 批量重命名说话人展示名 |

实现类：`MeetingV1Controller`。详见 [参会人与发言人接口说明](document/会议REST-参会人与发言人接口说明.md)、[打标说明](document/打标.md)。

---

## 会议导入 — `/v1/meeting/import`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v1/meeting/import/register-from-oss` | 登记 OSS 已有音频，启动异步转写 |
| POST | `/v1/meeting/import/batch-init` | 批量创建导入任务，返回 OSS 直传预签名（≤5 文件） |
| POST | `/v1/meeting/import/upload-complete` | 客户端直传 OSS 完成回调 |
| POST | `/v1/meeting/import/tasks/list` | 查询处理中的导入任务队列 |
| GET | `/v1/meeting/import/processing-count` | 处理中任务数量（首页 Banner） |
| POST | `/v1/meeting/import/read` | 标记任务已读 |

实现类：`MeetingImportController`。详见 [会议导入接口说明](document/会议导入-v1-register-from-oss-接口说明.md)。

---

## 音色克隆 — `/v1/meeting/voice`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/v1/meeting/voice/profile` | 查询音色/声纹资料（`type`: 0=声音, 1=声纹） |
| POST | `/v1/meeting/voice/clone` | 上传 WAV 克隆音色（`multipart/form-data`） |
| POST | `/v1/meeting/voice/save` | 保存已克隆的音色（`application/json`） |

实现类：`MeetingVoiceController`（BFF，转发至 Management）。详见 [音色克隆接口文档](document/音色克隆接口文档.md)。

---

## 声纹注册 — `/v1/meeting/voice/register`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v1/meeting/voice/register/prepare` | 两阶段注册 · 阶段一：上传音频 |
| POST | `/v1/meeting/voice/register/commit` | 两阶段注册 · 阶段二：提交昵称完成注册 |

实现类：`VoiceRegisterBffController`（BFF，转发至 Management）。详见 [个人声纹注册接口文档](document/个人声纹注册接口文档.md)。

---

## WebSocket — 实时会议转写

| 项 | 说明 |
|----|------|
| 端点 | `ws://{host}:12007/omniasst-meeting/meeting` |
| 连接参数 | 请求头 `X-Device-Id`（必填）、`X-User-Id`；或 URL 查询参数 `deviceId`、`userId` |
| 续接会议 | 请求头 `X-Meeting-Id` 或查询参数 `meetingId` |
| 心跳 | 客户端每 10 s 发送 `REQ_HEARTBEAT`，30 s 无心跳断开 |
| 消息格式 | `{"event": "...", "payload": {"pdata": {...}}}` |

### 客户端事件

| 事件 | 说明 |
|------|------|
| `REQ_HEARTBEAT` | 心跳 |
| `REQ_MEETING_AUDIO_DATA` | 音频帧（Base64 PCM 16kHz/16bit/mono） |
| `REQ_MEETING_PAUSE` | 暂停 |
| `REQ_MEETING_RESUME` | 恢复 |
| `REQ_MEETING_STOP` | 停止（`needSave`: 0=不保存, 1=保存） |
| `REQ_MEETING_MARK` | 时间轴打标 |

### 服务端事件

| 事件 | 说明 |
|------|------|
| `BACK_MEETING_ID` | 会议 ID 与开始时间 |
| `BACK_MEETING_ASR_TEXT` | ASR 实时识别结果 |
| `BACK_MEETING_SEGMENT_COMPLETE` | 段落完成（含说话人、时间轴） |
| `BACK_HEARTBEAT` | 心跳响应 |
| `BACK_MEETING_PAUSE_ACK` | 暂停确认 |
| `BACK_MEETING_RESUME_ACK` | 恢复确认 |
| `BACK_MEETING_STOP_ACK` | 停止确认 |
| `BACK_MEETING_MARK_ACK` | 打标确认 |
| `BACK_ERROR` | 错误（含 errorCode、stage） |

详见 [WebSocket 接口文档](document/会议场景WebSocket接口文档.md)。

---

## 统一响应格式

所有 REST 接口返回 `Response<T>`：

| 字段 | 类型 | 说明 |
|------|------|------|
| resCode | String | `OK` 表示成功 |
| resMessage | String | 描述信息 |
| resContent | T | 业务数据 |

---

## 服务间调用（非对外接口）

| 被调服务 | 说明 |
|----------|------|
| Voice 基础服务 | ASR、VAD、声纹识别 |
| Management 服务 | 音色克隆/保存、声纹注册、展示名管理 |
