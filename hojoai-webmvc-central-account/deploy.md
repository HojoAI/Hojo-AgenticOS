# 部署说明：hojoai-webmvc-central-account

可运行模块为 **`hojoai-webmvc-central-account-server`**，默认端口 **`11002`**，产物：`hojoai-webmvc-central-account-server/target/hojoai-webmvc-central-account-server-1.0.0-SNAPSHOT.jar`。

运行依赖：**Nacos**、**MySQL**、**Redis**、阿里云短信等。完整键名与默认值以 `hojoai-webmvc-central-account-server/src/main/resources/config/application-dev.properties` 为准；也可用 Nacos 覆盖。阿里云 SDK 凭证常用环境变量 **`ALIBABA_CLOUD_ACCESS_KEY_ID`** / **`ALIBABA_CLOUD_ACCESS_KEY_SECRET`**（勿提交仓库）。

## 环境变量（速查）

| 分组 | 变量 |
| --- | --- |
| Nacos | `NACOS_SERVER_ADDR`、`NACOS_USERNAME`、`NACOS_PASSWORD`；**可选**：`NACOS_CONFIG_*`（嵌套回退） |
| 数据源 | `DATASOURCE_URL`、`DATASOURCE_USERNAME`、`DATASOURCE_PASSWORD` |
| Redis | `REDIS_HOST`；**可选**：`REDIS_PASSWORD`、`REDIS_PORT`（默认 6379）、`REDIS_DATABASE`（默认 0） |
| 短信 / 验证码 | **可选**：`ALIYUN_SMS_SIGN_NAME`、`ALIYUN_SMS_TEMPLATE_CODE`、`CAPTCHA_MOCK_USER_MOBILE`、`CAPTCHA_MOCK_MOBILE_CAPTCHA` |
| 常用 | **可选**：`SPRING_PROFILES_ACTIVE`、`SERVER_PORT` |

敏感信息用 **Secret** / Nacos，勿写进镜像。`k8s-Deployment.yaml` 中 `${APP_NAME}` 等为 **`envsubst` 渲染 YAML** 用。

---

## Maven 与本地运行

- **要求**：JDK 21、Maven 3.6+
- **根目录**：`mvn clean package -DskipTests`（默认 **dev**）
- 运行：`java -jar hojoai-webmvc-central-account-server/target/hojoai-webmvc-central-account-server-1.0.0-SNAPSHOT.jar`

默认监听：`http://localhost:11002/`（业务路径以 Controller 为准，如 `/central-account/...`）。

---

## Docker

根目录先 `mvn clean package -DskipTests`，再：

```bash
docker build -t hojoai-webmvc-central-account:1.0.0-SNAPSHOT .
docker run --rm -p 11002:11002 -e SPRING_PROFILES_ACTIVE=dev \
  hojoai-webmvc-central-account:1.0.0-SNAPSHOT
```

按环境注入数据源、Redis、短信等变量（或 Nacos）。

---

## Kubernetes

`k8s-Deployment.yaml` 为 **Deployment 模板**，占位符：`APP_NAME`、`NAMESPACE`、`REPLICAS`、`IMAGE_URL`、`CONTAINER_PORT`（默认 **11002**）、`CPU_LIMIT` / `MEMORY_LIMIT` / `CPU_REQUEST` / `MEMORY_REQUEST`。

```bash
export APP_NAME=hojoai-webmvc-central-account NAMESPACE=default REPLICAS=2 \
  IMAGE_URL=your-registry.example.com/.../hojoai-webmvc-central-account:1.0.0-SNAPSHOT \
  CONTAINER_PORT=11002 CPU_LIMIT=2 MEMORY_LIMIT=2Gi CPU_REQUEST=500m MEMORY_REQUEST=512Mi
envsubst < k8s-Deployment.yaml | kubectl apply -f -
```

清单**不含 Service/Ingress**。

---

**建议流水线**：Maven 打包 → Docker build/push → 渲染清单 `kubectl apply`（或 GitOps）。
