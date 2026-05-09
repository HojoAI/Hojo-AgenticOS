# hojoai-webmvc-omniasst-focus — API 文档

## 基址

- **context-path**：`/omniasst-focus`
- 经网关：`{网关}/omniasst-focus/...`

以下路径均相对于 **context-path**。

---

## 日程 v1 — `/api/v1`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/schedule/task` | 日程任务查询 |
| GET | `/api/v1/schedule/free` | 空闲时间等 |
| POST | `/api/v1/schedule/conversation/{userId}` | 日程对话，**SSE**（`text/event-stream`） |
| POST | `/api/v1/schedule/meeting/{userId}` | 会议相关 |
| POST | `/api/v1/schedule` | 创建/提交日程 |
| POST | `/api/v1/schedule/card/{userId}` | 日程卡片 |
| GET | `/api/v1/schedules/{userId}` | 列表 |
| GET | `/api/v1/schedule/{userId}` | 详情 |
| PATCH | `/api/v1/schedule/{userId}/{scheduleId}` | 部分更新 |

## 日程 v2 — `/api/v2`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v2/schedule/flight` | 航班行程信息 |
| GET | `/api/v2/schedule/train` | 火车行程信息 |
| GET | `/api/v2/schedule/map` | 地图相关 |
| GET | `/api/v2/schedule/type-icon` | 类型图标 |
| GET | `/api/v2/schedule/movie-name` | 影片名称解析等 |
| DELETE | `/api/v2/schedule/{scheduleId}` | 删除日程 |
| POST | `/api/v2/schedule/{userId}` | 创建（v2） |
| PUT | `/api/v2/schedule/{scheduleId}` | 全量更新 |
| POST | `/api/v2/schedule/sys-sync/{userId}` | 系统同步 |

---

## 内部依赖（Feign，非对外 API）

`ManagementAPI` 等客户端会请求管理域路径（如 `/omniasst-management/api/...`），仅供服务间调用，**不**作为本服务对外接口。
