# 会议场景 Redis 推送数据文档

本文档描述会议场景中，通过 Redis Pub/Sub 推送的数据格式、推送时机和频道信息。

## 通用说明

### Redis 频道命名规则

所有 Redis 频道都有统一的前缀：`Omniasst:Voice:`

实际频道名称 = `Omniasst:Voice:` + 频道名

例如：
- 频道名：`Meeting:Segment`
- 实际频道：`Omniasst:Voice:Meeting:Segment`

### 消息格式

所有消息都是 JSON 格式字符串，通过 `JacksonUtil.bean2Json()` 序列化后发送。

### 异步发送

所有 Redis 推送都是异步的（`@Async`），发送失败不会影响主流程，只记录日志。

---

## 一、会议场景（Meeting）

### 1. 频道信息

#### 1.1 单段完成频道

**频道名**：`Meeting:Segment`  
**完整频道**：`Omniasst:Voice:Meeting:Segment`

#### 1.2 会议完整结果频道

**频道名**：`Meeting:Result`  
**完整频道**：`Omniasst:Voice:Meeting:Result`

### 2. 推送时机

#### 2.1 Meeting:Segment（单个段完成）- 实时推送

**时机**：每个音频段处理完成时立即推送

**触发方法**：`publishSegmentComplete(meetingId, segmentInfo)`

**触发场景**：
1. ASR识别到 `isEnd=1`，完成当前段时
2. 客户端主动停止会议时，先完成当前段
3. 连接异常断开时，如果有未完成的段，先完成当前段

**数据特点**：
- `segment` 对象包含当前完成的这一个段（不是数组）
- 不包含 `ossUrl`（会议未结束，WAV文件未生成）
- 不包含 `totalDuration`、`startTime`、`endTime`

#### 2.2 Meeting:Result（会议完整结果）- 最终推送

**时机**：会议完全结束时推送

**触发方法**：`publishMeetingResult(meetingId, ossUrl, totalDuration, startTime, endTime, needSave)`

**触发场景**：
1. 客户端主动停止会议（`REQ_MEETING_STOP`）
2. 连接异常断开（心跳超时或网络断开）

**处理流程**：
```
1. 完成当前段（如果有）→ 推送 Meeting:Segment
2. 从OSS下载完整PCM文件
3. PCM转WAV格式
4. 上传WAV文件到OSS，获取ossUrl
5. 推送 Meeting:Result（包含OSS URL + 会议元数据 + needSave标识）
```

**数据特点**：
- 不包含 `segments` 数组（段数据已通过 `Meeting:Segment` 推送）
- 包含 `ossUrl`（完整的WAV文件URL）
- 包含 `totalDuration`（总有效音频时长）
- 包含 `startTime`（会议开始时间）和 `endTime`（会议结束时间）
- 包含 `needSave`（是否需要保存：0=不保存，1=保存）
- 正常关闭时，`needSave` 由客户端在 `REQ_MEETING_STOP` 请求中指定
- 异常关闭时（心跳超时、应用异常关闭等），默认 `needSave=1`（保存）

### 3. 数据格式（MeetingResultMessage）

**说明**：`Meeting:Segment` 和 `Meeting:Result` 使用同一个实体类 `MeetingResultMessage`，根据推送场景不同，设置的字段也不同。

#### 3.1 Meeting:Segment 数据格式

```json
{
  "meetingId": "meeting-1234567890123456789",
  "segment": {
    "segmentId": "seg-1234567890123456789",
    "text": "你好，这是一段测试文本",
    "speakerId": "user-123",
    "speakerName": "张三",
    "speakerAvatar": "https://example.com/avatar.jpg",
    "startTime": 0,
    "endTime": 2000,
    "duration": 2000
  },
  "timestamp": 1704067200000
}
```

**字段说明**：
- 只包含 `meetingId`、`segment`、`timestamp` 字段
- `segment` 中包含 `speakerId`、`speakerName`、`speakerAvatar` 等讲话人信息
- 其他字段（`userId`、`ossUrl`、`totalDuration`、`startTime`、`endTime`、`needSave`）为 `null`，不会序列化

#### 3.2 Meeting:Result 数据格式

```json
{
  "meetingId": "meeting-1234567890123456789",
  "userId": "user-123",
  "ossUrl": "https://oss.example.com/meeting/meeting-1234567890123456789/audio.wav",
  "totalDuration": 60000,
  "startTime": 1704067200000,
  "endTime": 1704067260000,
  "needSave": 1,
  "timestamp": 1704067260000
}
```

**字段说明**：
- 只包含 `meetingId`、`userId`、`ossUrl`、`totalDuration`、`startTime`、`endTime`、`needSave`、`timestamp` 字段
- `userId` 从 WebSocket session attributes 中获取
- `segment` 字段为 `null`，不会序列化
- 段数据已经通过 `Meeting:Segment` 频道推送过了，订阅服务如果需要所有段的信息，应该订阅 `Meeting:Segment` 频道

### 4. 字段说明

#### MeetingResultMessage 字段

**说明**：`Meeting:Segment` 和 `Meeting:Result` 使用同一个实体类，根据推送场景不同，设置的字段也不同。未设置的字段为 `null`，由于使用了 `@JsonInclude(NON_NULL)`，不会序列化到 JSON 中。

| 字段 | 类型 | 必填 | 说明 | Meeting:Segment | Meeting:Result |
|------|------|------|------|----------------|----------------|
| `meetingId` | String | 是 | 会议ID | ✅ | ✅ |
| `timestamp` | Long | 是 | 消息发送时间戳（毫秒） | ✅ | ✅ |
| `userId` | String | 否 | 用户ID（从WebSocket session获取） | ❌ | ✅ |
| `segment` | SegmentInfo | 否 | 音频段信息（单个对象） | ✅ | ❌ |
| `ossUrl` | String | 否 | WAV音频文件在OSS中的访问URL | ❌ | ✅ |
| `totalDuration` | Long | 否 | 总有效音频时长（毫秒） | ❌ | ✅ |
| `startTime` | Long | 否 | 会议开始时间（连接建立时间，毫秒） | ❌ | ✅ |
| `endTime` | Long | 否 | 会议结束时间（连接断开时间，毫秒） | ❌ | ✅ |
| `needSave` | Integer | 否 | 是否需要保存：0=不保存，1=保存 | ❌ | ✅ |

#### SegmentInfo 字段

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `segmentId` | String | 是 | 段ID |
| `text` | String | 是 | ASR识别的文本内容 |
| `speakerId` | String | 是 | 说话人ID（通过声纹识别匹配，可能是用户ID或会议内编号） |
| `speakerName` | String | 否 | 说话人名称（匹配到用户时返回用户名，会议内编号时返回"UserName-1"格式） |
| `speakerAvatar` | String | 否 | 说话人头像URL（匹配到用户时返回头像，为空时返回空字符串） |
| `startTime` | Long | 是 | 在总时间轴中的开始偏移（毫秒） |
| `endTime` | Long | 是 | 在总时间轴中的结束偏移（毫秒） |
| `duration` | Long | 是 | 段的时长（毫秒） |

### 5. 时间字段说明

- **`startTime`（会议开始时间）**：WebSocket连接建立时记录，存储在 `SessionState.meetingStartTime`
- **`endTime`（会议结束时间）**：会议结束时记录，使用 `System.currentTimeMillis()`
- **`totalDuration`（总有效音频时长）**：所有有效音频段的总时长（经过VAD过滤），用于播放时定位
- **`segment.startTime/endTime`（段偏移）**：相对于总时间轴的偏移，用于播放时定位每个段的位置

### 6. 推送流程示例

```
会议进行中：
  音频段1完成 → 📢 Meeting:Segment (段1)
  音频段2完成 → 📢 Meeting:Segment (段2)
  音频段3完成 → 📢 Meeting:Segment (段3)
  
会议结束（主动停止或异常断开）：
  完成最后一段 → 📢 Meeting:Segment (最后一段)
  下载PCM → 转WAV → 上传OSS → 📢 Meeting:Result (OSS URL + 会议元数据 + needSave标识)
```

---

## 二、订阅示例

### Redis 订阅代码示例（Java）

```java
@Autowired
private RedisTemplate<String, String> redisTemplate;

public void subscribe() {
    redisTemplate.execute(new RedisCallback<Object>() {
        @Override
        public Object doInRedis(RedisConnection connection) throws DataAccessException {
            connection.subscribe((message, pattern) -> {
                String channel = new String(message.getChannel());
                String body = new String(message.getBody());
                
                if (channel.equals("Omniasst:Voice:Meeting:Segment")) {
                    MeetingResultMessage result = JacksonUtil.json2Bean(body, MeetingResultMessage.class);
                    System.out.println("Segment completed: " + result.getSegment().getText());
                } else if (channel.equals("Omniasst:Voice:Meeting:Result")) {
                    MeetingResultMessage result = JacksonUtil.json2Bean(body, MeetingResultMessage.class);
                    System.out.println("Meeting ended: " + result.getMeetingId() + ", needSave: " + result.getNeedSave());
                }
            }, 
            "Omniasst:Voice:Meeting:Segment".getBytes(),
            "Omniasst:Voice:Meeting:Result".getBytes()
            );
            return null;
        }
    });
}
```

---

## 三、注意事项

1. **异步推送**：所有推送都是异步的，不会阻塞主流程
2. **失败处理**：推送失败只记录日志，不影响WebSocket连接和业务逻辑
3. **消息顺序**：`Meeting:Segment` 总是在 `Meeting:Result` 之前推送
4. **空数据处理**：使用 `@JsonInclude(JsonInclude.Include.NON_NULL)`，null值不会被序列化
5. **段数据分离**：段数据通过 `Meeting:Segment` 频道推送，`Meeting:Result` 只包含会议元数据

