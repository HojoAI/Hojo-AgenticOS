# hojoai-webmvc-omniasst-management

**Omniasst 管理服务**：聚合管理域能力，包括会话与消息、虚拟形象、推送、对象存储预签名、消息中心、翻译会话列表、推荐、会议与声纹/语音配置等 REST 接口。

## 能力列表

### 会话与消息
- 会话管理：创建会话、查询列表、删除会话、更新标题
- 消息管理：发送/追加消息、批量操作、消息替换与删除
- 已读状态：全会话已读检查

### 虚拟形象
- 形象列表：查询所有可用虚拟形象
- 当前形象：获取/切换用户当前虚拟形象

### 推送
- 消息推送：推送通知（支持广播、用户、设备 Token 路由）
- 设备绑定：用户设备 Token 绑定
- FCM 调试：Firebase Cloud Messaging 推送调试

### 对象存储
- 预签名 URL：生成直传 OSS 的预签名 URL
- URL 续期：延长预签名 URL 有效期
- 追加上传：音频分块追加上传（断点续传场景）

### 消息中心
- 消息列表：分页查询、已读/未读计数
- 消息管理：单条/批量标记已读、清空
- 任务中心：进行中任务跟踪与完成

### 翻译会话
- 会话列表：用户翻译会话分页查询
- 消息列表：翻译会话内消息查询

### 推荐
- 推荐列表：按类型查询推荐内容
- 推荐管理：忽略推荐、创建推荐条目

### 会议与声纹
- 会议查询：会议列表、详情查询
- 声纹管理：注册、匹配、查询、删除声纹
- 音色克隆：WAV 音频上传克隆音色
- 声纹注册流程：两阶段声纹注册（prepare + commit）

## 技术栈

- Java 21、Spring Boot Web
- 继承 [hojoai-parent](../hojoai-parent)；含对语音网关等下游的 Feign 调用（内部细节见源码）

## 运行约定

| 项目 | 说明 |
|------|------|
| 可运行模块 | `hojoai-webmvc-omniasst-management-server` |
| 默认 HTTP 端口 | `12005`（以 `application.properties` 为准，部署时勿与网关端口冲突） |
| Servlet context-path | `/omniasst-management/api` |
| Spring 应用名 | `hojoai-webmvc-omniasst-management` |
| 经网关 | [hojoai-gateway-omniasst](../hojoai-gateway-omniasst) `/omniasst-management/**`；完整 URL 需包含 context-path，例如 `{网关}/omniasst-management/api/v0/...` |

## 构建与运行

```bash
cd hojoai-webmvc-omniasst-management
mvn clean install
cd hojoai-webmvc-omniasst-management-server && mvn spring-boot:run
```

## 文档

- [API.md](./API.md) — 各 `/v0`、`/v1` 资源路径说明（表内路径为去掉 context-path 后的 Servlet 路径）。
