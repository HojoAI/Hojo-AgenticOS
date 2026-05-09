# TranslateWebSocketHandler 处理逻辑说明

## 一、概述

`TranslateWebSocketHandler` 是一个实时语音翻译服务的 WebSocket 处理器，负责处理客户端的翻译请求，并与后端语音服务进行交互。

## 二、核心组件

### 2.1 SessionState（会话状态）

每个 WebSocket 连接维护一个 `SessionState` 对象，存储以下信息：

- **chatId**: 会话ID，由雪花算法生成
- **sourceLanguage/targetLanguage**: 源语言和目标语言
- **voiceClient**: 与语音服务的 WebSocket 客户端连接
- **asrText/transText**: 识别的原文和翻译文本
- **detectedLang**: 检测到的语言
- **voiceCloneEnabled**: 是否启用音色克隆
- **voiceCloneAudioBuffer**: 音色克隆音频缓冲区
- **voiceCloneAudioDurationMs**: 音色克隆音频时长（毫秒）
- **voiceCloneTriggered**: 是否已触发音色克隆

### 2.2 关键常量

- `VOICE_CLONE_MIN_DURATION_MS = 5000`: 音色克隆最小音频时长（5秒）
- `AUDIO_CHUNK_DURATION_MS = 320`: 每个音频块的时长（320毫秒）

## 三、消息处理流程

### 3.1 连接建立阶段

```
客户端连接 → afterConnectionEstablished()
  ↓
注册心跳管理
  ↓
创建 SessionState
  ↓
记录连接开始时间
```

**处理逻辑**：
- 从 session attributes 获取 `deviceId`
- 注册到心跳管理器
- 创建并初始化 `SessionState`
- 记录连接开始时间

### 3.2 配置阶段（REQ_ASR_TRANSLATE_CONFIG）

```
客户端发送配置 → handleConfig()
  ↓
验证配置参数（sourceLanguage, targetLanguage）
  ↓
从 Nacos 获取语音服务实例
  ↓
创建 VoiceServiceWebSocketClient
  ↓
连接到语音服务
  ↓
发送配置到语音服务
  ↓
收到配置确认 → 生成 chatId
  ↓
返回 chatId 给客户端（BACK_CHAT_ID）
```

**关键步骤**：
1. 验证配置参数是否完整
2. 通过 Nacos 服务发现获取语音服务实例
3. 建立与语音服务的 WebSocket 连接
4. 发送配置信息（源语言、目标语言）
5. 收到确认后生成 `chatId` 并返回给客户端

### 3.3 音色克隆启用（可选，REQ_VOICE_CLONE_ENABLE）

```
客户端请求启用音色克隆 → handleVoiceCloneEnable()
  ↓
检查是否已启用
  ↓
初始化音色克隆相关状态
  ↓
返回启用确认（BACK_VOICE_CLONE_ENABLE_ACK）
```

**处理逻辑**：
- 如果已启用，返回 `already_enabled` 状态
- 否则，设置 `voiceCloneEnabled = true`
- 初始化音频缓冲区和时长计数器
- 返回启用确认

### 3.4 音频数据处理（REQ_TRANSLATE_AUDIO_DATA）

```
客户端发送音频数据 → handleTranslateAudioData()
  ↓
解析 Base64 音频数据
  ↓
收集音色克隆音频（如果启用）
  ↓
检查是否达到音色克隆触发条件（≥5秒）
  ↓
触发音色克隆（异步）
  ↓
转发音频数据到语音服务
  ↓
语音服务处理并返回结果
  ↓
处理结果 → handleVoiceServiceResult()
  ↓
更新 asrText 和 transText
  ↓
返回结果给客户端（BACK_ASR_TEXT）
  ↓
如果 isEnd=1，发布翻译结果并重置状态
```

**关键逻辑**：

1. **音频数据解码**：将 Base64 字符串解码为字节数组

2. **音色克隆音频收集**：
   - 如果启用音色克隆且未触发，收集音频数据
   - 累计音频时长（每次增加 320ms）
   - 当达到 5 秒时，触发音色克隆

3. **音色克隆触发**（异步执行）：
   - 调用 `managementServiceClient.cloneVoice()` 接口
   - 成功后返回 `voiceId`（BACK_VOICE_CLONE_RESULT）
   - 失败则返回错误信息

4. **结果处理**：
   - 根据检测到的语言，确定原文和译文
   - 如果检测语言 = 源语言：原文=源语言文本，译文=目标语言文本
   - 如果检测语言 = 目标语言：原文=目标语言文本，译文=源语言文本
   - 发送结果给客户端

### 3.5 停止翻译（REQ_TRANSLATE_STOP）

```
客户端发送停止请求 → handleTranslateStop()
  ↓
发布连接结束事件
  ↓
关闭语音服务连接
```

**处理逻辑**：
- 记录结束时间
- 发布连接结束事件到持久化服务
- 关闭与语音服务的 WebSocket 连接

### 3.6 连接关闭（afterConnectionClosed）

```
连接关闭 → afterConnectionClosed()
  ↓
从心跳管理器注销
  ↓
发布连接结束事件
  ↓
关闭语音服务连接
  ↓
清理 SessionState
```

## 四、音色克隆详细流程

```
启用音色克隆
  ↓
收集音频数据（每次320ms）
  ↓
累计时长达到5秒
  ↓
触发音色克隆（异步）
  ↓
调用管理服务接口
  ↓
返回结果（成功/失败）
```

**音色克隆触发条件**：
- `voiceCloneEnabled = true`
- `voiceCloneTriggered = false`
- `voiceCloneAudioDurationMs >= 5000`

## 五、数据持久化

### 5.1 翻译结果持久化

在以下场景会调用 `translationPersistenceService.persistTranslateResult()`：

1. **句子结束**（`isEnd=1`）：
   - 保存原文、译文、语言信息、时间戳等

2. **连接结束**：
   - 保存连接开始时间、结束时间、持续时间

### 5.2 TranslateResultMessage 包含的信息

- `chatId`: 会话ID
- `userId`: 用户ID
- `originalText`: 原文
- `translatedText`: 译文
- `sourceLanguage/targetLanguage`: 源/目标语言
- `detectedLanguage`: 检测到的语言
- `timestamp`: 时间戳
- `isEnd`: 是否结束
- `startTime/endTime`: 连接开始/结束时间

## 六、错误处理

### 6.1 错误响应格式

```json
{
  "event": "BACK_ERROR",
  "payload": {
    "pdata": {
      "errorCode": "错误代码",
      "errorMessage": "错误信息",
      "stage": "错误阶段（config/process/voice_service）"
    }
  }
}
```

### 6.2 常见错误场景

1. **配置错误**（CONFIG_ERROR）：
   - 配置未找到
   - 配置参数不完整

2. **参数错误**（ILLEGAL_ARGUMENT）：
   - Base64 解码失败
   - 音频数据为空
   - 用户ID为空（音色克隆）

3. **处理错误**（PROCESS_ERROR）：
   - 音频处理异常
   - 音色克隆失败
   - 停止翻译异常

4. **语音服务错误**（VOICE_SERVICE_ERROR）：
   - 语音服务连接失败
   - 语音服务处理异常

## 七、心跳机制

- 客户端每 10 秒发送一次心跳（REQ_HEARTBEAT）
- 服务端更新最后活跃时间
- 返回心跳响应（BACK_HEARTBEAT），包含客户端时间戳和服务端时间

## 八、与语音服务的交互

### 8.1 VoiceServiceWebSocketClient

这是一个内部 WebSocket 客户端，用于与语音服务通信：

- **连接建立**：从 Nacos 获取服务实例，建立连接
- **发送配置**：发送源语言和目标语言配置
- **发送音频**：转发客户端音频数据
- **接收结果**：接收识别和翻译结果，通过回调返回

### 8.2 回调机制

```java
ResultCallback {
    onConfigAck()              // 配置确认
    onResult(translations, detectedLanguage, isEnd)  // 识别结果
    onError(errorCode, errorMessage)                // 错误信息
}
```

## 九、状态管理

### 9.1 运行时状态重置

在每次配置更新或句子结束时，会重置运行时状态：
- `asrText = ""`
- `transText = ""`
- `detectedLang = null`

### 9.2 会话状态清理

连接关闭时：
- 从 `sessionStates` Map 中移除
- 关闭语音服务连接
- 发布连接结束事件

## 十、关键设计点

1. **异步音色克隆**：使用 `CompletableFuture.runAsync()` 异步执行，避免阻塞音频处理
2. **状态隔离**：每个会话维护独立的状态，支持多客户端并发
3. **服务发现**：通过 Nacos 动态获取语音服务实例
4. **错误恢复**：完善的错误处理和日志记录
5. **数据持久化**：翻译结果自动保存到数据库
