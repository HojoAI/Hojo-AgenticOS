# 部署说明：hojoai-gateway-omniasst-voice

可运行模块为 **`hojoai-gateway-omniasst-voice-server`**，默认端口 **`12002`**，上下文 **`/omniasst-voice`**，产物：`hojoai-gateway-omniasst-voice-server/target/hojoai-gateway-omniasst-voice-server-1.0.0-SNAPSHOT.jar`。

运行依赖：**Nacos**、**Redis**、**PostgreSQL**、语音（TTS/ASR/多家云厂商）、**OSS**、Autoflow NLU 等。完整键名与默认值以 `hojoai-gateway-omniasst-voice-server/src/main/resources/config/application-dev.properties` 为准；也可用 Nacos 覆盖。

## 环境变量（速查）

| 分组 | 变量 |
| --- | --- |
| Nacos | `NACOS_SERVER_ADDR`、`NACOS_USERNAME`、`NACOS_PASSWORD`；**可选**：`NACOS_CONFIG_*`（不设则回退） |
| Redis | `OMNIASST_REDIS_HOST`、`OMNIASST_REDIS_PASSWORD`；**可选**：`OMNIASST_REDIS_PORT`（默认 6379） |
| PostgreSQL | `OMNIASST_POSTGRESQL_DATASOURCE_URL`、`USERNAME`、`PASSWORD` |
| 语音 / NLU | `HOJOAI_SPEECH_TTS_HOST`、`HOJOAI_TTS_API_KEY`、`HOJOAI_SPEECH_ASR_HOST`、`HOJOAI_ASR_API_KEY`、`AUTOFLOW_NLU_URL`、`AUTOFLOW_NLU_AUTHORIZATION` |
| OSS | **可选**：`ALIYUN_OSS_ENDPOINT`；`ALIYUN_OSS_BUCKET_NAME`、`ALIYUN_OSS_ACCESS_KEY_ID`、`ALIYUN_OSS_ACCESS_KEY_SECRET` |
| 豆包 / Azure / 阿里 NLS / Minimax | `DOUBAO_TTS_APP_ID`、`DOUBAO_TTS_ACCESS_TOKEN`、`AZURE_SPEECH_SUBSCRIPTION_KEY`、`ALIYUN_NLS_ACCESS_KEY_ID`、`ALIYUN_NLS_ACCESS_KEY_SECRET`、`ALIYUN_NLS_APPKEY`、`ALIYUN_NLS_TOKEN`、`MINIMAX_AI_API_KEY` |
| 常用 | **可选**：`SPRING_PROFILES_ACTIVE`、`SERVER_PORT`、`TZ`、`JAVA_TOOL_OPTIONS` |

敏感信息用 **Secret** / Nacos，勿写进镜像。`k8s-Deployment.yaml` 中 `${APP_NAME}` 等为 **`envsubst` 渲染 YAML** 用。

---

## Maven 与本地运行

- **要求**：JDK 21、Maven 3.6+
- **根目录**：`mvn clean package -DskipTests`（默认 **dev**）
- 运行：`java -jar hojoai-gateway-omniasst-voice-server/target/hojoai-gateway-omniasst-voice-server-1.0.0-SNAPSHOT.jar`

访问示例：`http://localhost:12002/omniasst-voice/...`

---

## Docker

根目录先 `mvn clean package -DskipTests`，再：

```bash
docker build -t hojoai-gateway-omniasst-voice:1.0.0-SNAPSHOT .
docker run --rm -p 12002:12002 -e TZ=Asia/Shanghai \
  hojoai-gateway-omniasst-voice:1.0.0-SNAPSHOT
```

按环境补上表中的环境变量（或 Nacos）。`Dockerfile` 中 `COPY` 的 JAR 版本需与 `pom.xml` 一致。

---

## Kubernetes

`k8s-Deployment.yaml` 为 **Deployment 模板**，占位符：`APP_NAME`、`NAMESPACE`、`REPLICAS`、`IMAGE_URL`、`CONTAINER_PORT`（默认 **12002**）、`CPU_LIMIT` / `MEMORY_LIMIT` / `CPU_REQUEST` / `MEMORY_REQUEST`。

```bash
export APP_NAME=hojoai-gateway-omniasst-voice NAMESPACE=default REPLICAS=2 \
  IMAGE_URL=your-registry.example.com/.../hojoai-gateway-omniasst-voice:1.0.0-SNAPSHOT \
  CONTAINER_PORT=12002 CPU_LIMIT=2 MEMORY_LIMIT=2Gi CPU_REQUEST=500m MEMORY_REQUEST=1Gi
envsubst < k8s-Deployment.yaml | kubectl apply -f -
```

清单**不含 Service/Ingress**。

---

**建议流水线**：Maven 打包 → Docker build/push → 渲染清单 `kubectl apply`（或 GitOps）。
