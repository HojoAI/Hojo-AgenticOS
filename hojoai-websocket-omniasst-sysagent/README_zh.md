# hojoai-websocket-omniasst-sysagent

**Omniasst 系统助理服务**：提供 HTTP 对话（SSE 流式）与 WebSocket 控制面，支持意图路由到聊天、联网搜、音乐、天气、日程、播客等多智能体链路，并包含耳机智能体、会话治理等核心能力。

## 能力列表

### 对话与意图路由
- chat：通用聊天，通过模型 API
- chat_websearch：联网搜索聊天
- doc：图文内容处理
- translator：翻译
- meeting：会议场景
- event/focus：日程服务（调用 focus 微服务）
- music：音乐服务（调用 music 微服务）
- weather：天气服务（调用 weather 微服务）
- podcast：播客/新闻推荐（调用 news 微服务）

### SSE 事件协议
- ping：心跳（~10秒间隔）
- loading：加载提示（thinking...、searching web...）
- message：核心业务响应（JSON，包含 conversationId、messageId、data 数组）
- error：错误信息
- agentType：子智能体类型宣告（chat、music、weather 等）

### WebSocket 控制面
- BUILD_RTC_TOKEN：请求 RTC Token 签发
- TICKET_RESULT：影票会话绑定

### 耳机智能体（HeadphoneAgent）
- 按业务日复用：userId + 业务日绑定单一会话
- Bubble 复制为 Speech：垂直智能体将 bubble 内容复制为 speech 用于 TTS
- agentType 事件抑制：headphoneMode=true 时抑制主聊 agentType 事件

### 会话治理
- 停止生成：中止 SSE 流
- 删消息：删除单条消息并重载上下文
- 删会话：完整会话清理（含 Redis 缓存）
- 上下文管理：Redis 缓存 + management 同步

### 扩展业务接口
- 电影查询：影片、影院、场次、座位、订单
- 支付回调：支付宝、盘禾支付异步通知
- 推送：友盟推送集成
- OAuth：Coze 平台 Token 获取

对外接口、字段定义、SSE 事件、WebSocket 协议见 **[API.md](./API.md)**。

## 技术栈

- Java 21、Spring Boot 3.x（Web、**WebSocket**、**WebFlux**、Actuator、AOP）
- **Spring Data Redis**、**OpenFeign** + LoadBalancer
- 继承 [hojoai-parent](../hojoai-parent)

## 模块与源码入口

| 模块 | 说明 |
|------|------|
| `hojoai-websocket-omniasst-sysagent-server` | 可运行应用：配置在 `src/main/resources`，主代码在 `.../sysagent/` 下。 |

常用包：

- `controller/conversation` — SSE 对话与删除、停止接口  
- `service/conversation` — 对话流水线、意图与各 `*AgentServiceImpl`  
- `interaction/websocket` — WebSocket 处理与 `IMessage` / `IMessageEvent`  
- `service/api/ConversationAPI` — 调用 management 的 Feign 客户端  

## 快速开始

```bash
cd hojoai-websocket-omniasst-sysagent
mvn clean package -DskipTests
java -jar hojoai-websocket-omniasst-sysagent-server/target/hojoai-websocket-omniasst-sysagent-server-1.0.0-SNAPSHOT.jar
```

完整部署步骤、环境变量与 K8s 模板用法见 **[deploy.md](./deploy.md)**。

## 文档

- **推荐阅读顺序**：先看 `README.md`（模块与能力）→ 再看 `API.md`（接入与协议）→ 最后看 `deploy.md`（运行与发布）。
- **[API.md](./API.md)** — HTTP/SSE 接口、请求体字段、耳机智能体语义、SSE 事件与 WebSocket 协议。
- **[deploy.md](./deploy.md)** — 运行约定、环境变量、Maven/Docker/Kubernetes 部署说明。
