# 会议 REST 接口 · 参会人 / 发言人（补充）

本文档说明**本次迭代**在会议 HTTP 接口上的变更：新增接口 1 个；既有接口行为调整 1 处；其余为服务端内部逻辑（无新端点）。

---

## 1. 变更概览

| 类型 | 说明 |
|------|------|
| **新增** | `POST /v1/meeting/speaker/rename`：按 `speakerId` 批量修改会议内该说话人的展示名，并同步 `meetings` 表中的 `segments_json`、`attendees` |
| **行为调整** | `GET /v1/meeting/{meetingId}`：当存在 `segmentsJson` 时，响应中的 **`attendees` 由服务端按段数据实时计算**（顺序见下文），不再仅依赖库内 `attendees` 字段的原始 JSON |
| **无新接口** | 会议标题 ≤25 字截断、持久化时参会人列表写入规则等均在服务端内部完成，不暴露独立 API |

---

## 2. 基础信息

| 项 | 说明 |
|----|------|
| 服务 | `hojoai-webmvc-omniasst-meeting` |
| Context Path | `/omniasst-meeting`（见 `application.properties`） |
| 默认端口 | `12007`（以实际部署为准） |
| 会议模块路径前缀 | `/v1/meeting` |

**完整 Base URL 示例：**

```text
http://{host}:12007/omniasst-meeting
```

---

## 3. 统一响应结构

与项目内其他接口一致，使用 `Response<T>`（字段名以 `hojoai-common` 实际定义为准）：

| 字段 | 类型 | 说明 |
|------|------|------|
| resCode | String | 成功多为 `OK` |
| resMessage | String | 描述信息 |
| resContent | T | 业务数据 |

参数校验失败、业务异常（如会议不存在、无权操作）可能抛出异常，HTTP 状态码与错误体以网关 / 全局异常处理为准。

---

## 4. 新增接口：发言人改名

### 4.1 描述

将指定会议下，某一 `speakerId` 在所有转写段（`meeting_segments`）上的 **`speaker_name`** 批量更新为新的展示名；随后刷新会议主表中的 **`segments_json`**，并按规则重算 **`attendees`** JSON 后落库。

**权限：** `userId` 必须与该会议记录的归属用户一致，否则拒绝。

### 4.2 请求

| 项 | 值 |
|----|-----|
| Method | `POST` |
| Path | `/omniasst-meeting/v1/meeting/speaker/rename` |
| Content-Type | `application/json` |

**Body（JSON）：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| meetingId | String | 是 | 会议 ID |
| userId | String | 是 | 当前操作人用户 ID，**须与会议归属用户一致** |
| speakerId | String | 是 | 说话人 ID，与转写段上的 `speaker_id` 一致 |
| speakerName | String | 是 | 修改后的展示名（首尾空格会被 trim） |

**请求示例：**

```json
{
  "meetingId": "meeting-xxx",
  "userId": "user-xxx",
  "speakerId": "spk-001",
  "speakerName": "张三"
}
```

### 4.3 响应

成功时 `resContent` 为字符串 **`success`**。

**响应示例：**

```json
{
  "resCode": "OK",
  "resMessage": "...",
  "resContent": "success"
}
```

### 4.4 错误说明（逻辑层）

| 场景 | 说明 |
|------|------|
| 会议不存在 | 抛出业务/非法参数类异常（如 `IllegalArgumentException: 会议不存在`） |
| userId 与会议归属不一致 | `IllegalArgumentException: 无权操作该会议` |
| 未命中任何段 | 仍可能返回成功，但服务端会打 warn 日志（`updated <= 0`） |

---

## 5. 既有接口行为调整：会议详情

### 5.1 接口信息

| 项 | 值 |
|----|-----|
| Method | `GET` |
| Path | `/omniasst-meeting/v1/meeting/{meetingId}` |

### 5.2 `attendees` 字段说明（本次调整）

当会议存在 **`segmentsJson`** 且解析后**非空**时：

- 响应中的 **`attendees`**（`List<String>`）由服务端根据**全部转写段**计算；
- **展示顺序**为：  
  1. **用户本人**：段的 `speaker_id` 与会议 `userId` 一致；  
  2. **具名说话人**（含声纹匹配的昵称、用户通过改名接口修改后的名字等）：展示名**不匹配**匿名格式 `发言人`+数字；  
  3. **匿名说话人**：名称匹配 `^发言人\d+$`（如 `发言人1`、`发言人2`），按数字升序。

若不存在段数据或 `segmentsJson` 为空，则仍回退为读取库内存储的 `attendees` JSON（与旧行为兼容）。

---

## 6. 相关内部逻辑（无独立 HTTP 接口）

| 能力 | 说明 |
|------|------|
| 会议标题 | 模型生成后服务端按 **25 个 Unicode 码点**做截断再落库 |
| 会议起止时间 | 仍为 WebSocket 流程中已有字段，未新增 REST |
| 参会人持久化 | 会议总结落库时会用与第 5.2 节**一致规则**写入 `meetings.attendees`，避免与 Hojo 返回列表混用 |

---

## 7. 与客户端的对接建议

- 展示「参会人」列表：直接消费 **`GET /v1/meeting/{meetingId}`** 的 `attendees` 即可（已含排序）。
- 用户在原文中修改发言人名称：调用 **`POST .../speaker/rename`**，成功后再次拉详情或依赖本地更新段列表与参会人。

---

*文档版本：与 `hojoai-webmvc-omniasst-meeting` 仓库中 `MeetingController`、`MeetingServiceImpl` 实现保持一致；若代码变更请同步修订本文档。*
