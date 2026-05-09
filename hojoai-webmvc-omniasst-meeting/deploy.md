# 部署说明：hojoai-webmvc-omniasst-meeting

可运行模块为 **`hojoai-webmvc-omniasst-meeting-server`**，默认端口 **`12007`**，上下文 **`/omniasst-meeting`**，产物：`hojoai-webmvc-omniasst-meeting-server/target/hojoai-webmvc-omniasst-meeting-server-1.0.0-SNAPSHOT.jar`。

运行依赖：**Nacos**、**Redis**、**MySQL** 与 **PostgreSQL**、会议相关模型服务。完整键名与默认值以 `hojoai-webmvc-omniasst-meeting-server/src/main/resources/config/application-dev.properties` 为准；也可用 Nacos 覆盖。

## 环境变量（速查）

| 分组 | 变量 |
| --- | --- |
| Nacos | `NACOS_SERVER_ADDR`、`NACOS_USERNAME`、`NACOS_PASSWORD`；**可选**：`NACOS_CONFIG_*`（不设则回退到 discovery 对应项） |
| Redis | `OMNIASST_REDIS_HOST`、`OMNIASST_REDIS_PASSWORD`；**可选**：`OMNIASST_REDIS_PORT`（默认 6379） |
| 数据源 | `OMNIASST_DATASOURCE_URL` / `USERNAME` / `PASSWORD`；`OMNIASST_POSTGRESQL_DATASOURCE_URL` / `USERNAME` / `PASSWORD` |
| 模型 / LLM | **可选**：`MEETING_OPENAI_API_KEY`（默认 `disabled`）；**必填**：`MEETING_HOJO_SUMMARY_URL`、`MEETING_HOJO_SUMMARY_AUTH_TOKEN`、`MEETING_TITLE_URL`、`MEETING_TITLE_API_KEY` |
| 常用 | **可选**：`SPRING_PROFILES_ACTIVE`、`SERVER_PORT` |

敏感信息用 **Secret** / Nacos，勿写进镜像。`k8s-Deployment.yaml` 中 `${APP_NAME}` 等为 **`envsubst` 渲染 YAML** 用，与 Spring 环境变量不同。

---

## Maven 与本地运行

- **要求**：JDK 21、Maven 3.6+
- **根目录**：`mvn clean package -DskipTests`（默认 **dev** Profile）
- 运行：`java -jar hojoai-webmvc-omniasst-meeting-server/target/hojoai-webmvc-omniasst-meeting-server-1.0.0-SNAPSHOT.jar`  
  端口可用 `SERVER_PORT=12007` 覆盖。

本地示例：`http://localhost:12007/omniasst-meeting/`

---

## Docker

镜像构建依赖仓库内 **`docker/ffmpeg/`** 下对应架构的静态 ffmpeg 包（见 `Dockerfile` 注释）；需先 **`mvn clean package -DskipTests`**，再在模块根目录：

```bash
docker build -t hojoai-webmvc-omniasst-meeting:1.0.0-SNAPSHOT .
docker run --rm -p 12007:12007 -e SPRING_PROFILES_ACTIVE=dev \
  hojoai-webmvc-omniasst-meeting:1.0.0-SNAPSHOT
```

按环境补上表中的环境变量（或 Nacos）。

---

## Kubernetes

`k8s-Deployment.yaml` 为 **Deployment 模板**，占位符：`APP_NAME`、`NAMESPACE`、`REPLICAS`、`IMAGE_URL`、`CONTAINER_PORT`（默认 **12007**）、`CPU_LIMIT` / `MEMORY_LIMIT` / `CPU_REQUEST` / `MEMORY_REQUEST`。

```bash
export APP_NAME=hojoai-webmvc-omniasst-meeting NAMESPACE=default REPLICAS=2 \
  IMAGE_URL=your-registry.example.com/.../hojoai-webmvc-omniasst-meeting:1.0.0-SNAPSHOT \
  CONTAINER_PORT=12007 CPU_LIMIT=2 MEMORY_LIMIT=2Gi CPU_REQUEST=500m MEMORY_REQUEST=512Mi
envsubst < k8s-Deployment.yaml | kubectl apply -f -
```

清单**不含 Service/Ingress**，需自行补全；`imagePullPolicy` 可按策略调整。

---

**建议流水线**：Maven 打包 → Docker build/push → 渲染清单 `kubectl apply`（或 GitOps）。
