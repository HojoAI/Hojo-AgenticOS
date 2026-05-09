# hojoai-webmvc-omniasst-meeting

**Omniasst 会议服务**：提供实时会议转写、说话人识别与音频处理能力。

## 能力列表

- 实时语音识别：实时转写会议音频内容，支持 WebSocket 流式输出
- 说话人识别：基于声纹技术识别不同说话人，支持说话人分离与追踪
- 音频段处理：智能分段，按说话人归类组织会议文本
- OSS 存储：会议音频自动上传至 OSS 保存
- 音色克隆：上传 WAV 音频克隆用户音色
- 声纹注册：两阶段声纹注册流程（上传 + 提交）
- 会议导入：异步转写 OSS 已有的音频文件，支持批量导入
- 会议管理：会议列表查询、详情聚合、参与者管理
- 会议控制：暂停/恢复/停止会议录制

## 技术栈

- Java 21、Spring Boot 3.x（Web、**WebSocket**、Actuator）
- **Spring Data Redis**、**OpenFeign** + LoadBalancer
- 继承 [hojoai-parent](../hojoai-parent)

## 架构说明

Meeting 服务（本服务） → Voice 基础服务 → 算法厂商

详见 Voice 基础服务 HTTP 接口文档。

## 快速开始

```bash
cd hojoai-webmvc-omniasst-meeting
mvn clean package -DskipTests
java -jar hojoai-webmvc-omniasst-meeting-server/target/hojoai-webmvc-omniasst-meeting-server-1.0.0-SNAPSHOT.jar
```

完整部署步骤、环境变量与 K8s 模板用法见 **[deploy.md](./deploy.md)**。

## 文档

- **[API.md](./API.md)** — HTTP/WebSocket 接口、请求体字段、服务间调用说明。
- **[deploy.md](./deploy.md)** — 运行约定、环境变量、Maven/Docker/Kubernetes 部署说明。
