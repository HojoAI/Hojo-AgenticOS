# 语音服务接口完整文档

## 一、接口概览

### HTTP接口（7个）
1. **音频文件转文本（HTTP-OSS）** - `POST /omniasst-voice/v1/asr`
2. **音频文件转文本（HTTP-文件上传）** - `POST /omniasst-voice/v1/asr/file`
3. **录音转文本（HTTP-SSE）** - `POST /omniasst-voice/v1/asr/stream`
4. **音频文件转文本并翻译（HTTP）** - `POST /omniasst-voice/v1/asr/translate`
5. **文字转语音文件（HTTP）** - `POST /omniasst-voice/v1/tts/file`
6. **文字转语音OSS（HTTP）** - `POST /omniasst-voice/v1/tts/oss`
7. **文字转语音流（HTTP-SSE）** - `POST /omniasst-voice/v1/tts/stream`

**注意**: WebSocket接口请参考 `语音服务长连接接口文档.md` 文档，包含翻译场景（`/translate`）、会议场景（`/meeting`）和TTS场景（`/common/tts`）的WebSocket接口。

---

## 二、HTTP接口详情

### 1. 音频文件转文本（HTTP-OSS）

**接口路径**: `POST /omniasst-voice/v1/asr`

**请求方式**: POST

**Content-Type**: `application/json`

**请求参数** (JSON):

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ossUrl | String | 是 | OSS音频文件地址（WAV格式） |
| language | String | 否 | 语言代码，默认"all" |

**响应格式**:

```json
{
  "resCode": "200",
  "resMessage": "success",
  "resContent": "识别出的文本内容"
}
```

**说明**:
- 从OSS下载音频文件并自动转换为PCM格式（16k采样率，16bit，单声道）进行识别
- 支持音频格式：WAV（会自动转换为PCM）
- 音频文件会按标准包大小（320*16*2=10240字节）切分处理
- 返回完整的识别文本

**请求示例**:
```bash
curl -X POST "http://host:port/omniasst-voice/v1/asr" \
  -H "Content-Type: application/json" \
  -d '{"ossUrl":"https://agent-static.oss-cn-shanghai.aliyuncs.com/omniasst-voice/tts/audio.wav","language":"zh"}'
```

---

### 2. 音频文件转文本（HTTP-文件上传）

**接口路径**: `POST /omniasst-voice/v1/asr/file`

**请求方式**: POST

**Content-Type**: `multipart/form-data`

**请求参数** (FormData):

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 是 | WAV格式音频文件（最大5MB） |

**响应格式**:

```json
{
  "resCode": "200",
  "resMessage": "success",
  "resContent": "识别出的文本内容"
}
```

**说明**:
- 前端直接上传WAV音频文件，支持multipart/form-data格式
- 文件大小限制：最大5MB
- 服务端自动将WAV转换为PCM格式（16k采样率，16bit，单声道）进行识别
- 支持音频格式：WAV（会自动转换为PCM）
- 音频文件会按标准包大小（320*16*2=10240字节）切分处理
- 返回完整的识别文本（非流式）

**错误响应**:
- 文件大小超过限制：`resCode: "500"`, `resContent: "FILE_TOO_LARGE: 文件大小超过限制"`
- 文件格式不正确：`resCode: "500"`, `resContent: "INVALID_FILE_FORMAT: 文件格式不正确，仅支持WAV格式"`
- 文件转换错误：`resCode: "500"`, `resContent: "FILE_CONVERSION_ERROR: 文件转换失败"`

**请求示例**:
```bash
curl -X POST "http://host:port/omniasst-voice/v1/asr/file" \
  -F "file=@/path/to/audio.wav"
```

---

### 3. 录音转文本（HTTP-SSE）

**接口路径**: `POST /omniasst-voice/v1/asr/stream`

**请求方式**: POST

**Content-Type**: `application/json`

**响应类型**: `text/event-stream` (Server-Sent Events)

**请求参数** (JSON):

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ossUrl | String | 是 | OSS音频文件地址（WAV格式） |
| language | String | 否 | 语言代码，默认"all" |

**响应格式** (SSE事件流):

每个SSE事件的数据部分是一个JSON字符串，格式为`AsrTextResponse`对象：

```
data: {"text":"你好","isEnd":0}

data: {"text":"你好这是","isEnd":0}

data: {"text":"你好这是一段","isEnd":1}
```

JSON字段说明：
- `text`: String - 识别出的文本（全量，每次更新）
- `isEnd`: Integer - 是否结束（0=识别中，1=识别完成）

**说明**:
- 从OSS下载音频文件并自动转换为PCM格式进行识别
- 支持音频格式：WAV（会自动转换为PCM）
- 流式返回识别结果，每个SSE事件包含JSON格式的`AsrTextResponse`对象
- `text`字段为全量文本（每次更新）
- `isEnd: 0`表示识别中，`isEnd: 1`表示识别完成
- 音频文件按标准包大小切分处理，最后一个小于标准大小的包会合并到前一个包中

**错误处理**:
当发生错误时，服务端会发送一个错误事件：
```
event: error
data: Error: [错误信息]
```

**请求示例**:
```bash
curl -X POST "http://host:port/omniasst-voice/v1/asr/stream" \
  -H "Content-Type: application/json" \
  -d '{"ossUrl":"https://agent-static.oss-cn-shanghai.aliyuncs.com/omniasst-voice/tts/audio.wav","language":"zh"}' \
  -N
```

---

### 4. 音频文件转文本并翻译（HTTP）

**接口路径**: `POST /omniasst-voice/v1/asr/translate`

**请求方式**: POST

**Content-Type**: `application/json`

**请求参数** (JSON):

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ossUrl | String | 是 | OSS音频文件地址（WAV格式） |
| language | String | 否 | 语言代码，默认"all" |

**响应格式**:

```json
{
  "resCode": "200",
  "resMessage": "success",
  "resContent": "翻译后的文本内容"
}
```

**说明**:
- 从OSS下载音频文件并自动转换为PCM格式进行识别
- 支持音频格式：WAV（会自动转换为PCM）
- 先进行ASR识别，再进行翻译
- 返回翻译后的文本内容

**请求示例**:
```bash
curl -X POST "http://host:port/omniasst-voice/v1/asr/translate" \
  -H "Content-Type: application/json" \
  -d '{"ossUrl":"https://agent-static.oss-cn-shanghai.aliyuncs.com/omniasst-voice/tts/audio.wav","language":"zh"}'
```

---

### 5. 文字转语音文件（HTTP）

**接口路径**: `POST /omniasst-voice/v1/tts/file`

**请求方式**: POST

**Content-Type**: `application/json`

**响应类型**: `audio/wav` (文件下载)

**请求参数** (JSON):

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| text | String | 是 | 需要转换为语音的文本内容 |
| language | String | 否 | 语言代码，默认"zh" |

**请求示例**:
```json
{
  "text": "你好，这是一段测试文本",
  "language": "zh"
}
```

**响应格式**:
- Content-Type: `audio/wav`
- Content-Disposition: `attachment; filename="tts_xxx.wav"`
- 响应体：WAV格式音频文件的二进制数据

**说明**:
- 直接返回WAV格式音频文件
- 客户端可以直接保存或播放返回的音频数据
- 音频格式：WAV

**请求示例**:
```bash
curl -X POST "http://host:port/omniasst-voice/v1/tts/file" \
  -H "Content-Type: application/json" \
  -d '{
    "text": "你好，这是一段测试文本",
    "language": "zh"
  }' \
  --output audio.wav
```

---

### 6. 文字转语音OSS（HTTP）

**接口路径**: `POST /omniasst-voice/v1/tts/oss`

**请求方式**: POST

**Content-Type**: `application/json`

**请求参数** (JSON):

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| text | String | 是 | 需要转换为语音的文本内容 |
| language | String | 否 | 语言代码，默认"zh" |
| voiceId | String | 否 | 语音ID，默认根据语言自动选择 |

**请求示例**:
```json
{
  "text": "你好，这是一段测试文本",
  "language": "zh",
  "voiceId": "default"
}
```

**响应格式**:

```json
{
  "resCode": "200",
  "resMessage": "success",
  "resContent": "https://xxx.oss-cn-hangzhou.aliyuncs.com/omniasst-voice/tts/tts_xxx.wav"
}
```

**说明**:
- 返回OSS上音频文件的URL
- 音频格式：WAV
- 音频文件已上传到OSS，客户端可通过返回的URL访问

**请求示例**:
```bash
curl -X POST "http://host:port/omniasst-voice/v1/tts/oss" \
  -H "Content-Type: application/json" \
  -d '{
    "text": "你好，这是一段测试文本",
    "language": "zh"
  }'
```

---

### 7. 文字转语音流（HTTP-SSE）

**接口路径**: `POST /omniasst-voice/v1/tts/stream`

**请求方式**: POST

**Content-Type**: `application/json`

**响应类型**: `text/event-stream` (Server-Sent Events)

**请求参数** (JSON):

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| text | String | 是 | 需要转换为语音的文本内容 |
| language | String | 否 | 语言代码，默认"zh" |
| voiceId | String | 否 | 语音ID，默认根据语言自动选择 |

**请求示例**:
```json
{
  "text": "你好，这是一段测试文本",
  "language": "zh",
  "voiceId": "default"
}
```

**响应格式** (SSE事件流):

正常数据事件：
```
data: [Base64编码的音频数据块1]

data: [Base64编码的音频数据块2]

data: [Base64编码的音频数据块3]
```

错误事件：
```
event: error
data: Error: [错误信息]
```

**说明**:
- 通过SSE流式返回音频数据
- 音频格式：PCM（16k采样率，16bit，单声道）
- 每个SSE事件的`data`字段包含Base64编码的音频数据块（字节数组）
- 客户端需要解码Base64并累积接收音频数据块
- 当发生错误时，会发送`event: error`的错误事件

**请求示例**:
```bash
curl -X POST "http://host:port/omniasst-voice/v1/tts/stream" \
  -H "Content-Type: application/json" \
  -d '{
    "text": "你好，这是一段测试文本",
    "language": "zh"
  }' \
  -N
```

---

## 三、WebSocket接口说明

WebSocket接口请参考 `语音服务长连接接口文档.md` 文档，包含以下场景：

1. **翻译场景** - `ws://host:port/omniasst-voice/translate`
   - 实时语音识别和翻译
   - 支持文本消息和二进制消息
   - 支持TTS音频流返回

2. **会议场景** - `ws://host:port/omniasst-voice/meeting`
   - 会议音频转录
   - 支持声纹识别和说话人识别
   - 支持音频段管理和OSS存储

3. **TTS场景** - `ws://host:port/omniasst-voice/common/tts`
   - 文本转语音，通过WebSocket通道流式输出音频
   - 支持文本消息请求
   - 支持二进制音频流响应

### TTS WebSocket接口详情

**连接地址**: `ws://host:port/omniasst-voice/common/tts`

**连接建立**:
建立连接时，可以通过以下两种方式传递设备ID和用户ID：

**方式1：请求头（推荐，适用于非浏览器客户端）**
```
X-Device-Id: device123 (可选)
X-User-Id: user456 (可选)
```

**方式2：URL查询参数（适用于浏览器客户端）**
由于浏览器限制，WebSocket连接无法自定义请求头，可通过URL查询参数传递：
```
ws://host:port/omniasst-voice/common/tts?deviceId=device123&userId=user456
```

**消息格式**:

所有文本消息采用统一格式：
```json
{
  "event": "事件类型",
  "payload": {
    "pdata": {
      // 具体业务数据
    }
  }
}
```

**客户端发送TTS请求**（文本消息）:
```json
{
  "event": "REQ_TTS_TEXT",
  "payload": {
    "pdata": {
      "text": "你好，这是一段测试文本",
      "language": "zh"
    }
  }
}
```

**参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| text | String | 是 | 需要转换为语音的文本内容 |
| language | String | 否 | 语言代码，默认"zh"（chinese、english、french） |

**服务端返回音频流**（二进制消息）:
- 服务端接收到TTS请求后，开始流式推送音频数据
- 音频格式：二进制PCM数据（直接通过WebSocket二进制消息发送）
- 客户端收到后需暂存至本地，累积完整音频后播放

**错误消息**（文本消息）:
```json
{
  "event": "BACK_ERROR",
  "payload": {
    "pdata": {
      "errorCode": "错误代码",
      "errorMessage": "错误信息",
      "stage": "tts"
    }
  }
}
```

**错误代码**:
- `ILLEGAL_ARGUMENT`: 参数错误（如text为空）
- `GENERAL_ERROR`: 通用错误（如TTS服务调用失败）
- `UNSUPPORTED_OPERATION`: 不支持的操作

**心跳机制**:

**客户端发送心跳**（每10秒）:
```json
{
  "event": "REQ_HEARTBEAT",
  "payload": {
    "pdata": {
      "timestamp": 1234567890
    }
  }
}
```

**服务端响应心跳**:
```json
{
  "event": "BACK_HEARTBEAT",
  "payload": {
    "pdata": {
      "timestamp": 1234567890,
      "serverTime": 1234567891
    }
  }
}
```

**注意事项**:
- 心跳间隔：10秒（客户端主动发送）
- 超时时间：30秒（连续3轮无心跳则断开连接）
- 收到任何消息（心跳、TTS请求）都会更新最后活跃时间
- 服务端不会主动关闭连接，只有在心跳超时时会关闭连接
- 浏览器客户端无法自定义请求头，必须通过URL查询参数传递deviceId和userId

**完整交互示例**:
```
1. 建立连接
   客户端 → WebSocket连接 (ws://host:port/omniasst-voice/common/tts?deviceId=device123)

2. 发送TTS请求
   客户端 → {
     "event": "REQ_TTS_TEXT",
     "payload": {
       "pdata": {
         "text": "你好，这是一段测试文本",
         "language": "zh"
       }
     }
   }
   
   服务端 → 二进制音频流（持续推送）

3. 心跳（每10秒）
   客户端 → {
     "event": "REQ_HEARTBEAT",
     "payload": {
       "pdata": {
         "timestamp": 1234567890
       }
     }
   }
   
   服务端 → {
     "event": "BACK_HEARTBEAT",
     "payload": {
       "pdata": {
         "timestamp": 1234567890,
         "serverTime": 1234567891
       }
     }
   }

4. 关闭连接
   客户端 → 关闭WebSocket连接
```

---

## 四、通用说明

### 请求头

WebSocket连接支持以下请求头（或URL查询参数）：

| 请求头/参数 | 说明 | 必填 |
|--------|------|------|
| X-Device-Id / deviceId | 设备ID | 可选 |
| X-User-Id / userId | 用户ID | 可选 |
| User-Agent | 用户代理 | 可选 |

**注意**: 浏览器客户端无法自定义请求头，可通过URL查询参数传递：`ws://host:port/omniasst-voice/translate?deviceId=xxx&userId=yyy`

### 音频格式要求

**ASR输入音频**:
- 格式：PCM
- 采样率：16kHz
- 位深：16bit
- 声道：单声道
- 标准包大小：10240字节（320ms）

**TTS输出音频**:
- 格式：MP3
- 采样率：根据TTS服务配置

### 错误处理

所有接口在发生错误时会返回相应的错误信息：

**HTTP接口错误响应**:
```json
{
  "resCode": "500",
  "resMessage": "错误描述",
  "resContent": null
}
```

**WebSocket错误消息**:
```json
{
  "event": "BACK_ERROR",
  "payload": {
    "pdata": {
      "errorCode": "ERROR_CODE",
      "errorMessage": "错误描述",
      "stage": "错误发生的阶段"
    }
  }
}
```

### 语言代码

常用语言代码：
- `zh`: 中文
- `en`: 英文
- `all`: 自动识别（仅ASR）

### 注意事项

1. **音频文件转文本**接口会按标准包大小（10240字节）切分处理音频
2. **SSE接口**需要客户端支持Server-Sent Events
3. **WebSocket连接**建议实现重连机制
4. **WebSocket接口**详细说明请参考 `语音服务长连接接口文档.md` 文档

---

## 五、接口统计

### 总计：7个HTTP接口

**HTTP接口（7个）**:
1. POST /omniasst-voice/v1/asr (OSS文件)
2. POST /omniasst-voice/v1/asr/file (文件上传)
3. POST /omniasst-voice/v1/asr/stream (SSE流式)
4. POST /omniasst-voice/v1/asr/translate (转文本并翻译)
5. POST /omniasst-voice/v1/tts/file (文件下载)
6. POST /omniasst-voice/v1/tts/oss (OSS存储)
7. POST /omniasst-voice/v1/tts/stream (SSE流式)

**WebSocket接口**:
- 请参考 `语音服务长连接接口文档.md` 文档
- 包含翻译场景（/translate）、会议场景（/meeting）和TTS场景（/common/tts）

---

## 六、返回值字段说明

### HTTP接口返回值

所有HTTP接口统一使用 `Response<T>` 格式：

```json
{
  "resCode": "200",
  "resMessage": "success",
  "resContent": "具体内容"
}
```

字段说明：
- `resCode`: 响应代码，200表示成功
- `resMessage`: 响应消息
- `resContent`: 响应内容，类型根据接口而定（String、Object等）

### WebSocket消息格式

WebSocket消息格式请参考 `语音服务长连接接口文档.md` 文档，所有文本消息统一使用以下格式：

```json
{
  "event": "事件类型",
  "payload": {
    "pdata": {
      "具体业务数据"
    }
  }
}
```

字段说明：
- `event`: 事件类型（字符串）
- `payload`: 载荷对象
- `payload.pdata`: 具体业务数据（根据事件类型不同而不同）

---

## 七、完整示例

### JavaScript WebSocket示例

WebSocket接口的完整示例请参考 `语音服务长连接接口文档.md` 文档，包含翻译场景和会议场景的详细示例。

---

文档版本：1.0  
最后更新：2024年

