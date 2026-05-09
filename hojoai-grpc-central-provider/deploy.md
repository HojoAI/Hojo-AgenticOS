# 部署说明：hojoai-grpc-central-provider

可运行模块为 **`hojoai-grpc-central-provider-server`**，HTTP 端口 **`11003`**（`server.port`），gRPC 端口 **`11103`**（`grpc.port`），产物：`hojoai-grpc-central-provider-server/target/hojoai-grpc-central-provider-server-1.0.0-SNAPSHOT.jar`。

运行依赖：**Nacos**、**MySQL**、**Redis**、盘禾、支付宝、友盟、航班等（见配置）。完整键名与默认值以 `hojoai-grpc-central-provider-server/src/main/resources/config/application-dev.properties` 为准；也可用 Nacos 覆盖。

**Firebase（FCM）**：将 `firebase-service-account.json.example` 复制为 **`firebase-service-account.json`** 并填入服务账号 JSON（已在 `.gitignore` 中忽略），勿提交密钥。

## 环境变量（速查）

| 分组 | 变量 |
| --- | --- |
| Nacos | `NACOS_SERVER_ADDR`、`NACOS_USERNAME`、`NACOS_PASSWORD`；**可选**：`NACOS_CONFIG_*`（不设则回退） |
| 数据源 | `DATASOURCE_URL`、`DATASOURCE_USERNAME`、`DATASOURCE_PASSWORD` |
| Redis | `REDIS_HOST`；**可选**：`REDIS_PASSWORD`（Redis 端口在配置文件中固定为 6379，若需覆盖请用 Nacos / 本地 properties） |
| 盘禾 | `PANHE_REQUEST_HTTP_URL`、`PANHE_REQUEST_HTTP_APPKEY`、`PANHE_REQUEST_HTTP_SECRET_KEY`、`PANHE_CALLBACK_URL` |
| 支付宝 | `ALIPAY_APP_ID`、`ALIPAY_APP_PRIVATE_KEY`、`ALIPAY_PUBLIC_KEY`、`ALIPAY_CALLBACK_URL`；**可选**：`ALIPAY_OPEN_API_URL` |
| 友盟 / 航班 | `UMENG_PUSH_APP_KEY`、`UMENG_PUSH_MASTER_SECRET`；**可选**：`UMENG_PUSH_DEFAULT_ACTIVITY`、小米相关；`VARIFLIGHT_APP_ID`、`VARIFLIGHT_APP_SECURITY` |
| Firebase 代理 | **可选**：`FIREBASE_PROXY_HOST` |
| 常用 | **可选**：`SPRING_PROFILES_ACTIVE`、`SERVER_PORT` |

敏感信息用 **Secret** / Nacos，勿写进镜像。`k8s-Deployment.yaml` 中 **`CONTAINER_PORT_1`（HTTP 11003）**、**`CONTAINER_PORT_2`（gRPC 11103）** 等为 **`envsubst` 渲染 YAML** 用，与 Spring 环境变量不同。

---

## Maven 与本地运行

- **要求**：JDK 21、Maven 3.6+
- **根目录**：`mvn clean package -DskipTests`（默认 **dev**）
- 运行：`java -jar hojoai-grpc-central-provider-server/target/hojoai-grpc-central-provider-server-1.0.0-SNAPSHOT.jar`

---

## Docker

根目录先 `mvn clean package -DskipTests`，再：

```bash
docker build -t hojoai-grpc-central-provider:1.0.0-SNAPSHOT .
docker run --rm -p 11003:11003 -p 11103:11103 -e SPRING_PROFILES_ACTIVE=dev \
  hojoai-grpc-central-provider:1.0.0-SNAPSHOT
```

---

## Kubernetes

`k8s-Deployment.yaml` 为 **Deployment 模板**，占位符：`APP_NAME`、`NAMESPACE`、`REPLICAS`、`IMAGE_URL`、`CONTAINER_PORT_1`（**11003**）、`CONTAINER_PORT_2`（**11103**）、`CPU_LIMIT` / `MEMORY_LIMIT` / `CPU_REQUEST` / `MEMORY_REQUEST`。

```bash
export APP_NAME=hojoai-grpc-central-provider NAMESPACE=default REPLICAS=2 \
  IMAGE_URL=your-registry.example.com/.../hojoai-grpc-central-provider:1.0.0-SNAPSHOT \
  CONTAINER_PORT_1=11003 CONTAINER_PORT_2=11103 \
  CPU_LIMIT=2 MEMORY_LIMIT=2Gi CPU_REQUEST=500m MEMORY_REQUEST=512Mi
envsubst < k8s-Deployment.yaml | kubectl apply -f -
```

集群内 gRPC 客户端应访问 **11103**，并与 Nacos metadata 中 **`grpc-port`** 一致。

---

**建议流水线**：Maven 打包 → Docker build/push → 渲染清单 `kubectl apply`（或 GitOps）。
