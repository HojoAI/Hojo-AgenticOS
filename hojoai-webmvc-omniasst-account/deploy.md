# 部署说明：hojoai-webmvc-omniasst-account

可运行模块为 **`hojoai-webmvc-omniasst-account-server`**，默认端口 **`12004`**，未配置 servlet context-path 时业务路径如 **`/omniasst-account/...`**，产物：`hojoai-webmvc-omniasst-account-server/target/hojoai-webmvc-omniasst-account-server-1.0.0-SNAPSHOT.jar`。

运行依赖：**Nacos**、**Redis**、**central-account** 服务发现、海外认证（Google / Apple / SES / Magic link）等。完整键名与默认值以 `hojoai-webmvc-omniasst-account-server/src/main/resources/config/application-dev.properties` 为准；也可用 Nacos 覆盖。

## 环境变量（速查）

| 分组 | 变量 |
| --- | --- |
| Nacos | `NACOS_SERVER_ADDR`、`NACOS_USERNAME`、`NACOS_PASSWORD`；**可选**：`NACOS_CONFIG_*`（不设则回退） |
| Redis | `OMNIASST_REDIS_HOST`、`OMNIASST_REDIS_PASSWORD`（端口等在 properties 中另有固定项，以文件为准） |
| Google | `OVERSEA_AUTH_GOOGLE_CLIENT_ID_IOS`、`OVERSEA_AUTH_GOOGLE_CLIENT_ID_ANDROID`、`OVERSEA_AUTH_GOOGLE_CLIENT_ID_WEB`；**可选**：`OVERSEA_AUTH_GOOGLE_PROXY_HOST`、`OVERSEA_AUTH_GOOGLE_PROXY_PORT` |
| Apple / SES / Magic link | `OVERSEA_AUTH_APPLE_BUNDLE_ID`；`OVERSEA_AUTH_SES_ACCESS_KEY`、`OVERSEA_AUTH_SES_SECRET_KEY`、`OVERSEA_AUTH_SES_FROM_ADDRESS`；**可选**：`OVERSEA_AUTH_SES_REGION`；`OVERSEA_AUTH_MAGIC_LINK_BASE_URL` |
| 常用 | **可选**：`SPRING_PROFILES_ACTIVE`、`SERVER_PORT` |

敏感信息用 **Secret** / Nacos，勿写进镜像。`k8s-Deployment.yaml` 中 `${APP_NAME}` 等为 **`envsubst` 渲染 YAML** 用。

---

## Maven 与本地运行

- **要求**：JDK 21、Maven 3.6+
- **根目录**：`mvn clean package -DskipTests`（默认 **dev**）
- 运行：`java -jar hojoai-webmvc-omniasst-account-server/target/hojoai-webmvc-omniasst-account-server-1.0.0-SNAPSHOT.jar`

本地示例：`http://localhost:12004/`；更多接口见 `document/`。

---

## Docker

根目录先 `mvn clean package -DskipTests`，再：

```bash
docker build -t hojoai-webmvc-omniasst-account:1.0.0-SNAPSHOT .
docker run --rm -p 12004:12004 -e SPRING_PROFILES_ACTIVE=dev \
  hojoai-webmvc-omniasst-account:1.0.0-SNAPSHOT
```

按环境补上表中的环境变量（或 Nacos）。

---

## Kubernetes

`k8s-Deployment.yaml` 为 **Deployment 模板**，占位符：`APP_NAME`、`NAMESPACE`、`REPLICAS`、`IMAGE_URL`、`CONTAINER_PORT`（默认 **12004**）、`CPU_LIMIT` / `MEMORY_LIMIT` / `CPU_REQUEST` / `MEMORY_REQUEST`。

```bash
export APP_NAME=hojoai-webmvc-omniasst-account NAMESPACE=default REPLICAS=2 \
  IMAGE_URL=your-registry.example.com/.../hojoai-webmvc-omniasst-account:1.0.0-SNAPSHOT \
  CONTAINER_PORT=12004 CPU_LIMIT=2 MEMORY_LIMIT=2Gi CPU_REQUEST=500m MEMORY_REQUEST=512Mi
envsubst < k8s-Deployment.yaml | kubectl apply -f -
```

清单**不含 Service/Ingress**。

---

**建议流水线**：Maven 打包 → Docker build/push → 渲染清单 `kubectl apply`（或 GitOps）。
