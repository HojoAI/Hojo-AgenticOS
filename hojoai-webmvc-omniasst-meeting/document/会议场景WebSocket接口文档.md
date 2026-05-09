# 会议场景 WebSocket 接口文档

## 一、概述

本文档描述会议智能体服务的 WebSocket 长连接接口协议。

### 连接方式
- **会议场景**: `ws://host:port/omniasst-meeting/meeting`

### 连接建立
建立连接时，可以通过以下两种方式传递设备ID和用户ID：

**方式1：请求头（推荐，适用于非浏览器客户端）**
```
X-Device-Id: device123 (可选)
X-User-Id: user456 (可选)
```

**方式2：URL查询参数（适用于浏览器客户端）**
由于浏览器限制，WebSocket连接无法自定义请求头，可通过URL查询参数传递：
```
ws://host:port/omniasst-meeting/meeting?deviceId=device123&userId=user456
```

**优先级**：服务端优先从请求头获取，如果请求头中没有，则从URL查询参数中获取。

### 心跳机制
- **心跳间隔**: 10秒（客户端主动发送）
- **超时时间**: 30秒（连续3轮无心跳则断开连接）
- **心跳消息**: 见下方心跳消息格式

---

## 二、会议场景 (`/omniasst-meeting/meeting`)

### 2.1 连接特性
- **消息类型**: 仅支持文本消息（JSON格式）
- **功能**: VAD + ASR识别 + 声纹识别
- **不支持**: 翻译和TTS

### 2.2 消息格式

#### 2.2.1 统一消息格式
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

#### 2.2.2 音频数据（文本消息，Base64编码）
```json
{
  "event": "REQ_MEETING_AUDIO_DATA",
  "payload": {
    "pdata": {
      "audioData": "UklGRiQAAABXQVZFZm10..."
    }
  }
}
```

**参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| audioData | String | 是 | Base64编码的PCM音频数据 |

**音频格式要求**:
- **格式**: PCM格式，16k采样率，16bit，单声道
- **编码**: Base64编码后作为字符串传输
- **包大小**: 建议按标准包大小（10240字节 = 320ms的音频）发送，服务端会累积到该大小后处理

### 2.3 流程说明

#### 流程1: 连接建立与初始化
连接建立后，服务端会立即下发会议ID。

**服务端返回会议ID**:
```json
{
  "event": "BACK_MEETING_ID",
  "payload": {
    "pdata": {
      "meetingId": "meeting-1234567890123456789",
      "timestamp": 1704067200000,
      "elapsedAudioMs": 0
    }
  }
}
```

**参数说明**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| meetingId | String | 唯一会议标识（格式：M{雪花ID} 等，以实际下发为准） |
| timestamp | Long | 会议开始时间戳（毫秒） |
| elapsedAudioMs | Long | 已录制时长（毫秒）；新开会为 0，带 `meetingId` 续接时为当前时间轴进度 |

是否新开会 / 续接由连接时是否携带 `meetingId` 判断，**不再下发 `sessionMode` 字段**。

#### 流程2: 音频处理阶段
收到会议ID后，客户端开始发送音频数据。

**客户端发送音频数据**（文本消息，Base64编码）:
```json
{
  "event": "REQ_MEETING_AUDIO_DATA",
  "payload": {
    "pdata": {
      "audioData": "UklGRiQAAABXQVZFZm10..."
    }
  }
}
```

**服务端处理流程**:
1. **VAD判断**: 累积到10240字节后调用VAD，判断音频有效性
2. **ASR识别**: VAD判断有效后，开始ASR识别
3. **声纹识别**: 每段音频识别完成后，进行声纹识别，匹配说话人
4. **返回识别结果**: 实时返回ASR识别文本
5. **段完成通知**: 当一段音频识别完成（isEnd=1）时，发送段完成通知

**服务端返回识别结果**（文本消息）:
```json
{
  "event": "BACK_MEETING_ASR_TEXT",
  "payload": {
    "pdata": {
      "fixedText": "你好，这是一段测试文本",
      "tmpText": "",
      "isEnd": 0,
      "segmentId": "segment-1234567890123456789"
    }
  }
}
```

**参数说明**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| fixedText | String | ASR识别的固定文本（已确认部分） |
| tmpText | String | ASR识别的临时文本（未确认部分） |
| isEnd | Integer | 0=未结束，1=结束 |
| segmentId | String | 当前音频段的唯一标识 |

**服务端返回段完成通知**（当isEnd=1时）:
```json
{
  "event": "BACK_MEETING_SEGMENT_COMPLETE",
  "payload": {
    "pdata": {
      "segmentId": "segment-1234567890123456789",
      "text": "你好，这是一段测试文本",
      "speakerId": "user-123",
      "speakerName": "张三",
      "speakerAvatar": "https://example.com/avatar.jpg",
      "startTime": 0,
      "endTime": 5000,
      "timestamp": 1704067200000
    }
  }
}
```

**参数说明**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| segmentId | String | 音频段唯一标识 |
| text | String | 该段的完整识别文本 |
| speakerId | String | 说话人ID（通过声纹识别匹配，可能是用户ID或会议内编号） |
| speakerName | String | 说话人名称（匹配到用户时返回用户名，会议内编号时返回"UserName-1"格式） |
| speakerAvatar | String | 说话人头像URL（匹配到用户时返回头像，为空时返回空字符串） |
| startTime | Long | 段在总时间轴的起始位置（毫秒） |
| endTime | Long | 段在总时间轴的结束位置（毫秒） |
| timestamp | Long | 消息发送时间戳（毫秒） |

**错误消息**:
```json
{
  "event": "BACK_ERROR",
  "payload": {
    "pdata": {
      "errorCode": "错误代码",
      "errorMessage": "错误信息",
      "stage": "错误阶段（asr/process）"
    }
  }
}
```

**错误代码**:
- `ASR_ERROR`: ASR识别错误
- `PROCESS_ERROR`: 处理过程错误
- `GENERAL_ERROR`: 通用错误
- `ILLEGAL_ARGUMENT`: 参数错误

#### 流程3: 心跳机制
会议场景支持心跳机制，通过文本消息更新活跃时间。

**客户端发送心跳**（每10秒，文本消息）:
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

**注意**: 收到任何消息（文本心跳、二进制音频）都会更新最后活跃时间。

#### 流程4: 关闭连接

**客户端主动断开连接**:
```json
{
  "event": "REQ_MEETING_STOP",
  "payload": {
    "pdata": {
      "needSave": 1
    }
  }
}
```

**参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| needSave | Integer | 是 | 是否需要保存：0=不保存，1=保存 |

**服务端响应**:
```json
{
  "event": "BACK_MEETING_STOP_ACK",
  "payload": {
    "pdata": {
      "status": "success",
      "meetingId": "meeting-1234567890123456789",
      "totalDuration": 120000,
      "segmentCount": 5,
      "message": "Meeting stopped successfully"
    }
  }
}
```

**参数说明**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| status | String | 状态：success（成功） |
| meetingId | String | 会议ID |
| totalDuration | Long | 总有效音频时长（毫秒） |
| segmentCount | Integer | 音频段总数 |
| message | String | 消息描述 |

**注意**:
- 客户端发送 `REQ_MEETING_STOP` 后，服务端会先完成当前正在处理的音频段，然后处理会议结束（下载完整音频、转换为WAV、上传OSS、发布最终结果到Redis），最后发送确认消息
- 服务端不会主动关闭连接，连接由客户端关闭
- 如果心跳超时（超过30秒无心跳）或应用异常关闭，服务端会默认保存（needSave=1）

---

## 三、事件类型列表

### 客户端发送事件
- `REQ_MEETING_AUDIO_DATA`: 发送音频数据
- `REQ_MEETING_STOP`: 停止会议
- `REQ_HEARTBEAT`: 心跳消息

### 服务端返回事件
- `BACK_MEETING_ID`: 返回会议ID
- `BACK_MEETING_ASR_TEXT`: 返回ASR识别结果
- `BACK_MEETING_SEGMENT_COMPLETE`: 返回段完成通知
- `BACK_MEETING_STOP_ACK`: 停止确认
- `BACK_HEARTBEAT`: 心跳响应
- `BACK_ERROR`: 错误消息

---

## 四、完整交互示例

```
客户端 → 服务端: 建立连接
服务端 → 客户端: BACK_MEETING_ID (返回meetingId)

客户端 → 服务端: REQ_MEETING_AUDIO_DATA (音频数据1)
客户端 → 服务端: REQ_MEETING_AUDIO_DATA (音频数据2)
...
服务端 → 客户端: BACK_MEETING_ASR_TEXT (识别结果，isEnd=0)
服务端 → 客户端: BACK_MEETING_ASR_TEXT (识别结果，isEnd=1)
服务端 → 客户端: BACK_MEETING_SEGMENT_COMPLETE (段完成通知)

客户端 → 服务端: REQ_MEETING_STOP (停止，needSave=1)
服务端 → 客户端: BACK_MEETING_STOP_ACK (确认)
客户端关闭连接
```

---

## 五、注意事项

### 5.1 音频格式
- 必须使用PCM格式，16k采样率，16bit，单声道
- Base64编码后传输
- 建议按10240字节（320ms）的包大小发送

### 5.2 声纹识别
- 服务端会对每段音频进行声纹识别
- 优先匹配全局用户声纹库（PostgreSQL）
- 如果未匹配到用户，则在会议内进行匹配
- 如果会议内也未匹配到，则创建新的会议内编号（UserId-1, UserId-2...）

### 5.3 段管理
- 每段音频识别完成后，会生成唯一的segmentId
- 段完成时会推送段完成通知，包含说话人信息
- 所有段的音频会累积到OSS，会议结束时转换为WAV格式

### 5.4 错误处理
- 所有错误都会通过 `BACK_ERROR` 事件返回
- 错误不会中断连接，客户端可以根据错误信息决定是否继续

### 5.5 心跳机制
- 客户端需要每10秒发送一次心跳
- 如果30秒内没有收到任何消息（包括心跳和音频数据），服务端会主动断开连接
- 异常断开时，服务端会默认保存会议记录（needSave=1）
