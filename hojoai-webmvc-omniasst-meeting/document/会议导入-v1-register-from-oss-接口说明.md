# 会议导入：登记 OSS 地址（Part1）

## 1. 概述

文件已由**云侧或端侧上传服务**写入 OSS 后，调用本接口向会议服务**登记**一条会议记录，
并启动后续异步流水线（转写、纪要等；流水线具体能力以服务端实现为准）。

- **方法**：`POST`
- **路径**：`/v1/meeting/import/register-from-oss`
- **完整 URL（默认本地）**：`http://{host}:{port}/omniasst-meeting/v1/meeting/import/register-from-oss`  
  - 默认端口见 `application.properties`：`server.port=12007`  
  - 上下文路径：`server.servlet.context-path=/omniasst-meeting`
- **Content-Type**：`application/json`

---

## 2. 请求体（JSON）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `userId` | string | 是 | 用户 ID |
| `ossUrlOrObjectKey` | string | 是 | 他方返回的 **OSS 完整访问 URL**，或 **对象键**（object key）。服务端会规范为对象键入库 |
| `originalFilename` | string | 否 | 用户侧原始文件名，用于默认标题与任务展示。不传时从对象键路径**最后一段**推导（如 `.../meeting-xxx/audio.wav` → `audio.wav`）；仍无则标题侧为「未命名录音」。扩展名优先从此字段解析，否则从对象键最后一段解析 |
| `fileSizeBytes` | long | 否 | 文件大小（字节）。若传则校验 **≤ 500MB** |

**格式约束**：音频扩展名须为 **mp3 / wav / m4a**（优先从 `originalFilename` 解析；若无或不含扩展名则尝试从对象键路径最后一段解析）。

### 请求示例

仅传 OSS URL（预签名查询串可保留，服务端按 path 解析对象键）：

```json
{
  "userId": "user_10001",
  "ossUrlOrObjectKey": "https://your-bucket.oss-cn-hangzhou.aliyuncs.com/omniasst/voice/meeting/M123/audio.wav?x-oss-signature=..."
}
```

可选附带原始文件名与大小：

```json
{
  "userId": "user_10001",
  "ossUrlOrObjectKey": "https://your-bucket.oss-cn-hangzhou.aliyuncs.com/meeting/M123/import.mp3?Expires=...",
  "originalFilename": "项目周会-20250324.mp3",
  "fileSizeBytes": 2097152
}
```

仅对象键示例：

```json
{
  "userId": "user_10001",
  "ossUrlOrObjectKey": "meeting/M1730123456789/import.mp3"
}
```

---

## 3. 响应

HTTP 状态码一般为 **200**。业务是否成功以项目统一 **`Response` 包装**为准（来自 `hojoai-common`），通常包含：

- 状态码字段（如 `resCode` 等，与 `ResponseStatusBasic.OK` 一致表示成功）
- 业务数据字段（如 `resContent` / `data`，以实际序列化字段名为准）

**成功时 `resContent` 对应结构**（类 `MeetingImportRegisterResponse`）：

| 字段 | 类型 | 说明 |
|------|------|------|
| `meetingId` | string | 本系统生成的会议 ID，如 `M` + 雪花 ID |
| `objectKey` | string | 解析后写入库中的 OSS **对象键** |

### 响应示例（示意）

```json
{
  "resCode": "200",
  "resContent": {
    "meetingId": "M1987654321012345678",
    "objectKey": "meeting/M1987654321012345678/import.mp3"
  }
}
```

---

## 4. 失败场景（常见）

以下情况可能返回 **400** 或业务错误（视全局异常处理而定）：

| 场景 | 说明 |
|------|------|
| 参数缺失 | `userId`、`ossUrlOrObjectKey` 为空 |
| 无法解析 OSS | `ossUrlOrObjectKey` 无法解析为有效对象键 |
| 格式不支持 | 扩展名不是 mp3/wav/m4a |
| 文件过大 | 传了 `fileSizeBytes` 且超过 500MB |
| 数据库/依赖异常 | 入库或下游失败，见日志与错误信息 |

异步流水线若尚未接入转写，任务可能在后续步骤标记失败（如 `PIPELINE_NOT_CONFIGURED`），**本接口 HTTP 仍可能返回 200 且返回 `meetingId`**，需结合 **`POST /v1/meeting/import/tasks/list`** 或详情接口查看状态。

---

## 5. 相关接口（同模块 v1）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v1/meeting/import/register-from-oss` | **本文档** |
| POST | `/v1/meeting/import/batch-init` | 本服务预签名直传（可选） |
| POST | `/v1/meeting/import/upload-complete` | 预签名上传完成回调（可选） |
| POST | `/v1/meeting/import/tasks/list` | 处理中队列 |
| GET | `/v1/meeting/import/processing-count?userId=` | 处理中数量 |
| POST | `/v1/meeting/import/read` | 清除「新」标记 |

以上路径均需加前缀：`/omniasst-meeting`。

---

## 6. 服务端配置说明（直处理链路）

登记成功后，服务端异步执行 **下载 OSS →（mp3/m4a 经 ffmpeg 转 PCM）→ ASR WebSocket 推流 → 落库与纪要**。以下配置在 `application-*.properties` 或 Nacos 等配置中心设置，**调用 HTTP 接口的客户端无需传参**。

### 6.1 `meeting.import.direct.asr.pace-realtime-factor`

| 项 | 说明 |
|----|------|
| **含义** | 向 ASR 每推送一帧 PCM 后的休眠比例：休眠毫秒数 = **该帧对应音频时长(ms) × 本系数**。用于避免「瞬间塞满整段长音频」导致识别端缓冲溢出、**只返回空 text**。 |
| **类型** | `double`，服务端会限制在 **0～2** 之间再参与计算。 |
| **默认** | `0.25`（约 **4 倍于实时** 的推流速度：例如 **15 分钟** 音频，仅推流阶段约 **3～4 分钟**；另含解码、ASR 收尾、模型纪要等耗时）。 |
| **调参** | 若出现 **转写结果为空或极少**：适当 **增大**（如 `0.4`、`0.5`，直至 `1.0` 与线上一致最稳）。若在保证有字的前提下仍嫌慢：在可接受范围内 **略减小**（如 `0.15`、`0.1`）。**`0`** 表示不休眠，长录音极易复现 ASR 无结果，不推荐。 |

### 6.2 其他常用项（可选）

| 配置键 | 默认 | 说明 |
|--------|------|------|
| `meeting.import.direct.asr.final-wait-seconds` | `10` | 发送结束帧后等待 ASR 回传收尾的秒数；长文件可适当加大。 |
| `meeting.import.ffmpeg.path` | `ffmpeg` | mp3/m4a 解码依赖系统 **ffmpeg**；Docker 镜像内需安装或内置二进制，必要时填绝对路径（如 `/usr/local/bin/ffmpeg`）。 |
| `meeting.import.ffmpeg.enabled` | `true` | 关闭后无法走 ffmpeg 解码分支（仅 wav 仍可用 Java 解码）。 |
| `meeting.import.ffmpeg.timeout-seconds` | `3600` | 单次 ffmpeg 解码超时（秒）。 |

---

## 7. 在 Apifox 中测试

### 7.1 新建环境

1. 打开 **环境管理**，新建环境（如「本地会议服务」）。
2. 增加变量：
   - `baseUrl` = `http://127.0.0.1:12007`（或你的网关地址，**不要**把 `/omniasst-meeting` 写进 host 时重复路径，见下条）
3. 若 Apifox 里 **完整 URL** 写法为：`{{baseUrl}}/omniasst-meeting/v1/meeting/import/register-from-oss`  
   - 则 `baseUrl` 不要带尾部斜杠。

### 7.2 新建接口

1. **新建接口** → 方法选 **POST**。
2. **URL** 填：`{{baseUrl}}/omniasst-meeting/v1/meeting/import/register-from-oss`
3. **Header**：`Content-Type: application/json`
4. **Body** → **raw** → **JSON**，粘贴第二节请求示例，把 `ossUrlOrObjectKey` 换成你们**真实可访问**的测试文件地址或对象键。

### 7.3 鉴权（若有）

若网关或 Spring Security 要求 Token：

- 在 Apifox **环境**或 **接口** 的 **Auth** / **Header** 中增加与线上一致的字段（如 `Authorization: Bearer xxx`），否则可能 401/403。

### 7.4 发送与查看结果

1. 点击 **发送**。
2. 在 **响应体** 中查看外层 `Response` 是否成功，并取出 `meetingId`、`objectKey`。
3. 可用 **后置操作** 把 `meetingId` 写入环境变量，便于接着测：
   - `GET {{baseUrl}}/omniasst-meeting/v1/meeting/{{meetingId}}`（会议详情，需与 `MeetingController` 路径一致）。

### 7.5 导入 OpenAPI（可选）

若团队用 Apifox **导入 OpenAPI**：可在项目里后续补 `springdoc-openapi` 由代码生成；当前可手动按本文档在 Apifox 建一条接口，并复制示例 JSON。

---

## 8. curl 示例（本地）

```bash
curl -X POST "http://127.0.0.1:12007/omniasst-meeting/v1/meeting/import/register-from-oss" ^
  -H "Content-Type: application/json" ^
  -d "{\"userId\":\"test_user\",\"ossUrlOrObjectKey\":\"meeting/Mxxx/import.mp3\"}"
```

（Linux/macOS 将 `^` 换为 `\` 并调整引号即可。）
