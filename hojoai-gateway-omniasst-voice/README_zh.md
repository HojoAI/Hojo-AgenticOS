# hojoai-gateway-omniasst-voice

**Omniasst 语音能力服务**：提供 TTS、ASR、VAD、声纹、翻译/NLU、声音克隆等 HTTP 接口，并通过 WebSocket 提供通用 TTS、ASR 翻译、ASR v1（事件化识别）及耳机专用 ASR v1 等长连接能力。

## 能力列表

### HTTP 接口
- TTS 语音合成：文件合成、SSE 流式、PCM 二进制流、OSS 上传
- ASR 语音识别：标准识别、SSE 流式、文件上传、实时识别
- Aliyun ASR：阿里云实时 ASR 与 WAV 格式识别
- VAD：语音活动检测
- 声纹：注册、查询、删除、识别（标准 + 文件方式）
- 翻译/NLU：翻译与 NLU 翻译接口
- 声音克隆：通过音频文件克隆音色

### WebSocket 长连接
- `/common/tts`：通用 TTS（Feign/豆包长连接/队列等）
- `/asr/translate`：ASR 翻译长连接
- `/asr/v1`：ASR v1 按 platform 选择实现（HOJO/HOJO_HEADPHONE）
- `/asr/v1/headphone`：耳机 ASR，固定走 HOJO_HEADPHONE，含空文本超时逻辑

### ASR v1 事件协议
- `REQ_ASR_CONFIG` → `BACK_ASR_CONFIG_ACK` → `REQ_ASR_AUDIO_DATA` → `RESP_ASR_RESULT`
- 耳机通道额外支持 `RESP_VAD_TIMEOUT`（空文本窗口超时）

## 技术栈

- Java 21、Spring Boot 3.x
- Spring Web / WebSocket、WebFlux（SSE 等场景）
- Nacos；外部 ASR/TTS 等地址由配置项指定

## 运行约定

| 项目 | 说明 |
|------|------|
| 可运行模块 | `hojoai-gateway-omniasst-voice-server` |
| 默认 HTTP 端口 | `12002` |
| Servlet context-path | `/omniasst-voice` |
| 经 Omniasst 网关 | 前缀一般为 `{网关}/omniasst-voice` |

## WebSocket 路径（相对 context-path）

与 HTTP 相同，均挂在 **`/omniasst-voice`** 之下（升级 URL 示例：`ws://{主机}:12002/omniasst-voice/asr/v1`）。

| 路径 | 说明 |
|------|------|
| `/common/tts` | 通用 TTS（Feign / 豆包长连接 / 队列等） |
| `/asr/translate` | ASR 翻译长连接 |
| `/asr/v1` | ASR v1：按 `platform` 选择 `AsrServiceFactory` 实现 |
| `/asr/v1/headphone` | 耳机 ASR：事件模型与 v1 一致，识别固定走 `HOJO_HEADPHONE`；`BACK_ASR_CONFIG_ACK.headphoneMode=true`；可能下行 `RESP_VAD_TIMEOUT`（空文本窗口，见 `HeadphoneAsrWebSocketHandler`） |

握手可选携带 `X-Device-Id`、`X-User-Id`（或 Query `deviceId`、`userId`）。事件名与 `payload.pdata` 约定见 [API.md](./API.md)。

### 耳机智能体 ASR 通道（说明）

**`/asr/v1/headphone`** 与 **`/asr/v1`** 共用同一套 JSON 事件协议（`REQ_ASR_CONFIG` → `BACK_ASR_CONFIG_ACK` → `REQ_ASR_AUDIO_DATA` → `RESP_ASR_RESULT`），区别在于识别链路与耳机专用空文本超时逻辑；与 **sysagent** 的耳机智能体链路（`headphoneMode: true`）搭配使用时，建议用户 ID、业务日（`user_now_date`）两端对齐。推荐消息顺序、`RESP_VAD_TIMEOUT` 行为及常量说明见 **[API.md](./API.md)**「耳机通道 ASR」小节。

## 模块结构

```
hojoai-gateway-omniasst-voice/
├── pom.xml
├── API.md                                  # 对外 HTTP + WebSocket 路径与 ASR v1 协议摘要
├── README.md
├── document/                               # 更细的协议与场景说明（中文）
└── hojoai-gateway-omniasst-voice-server/   # Spring Boot 应用
    └── src/main/java/.../voice/
        ├── controller/                     # HTTP API
        ├── configure/                      # WebSocket 注册等
        ├── websocket/                      # TTS / 翻译 / ASR v1 Handler
        └── service/                      # TTS、ASR、声纹等业务
```

## 构建与运行

```bash
cd hojoai-gateway-omniasst-voice
mvn clean install
cd hojoai-gateway-omniasst-voice-server && mvn spring-boot:run
```

环境相关配置见 `hojoai-gateway-omniasst-voice-server/src/main/resources/config/application-dev.properties`。

## 文档

| 文档 | 内容 |
|------|------|
| [API.md](./API.md) | **HTTP**（`/v1/tts`、`/v1/asr` 等）与 **WebSocket** 路径、ASR v1 事件与配置字段摘要 |
| [document/语音服务HTTP接口文档.md](./document/语音服务HTTP接口文档.md) | HTTP 接口详细说明 |
| [document/语音服务长连接接口文档.md](./document/语音服务长连接接口文档.md) | 长连接与消息体示例 |
