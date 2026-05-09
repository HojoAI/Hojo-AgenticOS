# Voice 基础服务 HTTP 接口文档

## 一、概述

Voice 服务是纯算法 API 封装层，为业务服务（Translate、Meeting等）提供语音相关的基础算法能力。

**服务定位**：
- ✅ 纯算法 API 封装
- ✅ 支持多厂商对接
- ❌ 不包含业务逻辑
- ❌ 不直接面向端侧

---

## 二、接口清单

### 2.1 VAD 语音活动检测

**接口地址**：`POST /v1/vad`

**请求参数**：`VADRequest`
```json
{
  "vid": "unique-id",
  "packageData": "base64-encoded-audio",
  "packageSize": 10240,
  "vadEnd": 60,
  "vadDrop": 0
}
```

**响应格式**：`Response<VADResponse>`
```json
{
  "code": 200,
  "message": "OK",
  "data": {
    "rtn": 0,
    "vadData": [0, 1, 1, 0, ...]
  }
}
```

---

### 2.2 实时 ASR 识别

**接口地址**：`POST /v1/asr/realtime`

**请求参数**：`ASRRequest`
```json
{
  "vid": "unique-id",
  "task": "all",
  "packageIndex": 0,
  "packageData": "base64-encoded-audio",
  "packageSize": 10240,
  "packageEnd": 0,
  "type": "pcm",
  "lang": "all",
  "vadEnd": 60,
  "doItn": 1,
  "doLangDetect": 1
}
```

**响应格式**：`Response<ASRResponse>`
```json
{
  "code": 200,
  "message": "OK",
  "data": {
    "rtn": 0,
    "isEnd": 0,
    "langDetect": "chinese",
    "text": {
      "text": "识别文本",
      "fixedText": "固定文本",
      "tmpText": "临时文本"
    }
  }
}
```

---

### 2.3 音频翻译（ASR + 翻译）

**接口地址**：`POST /v1/translate`

**请求参数**：`AsrTransRequest`
```json
{
  "vid": "unique-id",
  "packageIndex": 0,
  "packageData": "base64-encoded-audio",
  "packageSize": 10240,
  "packageEnd": 0,
  "type": "pcm",
  "lang": "english",
  "vadEnd": 600,
  "doPunc": 0,
  "doLangDetect": 0,
  "doItn": 0,
  "doSub": 0
}
```

**响应格式**：`Response<AsrTransResponse>`
```json
{
  "code": 200,
  "message": "OK",
  "data": {
    "rtn": 0,
    "msg": "success",
    "text": {
      "text": "翻译后的文本"
    }
  }
}
```

---

### 2.4 文本翻译（NLU 兜底）

**接口地址**：`POST /v1/nlu/translate`

**请求头**：`Authorization: your-token`

**请求参数**：`TranslateModelRequest`
```json
{
  "text": "需要翻译的文本",
  "lang": "english",
  "style": "默认",
  "context": null
}
```

**响应格式**：`Response<TranslateModelResponse>`
```json
{
  "code": 200,
  "message": "OK",
  "data": {
    "statusCode": 200,
    "translateResponseData": {
      "translation": "翻译结果"
    }
  }
}
```

---

### 2.5 声纹识别

**接口地址**：`POST /v1/voiceprint/recognize`

**请求参数**：`VoiceprintRequest`
```json
{
  "vid": "unique-id",
  "audio": "base64-encoded-audio",
  "audioLength": 32000
}
```

**响应格式**：`Response<VoiceprintResponse>`
```json
{
  "code": 200,
  "message": "OK",
  "data": {
    "rtn": 0,
    "msg": "success",
    "voiceprint": [0.1, 0.2, 0.3, ...]
  }
}
```

---

## 三、通用说明

### 3.1 响应格式

所有接口统一使用 `Response<T>` 封装：

```json
{
  "code": 200,          // 状态码
  "message": "OK",      // 状态消息
  "data": {             // 实际数据
    // 具体的响应数据
  }
}
```

### 3.2 音频格式

- **格式**：PCM
- **采样率**：16kHz
- **位深度**：16bit
- **声道**：单声道
- **编码**：Base64

### 3.3 错误处理

当接口调用失败时，`Response.code` 不等于 200，或者 `data.rtn` 不等于 0。

---

## 四、使用场景

| 接口 | 使用场景 | 调用服务 |
|------|---------|---------|
| `/v1/vad` | VAD 语音活动检测 | Translate、Meeting |
| `/v1/asr/realtime` | 实时 ASR 识别 | Translate、Meeting |
| `/v1/translate` | 音频翻译 | Translate |
| `/v1/nlu/translate` | 文本翻译（兜底） | Translate |
| `/v1/voiceprint/recognize` | 声纹识别 | Meeting |

---

## 五、配置说明

Voice 服务需要配置以下内容：

```properties
# 算法服务地址
ai.asr.host=https://asr-service-url
ai.voiceprint.host=https://voiceprint-service-url

# NLU 服务配置
autoflow.model.nlu.url=https://nlu-service-url
autoflow.model.nlu.authorization=your-token
```

---

**版本**：v1.0  
**更新日期**：2025-12-26

