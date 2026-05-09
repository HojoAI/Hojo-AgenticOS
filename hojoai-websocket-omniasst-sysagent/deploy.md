# 部署说明：hojoai-websocket-omniasst-sysagent

可运行模块为 **`hojoai-websocket-omniasst-sysagent-server`**，默认端口 **`12003`**，默认无 servlet context-path（业务路径常含 `/omniasst-sysagent`，以实际 Controller 为准），产物：`hojoai-websocket-omniasst-sysagent-server/target/hojoai-websocket-omniasst-sysagent-server-1.0.0-SNAPSHOT.jar`。

运行依赖：**Nacos**、**Redis**、阿里云 RTC、Coze OAuth、模型 API、友盟推送、TTS 等。完整键名与默认值以 `hojoai-websocket-omniasst-sysagent-server/src/main/resources/config/application-dev.properties` 为准；也可用 Nacos 覆盖。

## 运行约定

| 项目 | 说明 |
| --- | --- |
| 可运行模块 | `hojoai-websocket-omniasst-sysagent-server` |
| 默认 HTTP 端口 | `12003`（WebSocket 升级与 SSE 共用） |
| Servlet context-path | 默认无；业务路径包含 `/omniasst-sysagent` |
| Spring 应用名 | `hojoai-websocket-omniasst-sysagent` |
| 经网关访问 | [hojoai-gateway-omniasst](../hojoai-gateway-omniasst) 等：`ws://{网关}/omniasst-sysagent`，HTTP 前缀与网关 `Path` 保持一致 |

本地联调至少保证 **Redis** 与 **management 注册发现（或等价环境）** 可用，否则会话创建与缓存能力可能失败。

## 环境变量（速查）

| 分组 | 变量 |
| --- | --- |
| Nacos | `NACOS_SERVER_ADDR`、`NACOS_USERNAME`、`NACOS_PASSWORD`；**可选**：`NACOS_CONFIG_*`（不设则回退） |
| Redis | `OMNIASST_REDIS_HOST`、`OMNIASST_REDIS_PASSWORD`；**可选**：`OMNIASST_REDIS_PORT`（默认 6379） |
| 阿里云 RTC | `ALIYUN_RTC_APP_ID`、`ALIYUN_RTC_APP_KEY` |
| Coze | `COZE_OAUTH_CLIENT_ID`、`COZE_OAUTH_PUBLIC_KEY` |
| 模型 / 推送 / TTS | `HOJOAI_MODEL_API_URL`、`UMENG_PUSH_APP_KEY`、`UMENG_PUSH_MESSAGE_SECRET`、`UMENG_PUSH_MASTER_SECRET`、`HOJOAI_TTS_API_KEY` |
| 常用 | **可选**：`SPRING_PROFILES_ACTIVE`、`SERVER_PORT` |

敏感信息用 **Secret** / Nacos，勿写进镜像。`k8s-Deployment.yaml` 中 `${APP_NAME}` 等为 **`envsubst` 渲染 YAML** 用。

镜像内含 DingRTC 原生库（`lib/x86_64`），当前 Dockerfile 以 **x86_64** 为主；其它架构需自行调整。

---

## Maven 与本地运行

- **要求**：JDK 21、Maven 3.6+（与根目录 `Dockerfile` 中 Dragonwell 21 一致）
- **根目录**：`mvn clean package -DskipTests`（默认 **dev**）
- 运行：`java -jar hojoai-websocket-omniasst-sysagent-server/target/hojoai-websocket-omniasst-sysagent-server-1.0.0-SNAPSHOT.jar`

---

## Docker

根目录先 `mvn clean package -DskipTests`，再：

```bash
docker build -t hojoai-websocket-omniasst-sysagent:1.0.0-SNAPSHOT .
docker run --rm -p 12003:12003 -e SPRING_PROFILES_ACTIVE=dev \
  hojoai-websocket-omniasst-sysagent:1.0.0-SNAPSHOT
```

按环境补上表中的环境变量（或 Nacos）。

---

## Kubernetes

`k8s-Deployment.yaml` 为 **Deployment 模板**，占位符：`APP_NAME`、`NAMESPACE`、`REPLICAS`、`IMAGE_URL`、`CONTAINER_PORT`（默认 **12003**）、`CPU_LIMIT` / `MEMORY_LIMIT` / `CPU_REQUEST` / `MEMORY_REQUEST`。

```bash
export APP_NAME=hojoai-websocket-omniasst-sysagent NAMESPACE=default REPLICAS=2 \
  IMAGE_URL=your-registry.example.com/.../hojoai-websocket-omniasst-sysagent:1.0.0-SNAPSHOT \
  CONTAINER_PORT=12003 CPU_LIMIT=1 MEMORY_LIMIT=1Gi CPU_REQUEST=200m MEMORY_REQUEST=512Mi
envsubst < k8s-Deployment.yaml | kubectl apply -f -
```

清单**不含 Service/Ingress**；可按需配置 Actuator 探针。

---

**建议流水线**：Maven 打包 → Docker build/push → 渲染清单 `kubectl apply`（或 GitOps）。
