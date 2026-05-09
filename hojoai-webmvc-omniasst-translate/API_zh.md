# hojoai-webmvc-omniasst-translate — API 文档

## 基址

- **context-path**：`/omniasst-translate`
- 经网关：`{网关}/omniasst-translate/...`

---

## 翻译会话 — `/v0/translation/conversations`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v0/translation/conversations/list` | 翻译会话列表 |
| POST | `/v0/translation/conversations/messages/list` | 会话内消息列表 |

实现类：`TranslationController`。

---

## 服务间调用（Feign，非对外接口）

以下接口由本服务 **调用** 其他微服务，一般 **不应** 由终端直接访问本模块来替代原服务：

| 客户端 | 方法路径（相对被调服务） | 说明 |
|--------|--------------------------|------|
| `VoiceBaseServiceClient` | POST `/v1/asr/realtime`、`/v1/vad`、`/v1/translate`、`/v1/nlu/translate` | 语音基础能力 |
| `ManagementServiceClient` | POST `/v0/voice/clone`、`/v0/push`、`/v0/conversations/conversation/message/push` | 管理域 |
| `TitleGenerationModelClient` | POST `/v1/generateTitle` | 标题生成模型 |
