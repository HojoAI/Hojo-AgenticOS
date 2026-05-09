# 部署说明：hojoai-webmvc-omniasst-weather

可运行模块为 **`hojoai-webmvc-omniasst-weather-server`**，默认端口 **`12010`**，上下文 **`/omniasst-weather`**，产物：`hojoai-webmvc-omniasst-weather-server/target/hojoai-webmvc-omniasst-weather-server-1.0.0-SNAPSHOT.jar`。

运行依赖：**Nacos**、**Dify**、彩云 / LLM / 高德等外部 API。完整键名与默认值以 `hojoai-webmvc-omniasst-weather-server/src/main/resources/config/application-dev.properties` 为准；也可用 Nacos 覆盖。

## 环境变量（速查）

| 分组 | 变量 |
| --- | --- |
| Nacos | `NACOS_SERVER_ADDR`、`NACOS_USERNAME`、`NACOS_PASSWORD`；**可选**：`NACOS_CONFIG_*`（不设则回退到 discovery 对应项） |
| Dify | `DIFY_API_URL`、`DIFY_API_TOKEN` |
| 天气 / 地图 / LLM | `CAIYUN_WEATHER_TOKEN`、`LLM_API_KEY`、`AMAP_GEO_KEY` |
| 常用 | **可选**：`SPRING_PROFILES_ACTIVE`、`SERVER_PORT` |

敏感信息用 **Secret** / Nacos，勿写进镜像。`k8s-Deployment.yaml` 中 `${APP_NAME}` 等为 **`envsubst` 渲染 YAML** 用。

---

## Maven 与本地运行

- **要求**：JDK 21、Maven 3.6+（与根目录 `Dockerfile` 中 Dragonwell 21 一致）
- **根目录**：`mvn clean package -DskipTests`（默认 **dev**）
- 运行：`java -jar hojoai-webmvc-omniasst-weather-server/target/hojoai-webmvc-omniasst-weather-server-1.0.0-SNAPSHOT.jar`

本地示例：`http://localhost:12010/omniasst-weather/`（Actuator 路径需带相同 context-path）。

---

## Docker

根目录先 `mvn clean package -DskipTests`，再：

```bash
docker build -t hojoai-webmvc-omniasst-weather:1.0.0-SNAPSHOT .
docker run --rm -p 12010:12010 -e SPRING_PROFILES_ACTIVE=dev \
  hojoai-webmvc-omniasst-weather:1.0.0-SNAPSHOT
```

按环境补上表中的环境变量（或 Nacos）。父 POM 若在私服，构建需配置 `~/.m2/settings.xml` 或 CI 凭证。

---

## Kubernetes

`k8s-Deployment.yaml` 为 **Deployment 模板**，占位符：`APP_NAME`、`NAMESPACE`、`REPLICAS`、`IMAGE_URL`、`CONTAINER_PORT`（默认 **12010**）、`CPU_LIMIT` / `MEMORY_LIMIT` / `CPU_REQUEST` / `MEMORY_REQUEST`。

```bash
export APP_NAME=hojoai-webmvc-omniasst-weather NAMESPACE=default REPLICAS=2 \
  IMAGE_URL=your-registry.example.com/.../hojoai-webmvc-omniasst-weather:1.0.0-SNAPSHOT \
  CONTAINER_PORT=12010 CPU_LIMIT=1 MEMORY_LIMIT=1Gi CPU_REQUEST=200m MEMORY_REQUEST=512Mi
envsubst < k8s-Deployment.yaml | kubectl apply -f -
```

清单**不含 Service/Ingress**；探针 URL 需包含 **`/omniasst-weather`** 前缀。

---

**建议流水线**：Maven 打包 → Docker build/push → 渲染清单 `kubectl apply`（或 GitOps）。
