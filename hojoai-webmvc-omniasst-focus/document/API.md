# hojoai-omniasst-focus HTTP API 一览

基址：`http://{host}:12008/omniasst-focus`（端口与 context-path 以 `application.properties` 为准）。

## API v1（`/api/v1`）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/schedule/conversation/{userId}` | AI 对话创建日程（SSE） |
| POST | `/schedule/meeting/{userId}` | 会议相关创建 |
| POST | `/schedule` | 创建日程 |
| POST | `/schedule/card/{userId}` | 卡片创建 |
| GET | `/schedules/{userId}` | 日程列表 |
| GET | `/schedule/{userId}` | 日程详情 |
| PATCH | `/schedule/{userId}/{scheduleId}` | 更新日程 |
| GET | `/schedule/task` | 定时分析任务触发（query: userId） |
| GET | `/schedule/free` | 空闲建议类任务触发 |

## API v2（`/api/v2`）

### 日程

| 方法 | 路径 | 说明 |
|------|------|------|
| DELETE | `/schedule/{scheduleId}` | 删除日程 |
| POST | `/schedule/{userId}` | 创建日程 |
| PUT | `/schedule/{scheduleId}` | 更新日程 |
| POST | `/schedule/sys-sync/{userId}` | 系统同步日程 |

### 外部查询

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/schedule/flight` | 航班查询 |
| GET | `/schedule/train` | 火车查询 |
| GET | `/schedule/map` | 地图查询 |
| GET | `/schedule/type-icon` | 类型图标 |
| GET | `/schedule/movie-name` | 影视名称 |

请求体与 Query 参数以各 `Controller` 方法签名为准；统一响应封装为 `com.hojoai.common.response.Response`（异常见全局 `IExceptionHandler`）。
