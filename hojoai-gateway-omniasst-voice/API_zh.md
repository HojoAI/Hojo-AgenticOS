# hojoai-gateway-omniasst-voice — API 文档

## 基址

- **Servlet context-path**：`/omniasst-voice`（见 `application.properties`）
- 经 Omniasst 网关访问时，完整前缀一般为：`{网关主机}/omniasst-voice`

以下 **HTTP** 路径均相对于 **context-path**（例如：`POST /omniasst-voice/v1/asr`）。

**WebSocket** 升级 URL 同样挂在 context-path 下（例如：`ws://{主机}:{端口}/omniasst-voice/asr/v1`）。

---

## WebSocket（长连接）

注册路径见 `WebSocketConfig`；握手经 `WebSocketHandshakeInterceptor`，可将终端与用户写入会话属性（**不拦截**，始终放行）。

| 用途 | 路径（相对 context-path） | 说明 |
|------|---------------------------|------|
| 通用 TTS | `/common/tts` | Feign TTS、豆包长连接、队列与 OSS 等分支，见 `TtsWebSocketHandler` |
| ASR 翻译 | `/asr/translate` | 翻译类长连接 |
| ASR v1 | `/asr/v1` | 事件化 ASR：按 `platform` 选择 `AsrServiceFactory` 中注册的服务 |
| 耳机 ASR v1 | `/asr/v1/headphone` | 与 v1 相同事件模型，识别固定走 `HOJO_HEADPHONE`（平台码 `1`）实现，含空文本窗口与 `RESP_VAD_TIMEOUT` 等逻辑，见 `HeadphoneAsrWebSocketHandler` |

### 握手参数（可选）

- **Header（优先）**：`X-Device-Id`、`X-User-Id`（常量见 `WebSocketConstants`）
- **Query**：`deviceId`、`userId`（Header 缺省时补全）

### ASR v1 / 耳机 v1 消息外壳

文本帧为 JSON，根字段与 `IMessage` 一致：

- `event`：事件名字符串（见下表 `AsrMessageEvent`）
- `payload`：业务外层；解析时从 `payload.pdata` 读取具体 DTO（见 `IMessageCustom.parseBusinessRequest`）

**下行**业务体同样包在 `payload.pdata` 中（`IMessageCustom.newInstanceCustom`）。

### ASR 事件一览（`AsrMessageEvent`）

| 方向 | `event` 值 | 说明 |
|------|-------------|------|
| 上行 | `REQ_HEARTBEAT` | 心跳请求，`pdata` 为 `HeartbeatRequest` |
| 上行 | `REQ_ASR_CONFIG` | ASR 配置，`pdata` 为 `AsrConfigRequest` |
| 上行 | `REQ_ASR_AUDIO_DATA` | 音频数据，`pdata` 为 `AsrAudioDataRequest` |
| 下行 | `BACK_HEARTBEAT` | 心跳响应，`pdata` 为 `HeartbeatResponse` |
| 下行 | `BACK_ASR_CONFIG_ACK` | 配置确认，`pdata` 为 `AsrConfigResponse` |
| 下行 | `RESP_ASR_RESULT` | 识别结果，`pdata` 为 `AsrResult`（字段含 `fixedText`、`tmpText`、`text`、`isEnd`、`custom`） |
| 下行 | `RESP_VAD_TIMEOUT` | 耳机通道：空文本检测窗口超时后下发，`pdata` 含 `message`（如「5秒内未检测到有效语音」）；窗口时长与策略见 `HeadphoneAsrWebSocketHandler` 内常量 |
| 下行 | `BACK_ERROR` | 错误，`pdata` 为 `ErrorResponse`（`errorCode`、`errorMessage`、`stage`） |

### 配置与平台（`AsrConfigRequest` / `AsrPlatform`）

`AsrConfigRequest` 字段：

- `platform`：整型，默认 `0`。当前注册：`0` = HOJO（`HojoAsrServiceImpl`），`1` = HOJO_HEADPHONE（`HojoHeadphoneAsrServiceImpl`）
- `language`：可选；缺省时 `platform == 0` 默认 `chinese`，否则默认 `zh-CN`
- `type`：音频类型，默认 `pcm`

`BACK_ASR_CONFIG_ACK` 对应 `AsrConfigResponse`：`status`、`message`、`platform`、`language`、`type`，以及 **`headphoneMode`**：

- `/asr/v1` 上恒为 `false`
- `/asr/v1/headphone` 上恒为 `true`

### 音频包（`AsrAudioDataRequest`）

- `audioData`：音频载荷字符串（编码方式与 ASR 实现及客户端约定一致，常见为 Base64）
- `custom`：可选透传
- `isEnd`：是否本轮结束，默认 `false`

须先收到成功的 `BACK_ASR_CONFIG_ACK` 后再发 `REQ_ASR_AUDIO_DATA`，否则会返回 `BACK_ERROR`（阶段 `asr`，提示先配置）。

### `/asr/v1` 与 `/asr/v1/headphone` 差异摘要

| 项目 | `/asr/v1` | `/asr/v1/headphone` |
|------|-----------|---------------------|
| 识别服务 | `AsrServiceFactory.getService(platform)`，按配置平台解析 | 固定 `getService(1)`，即 `HOJO_HEADPHONE` |
| `headphoneMode` | `false` | `true` |
| 额外下行 | 无 | 可能下发 `RESP_VAD_TIMEOUT`（空文本窗口逻辑，见 `HeadphoneAsrWebSocketHandler`） |

### 耳机通道 ASR（`/asr/v1/headphone`）详细说明

#### 与通用 `/asr/v1` 的关系

- **事件名、`payload.pdata` 结构**与 **`/asr/v1`** 完全一致，客户端可复用同一套解析代码；差异在于**识别实现固定为耳机平台**、**`BACK_ASR_CONFIG_ACK.headphoneMode` 恒为 `true`**，以及耳机侧**空文本检测**可能下发 **`RESP_VAD_TIMEOUT`**。
- 升级 URL 示例（直连、默认端口）：`ws://{主机}:12002/omniasst-voice/asr/v1/headphone`；经 Omniasst 网关时在网关主机后拼接 **`/omniasst-voice`** 前缀即可。

#### 推荐消息顺序（端侧状态机）

1. （可选）上行 **`REQ_HEARTBEAT`**，下行 **`BACK_HEARTBEAT`**，用于长连接保活。
2. 上行 **`REQ_ASR_CONFIG`**（`pdata`：`AsrConfigRequest`）。服务端仍会解析 `language`、`type` 等；**实际识别**始终走 **`AsrPlatform.HOJO_HEADPHONE`**（平台码 **`1`**），与请求中的 `platform` 字段无强绑定（配置 ACK 中 `platform` 以会话状态为准）。
3. 收到 **`BACK_ASR_CONFIG_ACK`** 且业务判定为成功后，再上行 **`REQ_ASR_AUDIO_DATA`**；否则服务端返回 **`BACK_ERROR`**（阶段 **`asr`**，提示先配置）。
4. 持续上行业务音频；下行以 **`RESP_ASR_RESULT`** 为主（`pdata`：`AsrResult`，关注 `text` / `tmpText` / `fixedText`、`isEnd`、`custom`）。
5. 处理 **`RESP_VAD_TIMEOUT`**：表示在**空文本检测窗口**内多次回调累计的识别文本仍为空时触发的提示（见下节）；端侧可用于结束本轮拾音、提示用户重新开口或回到待机 UI。**当前实现**在发送该事件后**不自动关闭** WebSocket，是否重配、是否断连由客户端策略决定。

#### `RESP_VAD_TIMEOUT` 与源码常量（实现细节）

逻辑见 **`HeadphoneAsrWebSocketHandler`**：

- **`EMPTY_ASR_TEXT_TIMEOUT_MS`**：空文本检测**单次调度**间隔（当前源码为 **`10_000`** ms）。在窗口内对 **`AsrResult.getText()`** 做 trim 后长度**累加**；若窗口到期累加仍为 0，则下发 **`RESP_VAD_TIMEOUT`**；若曾出现非空文本则清零并**重新**启动下一轮窗口计时。
- 下行 **`pdata`** 当前为 **`{"message":"5秒内未检测到有效语音"}`**（固定文案；与上述毫秒常量不必字面相等，以产品展示为准）。
- 类内另有 **`VAD_TIMEOUT_WINDOW_MS`（30s）** 等与首包音频相关的超时判断，**在部分版本源码中可能处于注释状态**；以当前分支 **`handleAsrAudioData`** 实际生效逻辑为准。

#### 与 sysagent「耳机智能体」串联

语音侧仅负责**流式识别**；对话记忆与「按日单会话」由 **sysagent** 内嵌耳机智能体在 HTTP **`headphoneMode: true`** 下完成。联调建议：

- 握手携带 **`X-User-Id`**（或 Query `userId`），与 sysagent **`X-User-Id` / `inputs.user_id`** 一致。
- 识别文本作为一轮用户输入调用 **`POST /omniasst-sysagent/v0/conversation/chat`**，body 中 **`headphoneMode: true`**，并传入一致的 **`inputs.user_now_date`**（若使用按日会话策略）。

详见 **[hojoai-websocket-omniasst-sysagent/API.md](../hojoai-websocket-omniasst-sysagent/API.md)** 中「耳机智能体」章节。

---

## 语音合成 TTS — `/v1/tts`

| 方法 | 路径 | Content-Type / 说明 |
|------|------|---------------------|
| POST | `/v1/tts/file` | 合成音频文件 |
| POST | `/v1/tts/oss` | 合成并上传 OSS 等 |
| POST | `/v1/tts/stream` | SSE 流式合成（`text/event-stream`） |
| POST | `/v1/tts/pcm/stream` | PCM 二进制流（`application/octet-stream`） |

---

## 语音识别 ASR — `/v1/asr`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v1/asr` | 默认识别 |
| POST | `/v1/asr/stream` | SSE 流式识别 |
| POST | `/v1/asr/file` | `multipart/form-data` 上传文件识别 |
| POST | `/v1/asr/realtime` | 实时识别 |

流式/实时能力也可通过上文 **WebSocket `/asr/v1` 或 `/asr/v1/headphone`** 使用事件协议接入。

---

## 阿里云 ASR — `/v1/asr/aliyun`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v1/asr/aliyun/realtime` | 阿里云实时 ASR |
| POST | `/v1/asr/aliyun/wav` | WAV 等格式识别 |

---

## VAD — `/v1/vad`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v1/vad` | 语音活动检测 |

---

## 声纹 — `/v1/voiceprint`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v1/voiceprint/register` | `multipart/form-data` 注册 |
| POST | `/v1/voiceprint/list` | 声纹列表 |
| DELETE | `/v1/voiceprint/{userId}` | 删除 |
| POST | `/v1/voiceprint/recognize` | 识别 |
| POST | `/v1/voiceprint/recognize/file` | `multipart/form-data` 文件识别 |

---

## 翻译 / NLU — `/v1`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v1/translate` | 翻译 |
| POST | `/v1/nlu/translate` | NLU 翻译 |

---

## 声音克隆 — `/v1/voice`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v1/voice/clone` | `multipart/form-data` 克隆 |

---

## 下游 Feign（非本服务对外路径）

本服务通过 Feign 调用外部 ASR/TTS 等（如 `AsrAPI` 的 `/asr_stream`、`/asr_trans`、`/vad`），由配置项 `ai.asr.host` 等指定，**客户端不应直接调用**。
