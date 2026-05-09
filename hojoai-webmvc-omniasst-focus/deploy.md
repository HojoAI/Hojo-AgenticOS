# 部署说明：hojoai-webmvc-omniasst-focus

可运行模块为 **`hojoai-webmvc-omniasst-focus-server`**，默认端口 **`12008`**，上下文 **`/omniasst-focus`**，产物：`hojoai-webmvc-omniasst-focus-server/target/hojoai-webmvc-omniasst-focus-server-1.0.0-SNAPSHOT.jar`。

运行依赖：**Nacos**、**MySQL**、**Redis**、**Spring AI OpenAI**、中央 **gRPC Provider** 等。完整键名与默认值以 `hojoai-webmvc-omniasst-focus-server/src/main/resources/config/application-dev.properties` 为准；也可用 Nacos 覆盖。

## 环境变量（速查）

变量前缀均为 **`HOJOAI_FOCUS_`**（与 discovery/config 解耦）。未标可选的通常需显式设置。

| 分组 | 变量 |
| --- | --- |
| Nacos | `HOJOAI_FOCUS_NACOS_SERVER_ADDR`；**可选**：`HOJOAI_FOCUS_NACOS_USERNAME` / `PASSWORD`、`HOJOAI_FOCUS_NACOS_CONFIG_*`（回退到 discovery）、`HOJOAI_FOCUS_SPRING_CONFIG_IMPORT` |
| 数据源 | `HOJOAI_FOCUS_DATASOURCE_URL`、`USERNAME`、`PASSWORD` |
| Redis | `HOJOAI_FOCUS_REDIS_HOST`；**可选**：`PORT`、`USERNAME`、`PASSWORD`、`DATABASE`、`SSL_ENABLED` |
| AI / gRPC | `HOJOAI_FOCUS_OPENAI_BASE_URL`、`HOJOAI_FOCUS_OPENAI_API_KEY`；**可选**：`HOJOAI_FOCUS_GRPC_CENTRAL_PROVIDER_NAME` |
| Snowflake | **可选**：`HOJOAI_FOCUS_SNOWFLAKE_DATACENTER_ID`、`HOJOAI_FOCUS_SNOWFLAKE_WORKER_ID` |
| 常用 | **可选**：`SPRING_PROFILES_ACTIVE`、`SERVER_PORT` |

敏感信息用 **Secret** / Nacos，勿写进镜像。`k8s-Deployment.yaml` 中 `${APP_NAME}` 等为 **`envsubst` 渲染 YAML** 用。

---

## Maven 与本地运行

- **要求**：JDK 21、Maven 3.6+
- **根目录**：`mvn clean package -DskipTests`（默认 **dev**）
- 运行：`java -jar hojoai-webmvc-omniasst-focus-server/target/hojoai-webmvc-omniasst-focus-server-1.0.0-SNAPSHOT.jar`

本地示例：`http://localhost:12008/omniasst-focus/`；更多接口见 `document/API.md`。

---

## Docker

根目录先 `mvn clean package -DskipTests`，再：

```bash
docker build -t hojoai-webmvc-omniasst-focus:1.0.0-SNAPSHOT .
docker run --rm -p 12008:12008 -e SPRING_PROFILES_ACTIVE=dev \
  hojoai-webmvc-omniasst-focus:1.0.0-SNAPSHOT
```

**可选**：`docker build --build-arg BASE_IMAGE=eclipse-temurin:21-jre ...` 覆盖基础镜像（见 `Dockerfile`）。

---

## Kubernetes

`k8s-Deployment.yaml` 为 **Deployment 模板**，占位符：`APP_NAME`、`NAMESPACE`、`REPLICAS`、`IMAGE_URL`、`CONTAINER_PORT`（默认 **12008**）、`CPU_LIMIT` / `MEMORY_LIMIT` / `CPU_REQUEST` / `MEMORY_REQUEST`。

```bash
export APP_NAME=hojoai-webmvc-omniasst-focus NAMESPACE=default REPLICAS=2 \
  IMAGE_URL=your-registry.example.com/.../hojoai-webmvc-omniasst-focus:1.0.0-SNAPSHOT \
  CONTAINER_PORT=12008 CPU_LIMIT=2 MEMORY_LIMIT=2Gi CPU_REQUEST=500m MEMORY_REQUEST=512Mi
envsubst < k8s-Deployment.yaml | kubectl apply -f -
```

清单**不含 Service/Ingress**，需在 Deployment / Secret 中注入 `HOJOAI_FOCUS_*`。

---

**建议流水线**：Maven 打包 → Docker build/push → 渲染清单 `kubectl apply`（或 GitOps）。
