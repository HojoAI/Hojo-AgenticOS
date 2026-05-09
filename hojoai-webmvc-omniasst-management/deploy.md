# 部署说明：hojoai-webmvc-omniasst-management

可运行模块为 **`hojoai-webmvc-omniasst-management-server`**，默认端口 **`12005`**，上下文 **`/omniasst-management/api`**，产物：`hojoai-webmvc-omniasst-management-server/target/hojoai-webmvc-omniasst-management-server-1.0.0-SNAPSHOT.jar`。

运行依赖：**Nacos**、**MySQL**、**PostgreSQL**、**Redis**、阿里云 **OSS** 等。完整键名与默认值以 `hojoai-webmvc-omniasst-management-server/src/main/resources/config/application-dev.properties` 为准；也可用 Nacos 覆盖。

## 环境变量（速查）

| 分组 | 变量 |
| --- | --- |
| Nacos | `NACOS_SERVER_ADDR`、`NACOS_USERNAME`、`NACOS_PASSWORD`；**可选**：`NACOS_CONFIG_*`（不设则回退） |
| MySQL | `OMNIASST_DATASOURCE_URL`、`OMNIASST_DATASOURCE_USERNAME`、`OMNIASST_DATASOURCE_PASSWORD` |
| PostgreSQL | `OMNIASST_POSTGRESQL_DATASOURCE_URL`、`USERNAME`、`PASSWORD` |
| Redis | `OMNIASST_REDIS_HOST`、`OMNIASST_REDIS_PASSWORD`；**可选**：`OMNIASST_REDIS_PORT`（默认 6379） |
| OSS | **可选**：`OMNIASST_MANAGEMENT_ALIYUN_OSS_ENDPOINT`；`ALIYUN_OSS_ACCESS_KEY_ID`、`ALIYUN_OSS_ACCESS_KEY_SECRET`、`ALIYUN_OSS_BUCKET_NAME` |
| LLM | **可选**：`LLM_TITLE_GENERATION_URL` |
| 常用 | **可选**：`SPRING_PROFILES_ACTIVE`、`SERVER_PORT` |

敏感信息用 **Secret** / Nacos，勿写进镜像。`k8s-Deployment.yaml` 中 `${APP_NAME}` 等为 **`envsubst` 渲染 YAML** 用。

---

## Maven 与本地运行

- **要求**：JDK 21、Maven 3.6+
- **根目录**：`mvn clean package -DskipTests`（默认 **dev**）
- 运行：`java -jar hojoai-webmvc-omniasst-management-server/target/hojoai-webmvc-omniasst-management-server-1.0.0-SNAPSHOT.jar`

本地示例：`http://localhost:12005/omniasst-management/api/`；更多接口见 `document/`。

---

## Docker

根目录先 `mvn clean package -DskipTests`，再：

```bash
docker build -t hojoai-webmvc-omniasst-management:1.0.0-SNAPSHOT .
docker run --rm -p 12005:12005 -e SPRING_PROFILES_ACTIVE=dev \
  hojoai-webmvc-omniasst-management:1.0.0-SNAPSHOT
```

按环境补上表中的环境变量（或 Nacos）。

---

## Kubernetes

`k8s-Deployment.yaml` 为 **Deployment 模板**，占位符：`APP_NAME`、`NAMESPACE`、`REPLICAS`、`IMAGE_URL`、`CONTAINER_PORT`（默认 **12005**）、`CPU_LIMIT` / `MEMORY_LIMIT` / `CPU_REQUEST` / `MEMORY_REQUEST`。

```bash
export APP_NAME=hojoai-webmvc-omniasst-management NAMESPACE=default REPLICAS=2 \
  IMAGE_URL=your-registry.example.com/.../hojoai-webmvc-omniasst-management:1.0.0-SNAPSHOT \
  CONTAINER_PORT=12005 CPU_LIMIT=2 MEMORY_LIMIT=2Gi CPU_REQUEST=500m MEMORY_REQUEST=512Mi
envsubst < k8s-Deployment.yaml | kubectl apply -f -
```

清单**不含 Service/Ingress**。

---

**建议流水线**：Maven 打包 → Docker build/push → 渲染清单 `kubectl apply`（或 GitOps）。
