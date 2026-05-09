# hojoai-websocket-omniasst-sysagent — API 文档

## 文档导航

- [概述](#概述)
- [快速接入（最小路径）](#quickstart)
- [HTTP：对话与流式输出](#http对话与流式输出)
- [耳机智能体（`headphoneMode = true`）](#headphone-mode)
- [SSE：事件名与载荷](#sse事件名与载荷)
- [WebSocket 端点](#websocket-端点)
- [说明](#说明)

## 概述

本服务同时提供：

- **HTTP + SSE**：主对话流（`/omniasst-sysagent/v0/conversation/...`），`Content-Type: text/event-stream`。
- **WebSocket**：RTC Token、影票会话绑定等控制面消息（根路径 `/omniasst-sysagent`，与网关路由一致）。

默认 HTTP 端口见 `application.properties`（如 **`12003`**）。**无 servlet context-path** 时，上述路径均位于应用根路径下。

经 **hojoai-gateway-omniasst** 访问时，在网关 `Path` 前缀下拼接 **`/omniasst-sysagent`** 即可（与直连应用相同的路径片段）。

运行约定、环境变量与部署步骤见 **[deploy.md](./deploy.md)**。

<a id="quickstart"></a>
## 快速接入（最小路径）

建议按以下顺序接入：

1. 先调用 `/chat` 打通 SSE 主链路，拿到首帧 `conversationId` + `messageId`。
2. 在客户端保存 `conversationId`，后续继续多轮或调用 `/chat/stop`。
3. 需要语音优先与按日单会话时，在 `/chat` 或 `/rechat` 中启用 `headphoneMode: true`。
4. 需要 RTC Token 或影票会话绑定时，再接入 WebSocket `eventType` 协议。

### 最小 SSE 示例（`/chat`）

```bash
curl -N --location 'http://{host}:12003/omniasst-sysagent/v0/conversation/chat' \
  --header 'Content-Type: application/json' \
  --header 'X-User-Id: 10001' \
  --data '{
    "responseMode": "streaming",
    "contents": [{ "type": "text", "text": "你好，介绍一下你自己" }],
    "inputs": { "user_id": "10001" }
  }'
```

### 最小 WebSocket 上行示例

客户端上行负载为 JSON，`eventType` 必须与 `IMessageEvent.name()` 完全一致，例如：

```json
{
  "eventType": "BUILD_RTC_TOKEN"
}
```

---

## HTTP：对话与流式输出

控制器：`ConversationController`，前缀 **`/omniasst-sysagent/v0/conversation`**。

除特别说明外，请求体为 **JSON**，响应为 **`hojoai.common.response.Response`** 封装（删除/停止等非流式接口）。

### 请求头

| 头 | 必填 | 说明 |
|----|------|------|
| `X-User-Id` | 是（`/chat`、`/rechat`） | 用户标识；服务端用于会话创建、落库与耳机按日绑会话。 |
| `Content-Type` | 是 | `application/json`（流式接口同样需要合法 JSON 请求体）。 |

### 接口一览

| 方法 | 路径 | 响应 | 说明 |
|------|------|------|------|
| POST | `/omniasst-sysagent/v0/conversation/chat` | SSE | 发起一轮对话，流式推送模型与智能体输出。 |
| POST | `/omniasst-sysagent/v0/conversation/rechat` | SSE | 基于历史锚点消息重聊/再生成。 |
| POST | `/omniasst-sysagent/v0/conversation/message/delete-chat` | JSON | 删除单条消息（同步 management）。 |
| POST | `/omniasst-sysagent/v0/conversation/delete-chat` | JSON | 删除整个会话。 |
| POST | `/omniasst-sysagent/v0/conversation/chat/stop` | JSON | 停止指定 `conversationId` + `messageId` 的当前生成。 |

直连示例：`http://{host}:12003/omniasst-sysagent/v0/conversation/chat`。

### 请求体：`ConversationRequest`（`/chat`）

| 字段 | 类型 | 说明 |
|------|------|------|
| `responseMode` | string | 通常为 `streaming`（与 `ConversationConstant.RESPONSE_MODE` 一致）。 |
| `conversationId` | string | 已有会话可传；新会话可空，由服务端分配后通过 SSE 首帧下发。 |
| `avatarId` | string | 数字人形象 ID。 |
| `contents` | array | 多段输入；每项含 `type`（如 `text`、`image`…）、`text`、`url`、`extra`。 |
| `inputs` | object | 用户与环境信息，见下表。 |
| `headphoneMode` | boolean | 默认 `false`。为 `true` 时启用**耳机智能体链路**：同一 `user_id` 在**业务日**内复用**一个**会话 ID（见下文）。 |

`inputs` 在 JSON 中使用 **snake_case** 字段名（与 `ConversationRequest.Inputs` 上 `@JsonProperty` 一致）：

| JSON 字段 | 说明 |
|-----------|------|
| `user_id` | 用户 ID（与头 `X-User-Id` 配合使用；业务日以该用户维度计算）。 |
| `user_nickname` | 昵称。 |
| `user_now_date` | 客户端**业务日**字符串；支持 `yyyy-MM-dd` 或 `yyyy/MM/dd`。缺省时使用**服务器本地日期**。耳机按日绑会话时用于计算键。 |
| `user_province` / `user_city` / `user_district` | 行政区划。 |
| `user_longitude` / `user_latitude` | 经纬度。 |
| `location` | 粗粒度位置描述。 |

### 请求体：`RechatRequest`（`/rechat`）

| 字段 | 类型 | 说明 |
|------|------|------|
| `conversationId` | string | 会话 ID；耳机智能体链路下可与按日缓存的会话对齐。 |
| `messageId` | string | 锚定消息 ID。 |
| `avatarId` | string | 形象 ID。 |
| `inputs` | object | 同 `ConversationRequest.Inputs`（含 `user_now_date` 等）。 |
| `headphoneMode` | boolean | 与 `ConversationRequest.headphoneMode` 语义一致。 |

### 请求体：`DeleteChatRequest`（删除类）

| 字段 | 类型 | 说明 |
|------|------|------|
| `conversationId` | string | 必填。 |
| `messageId` | string | **删单条消息**（`/message/delete-chat`）时必填，为目标消息 ID。**删整会话**（`/delete-chat`）时同样使用该 DTO，主体为 `conversationId`；`messageId` 是否参与校验以 **management** 及联调约定为准。 |

### 请求体：`StopRequest`（`/chat/stop`）

| 字段 | 类型 | 说明 |
|------|------|------|
| `conversationId` | string | 会话 ID。 |
| `messageId` | string | 当前轮 assistant 消息 ID，与 SSE 注册键一致。 |

<a id="headphone-mode"></a>
### 耳机智能体（`headphoneMode = true`）

#### 适用场景

客户端为**语音优先**或**长会话**产品形态时，希望同一自然人在**一个业务日**内始终落在**同一会话**上（便于上下文连续、避免每次冷启动新建会话、与管理端「耳机会话」统计对齐），在 `/chat`、`/rechat` 请求体中置 **`headphoneMode: true`** 即可进入耳机智能体链路。与 **语音服务**的耳机 ASR WebSocket（**[hojoai-gateway-omniasst-voice/API.md](../hojoai-gateway-omniasst-voice/API.md)** 中的 **`/asr/v1/headphone`**）无强制耦合，但联调时建议 **`X-User-Id` / `inputs.user_id` / `inputs.user_now_date`** 与语音侧握手参数语义一致。

#### 分层与系统位置

- **分层归属**：耳机智能体属于 **Omniasst 业务域应用编排层**，不是独立网关或基础设施组件。
- **系统位置**：以内嵌方式运行在 **`hojoai-websocket-omniasst-sysagent`** 进程内，位于 `/conversation/chat` 入口与意图/会话执行层之间。
- **上下游关系**：上游承接客户端 SSE 请求，下游编排会话治理（Redis + management）与意图服务，并与语音网关耳机 ASR 通道协同。

#### 具体职责

| 职责域 | 说明 |
|------|------|
| 会话治理 | 按业务日单会话、并发首连锁保护、跨日切换与异常回退。 |
| 交互策略 | 控制耳机场景 SSE 表现（含 `agentType` 事件策略）与语音优先输出形态。 |
| 意图策略 | 对垂直智能体链路施加耳机场景规则（如 `bubble -> speech` 复制）。 |
| 链路一致性 | 保障 `X-User-Id`、`inputs.user_id`、`inputs.user_now_date`、`headphoneMode` 与落库标志语义一致。 |

#### 服务端机制摘要

| 机制 | 说明 |
|------|------|
| 按日单会话 | **`HeadphoneDailyConversationService`**：在 Redis 中按 **`userId` + 业务日** 绑定**当日唯一** `conversationId`；并发多路首连时通过短时分布式锁避免同一天创建多个会话。 |
| 业务日 | 取自 **`inputs.user_now_date`**（JSON 字段名 **`user_now_date`**），支持 **`yyyy-MM-dd`** 或 **`yyyy/MM/dd`**；解析失败或未传时使用 **`ZoneId.systemDefault()`** 下服务器**本地日期**。 |
| 空 `userId` | `userId` 为空时**不**走按日缓存分支，回退为与普通模式相同的 **`ConversationManagement#createConversationAndMessageId`** 行为。 |
| management | 创建会话时 **`CreateConversationIdRequest`** 携带 **`headphoneMode`**；批量落库经 Feign **`saveConversationMessages(..., headphoneMode)`** 同步标志（见 **`ConversationAPI`**）。 |
| Redis 键（实现细节） | 绑定值：`Omniasst:Headphone:Daily:{userId}:{yyyy-MM-dd}`；锁：`Omniasst:Headphone:Daily:Lock:{userId}:{yyyy-MM-dd}`（锁 TTL 约 30s，等待轮询见源码常量）。绑定键 TTL 为「至业务日结束」的秒数（含上下限裁剪，见 **`secondsUntilEndOfBusinessDay`**）。 |
| 锁等待耗尽 | 若长时间未取得锁，会打日志并**回退**为新建会话（与「严格单日单会话」理想情况略有偏差，运维需关注 Redis 延迟）。 |

#### 意图调度与 SSE 表现

- **`ConversationContext.headphoneMode`** 贯穿意图流水线；多智能体串联、各 `AgentService` 调用逻辑与常规定义一致。
- **垂直智能体 `subType`**：对翻译、会议、日程、音乐、天气、播客等目标智能体，耳机智能体链路下可对 **`subType=bubble`** 再复制一份 **`speech`** 条目，便于端侧 TTS / 播报（由 **`IntentionServiceImpl`** 写入任务序列，**非**意图模型原始 JSON 字段）。
- **`agentType` SSE 事件**：当意图目标为 **主聊天 `chat`**，或落入 **default（未知 / 空 `targetAgent` 走聊天）** 路径时，**不再**发送 `agentType` 事件，以减少语音 UI 侧无效事件；其它子智能体仍会发送 `agentType`（值为 `ConversationConstant.AGENT_*` 对应字符串）。

#### 端侧接入建议

1. **请求头**：`/chat`、`/rechat` 必带 **`X-User-Id`**，与 body 中 **`inputs.user_id`** 一致（或与产品约定的用户主键一致）。
2. **业务日**：按产品定义传入 **`inputs.user_now_date`**（例如用户所在时区的日历日）；若依赖服务器日，可不传或传与服务器一致的日期。
3. **`conversationId`**：首日可为空，由 SSE **首轮** `message` 中的 `conversationId` 落盘；次日或跨业务日键变化后会话会切换，客户端应以服务端下发为准。
4. **停止生成**：仍使用 **`POST .../chat/stop`**，携带 SSE 首轮下发的 **`conversationId`** 与当前轮 **`messageId`**。
5. **与语音 ASR 串联**：识别完成后将文本写入 **`contents`**（如 `type: text`）调用 `/chat`，并保持 **`headphoneMode: true`**；语音 WebSocket 建议在握手携带与 **`X-User-Id`** 同源的用户标识，便于排障与审计（见 voice 服务 **`API.md`**）。

---

## SSE：事件名与载荷

服务端使用 Spring **`SseEmitter`**，事件名与 `ConversationConstant` 中常量一致：

| event（SSE `event:` 行） | `data` 形态 | 说明 |
|---------------------------|-------------|------|
| `ping` | 空字符串 | 约每 10 秒心跳。 |
| `loading` | 纯文本 | 如「正在思考...」等加载文案。 |
| `message` | **JSON 字符串** | 多为 `ConversationResponse` 序列化结果。 |
| `error` | 纯文本 | 错误信息。 |
| `agentType` | 纯文本 | 即将进入的子智能体类型，值为 `chat`、`music`、`weather` 等与 `ConversationConstant.AGENT_*` 一致的标识。 |

`ConversationResponse` 主要字段：`conversationId`、`messageId`、`avatarId`、`timestamp`、`finished`、`data`（数组，每项含 `type`、`text`、`url`、`subType`、`contentId`）。**首轮**在流式业务开始前会推送一帧仅含 **`conversationId` + `messageId`** 的 `message` 事件，便于客户端落盘与后续 `stop` 调用。

客户端解析 `message` 时应对 `data` 做 JSON 解析（整帧为字符串）。

---

## WebSocket 端点

| URL 模式 | 说明 |
|----------|------|
| `ws://{host}:{port}/omniasst-sysagent` | 直连应用 |
| `ws://{网关主机}/omniasst-sysagent` | 经网关（`lb:ws://...`） |

注册类：`IWebSocketConfigurer`。

### 握手与请求头

握手拦截器 `IHandshakeInterceptor` 会从 HTTP 请求头读取设备相关信息并写入 session attributes（头字段与 attribute 的映射以源码为准）。

### 消息格式

- 负载为 **JSON**，外层结构为 `IMessage`（含 `eventType` 等字段，具体见源码）。
- 服务端根据 `eventType` 分发；**非法或未知事件** 将返回统一错误事件帧（如 `ILLEGAL_ARGUMENT`、`UNSUPPORTED_OPERATION`、`GENERAL_ERROR`）。

### 客户端可发送的业务事件

校验逻辑要求 JSON 中的 `eventType` 与枚举 **`IMessageEvent.name()`** 完全一致。

| eventType | 行为 |
|-----------|------|
| `BUILD_RTC_TOKEN` | 签发 RTC Token；服务端回推 `BUILD_RTC_TOKEN_RESULT` |
| `TICKET_RESULT` | 绑定用户与会话，供影票等场景服务端下行（见 `IWebSocketSendHandler`） |

### 服务端错误 / 回推事件（`IMessageEvent` 枚举值）

常见下行：`GENERAL_ERROR`、`ILLEGAL_ARGUMENT`、`UNSUPPORTED_OPERATION`、`BUILD_RTC_TOKEN_RESULT`。完整枚举见源码 **`IMessageEvent`**（其中 `TICKET_RESULT`、`BUILD_RTC_TOKEN` 等为**上行**客户端事件名）。

---

## 说明

- 连接关闭时，WebSocket 实现会 **清空** `IWebSocketSendHandler` 中的会话映射（见 `afterConnectionClosed`）。
- 业务上还会与 gRPC、资讯 WebSocket、**hojoai-webmvc-omniasst-management**（Feign `ConversationAPI`）等协作，细节见各 `*ServiceImpl` 与配置。
- 本仓库另有电影、支付回调、推送等 HTTP 模块，路径以各 `*Controller` 的 `@RequestMapping` 为准；本文档以**对话 SSE** 与 **WebSocket 控制面**为主。
