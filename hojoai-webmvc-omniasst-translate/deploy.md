# 部署说明：hojoai-webmvc-omniasst-translate

可运行模块为 **`hojoai-webmvc-omniasst-translate-server`**，默认端口 **`12006`**，上下文 **`/omniasst-translate`**，产物：`hojoai-webmvc-omniasst-translate-server/target/hojoai-webmvc-omniasst-translate-server-1.0.0-SNAPSHOT.jar`。

运行依赖：**Nacos**、**Redis**、**MySQL**、Autoflow NLU、下游 voice/management 等（以配置为准）。完整键名与默认值以 `hojoai-webmvc-omniasst-translate-server/src/main/resources/config/application-dev.properties` 为准；也可用 Nacos 覆盖。

## 环境变量（速查）

| 分组 | 变量 |
| --- | --- |
| Nacos | `NACOS_SERVER_ADDR` 等（discovery 带本地默认 **127.0.0.1:8848**）；`NACOS_USERNAME`、`NACOS_PASSWORD`；**可选**：`NACOS_CONFIG_*`（嵌套回退，见 properties 文件） |
| Redis | `TRANSLATE_REDIS_HOST`、`TRANSLATE_REDIS_PASSWORD`；**可选**：`TRANSLATE_REDIS_PORT`（默认 6379）、`TRANSLATE_REDIS_DATABASE`（默认 6）、`TRANSLATE_REDIS_USERNAME` |
| 数据源 | `TRANSLATE_DATASOURCE_URL`、`USERNAME`、`PASSWORD` |
| NLU / LLM | `AUTOFLOW_NLU_URL`、`AUTOFLOW_NLU_AUTHORIZATION`；**可选**：`LLM_TITLE_GENERATION_URL` |
| 常用 | **可选**：`SPRING_PROFILES_ACTIVE`、`SERVER_PORT` |

敏感信息用 **Secret** / Nacos，勿写进镜像。`k8s-Deployment.yaml` 中 `${APP_NAME}` 等为 **`envsubst` 渲染 YAML** 用。

---

## Maven 与本地运行

- **要求**：JDK 21、Maven 3.6+
- **根目录**：`mvn clean package -DskipTests`（默认 **dev**）
- 运行：`java -jar hojoai-webmvc-omniasst-translate-server/target/hojoai-webmvc-omniasst-translate-server-1.0.0-SNAPSHOT.jar`

业务 HTTP：`http://localhost:12006/omniasst-translate/`；翻译 **WebSocket** 注册为 **`/translate`**，完整地址示例：`ws://localhost:12006/omniasst-translate/translate`。

---

## Docker

根目录先 `mvn clean package -DskipTests`，再：

```bash
docker build -t hojoai-webmvc-omniasst-translate:1.0.0-SNAPSHOT .
docker run --rm -p 12006:12006 -e SPRING_PROFILES_ACTIVE=dev \
  hojoai-webmvc-omniasst-translate:1.0.0-SNAPSHOT
```

按环境补上表中的环境变量（或 Nacos）。

---

## Kubernetes

`k8s-Deployment.yaml` 为 **Deployment 模板**，占位符：`APP_NAME`、`NAMESPACE`、`REPLICAS`、`IMAGE_URL`、`CONTAINER_PORT`（默认 **12006**）、`CPU_LIMIT` / `MEMORY_LIMIT` / `CPU_REQUEST` / `MEMORY_REQUEST`。

```bash
export APP_NAME=hojoai-webmvc-omniasst-translate NAMESPACE=default REPLICAS=2 \
  IMAGE_URL=your-registry.example.com/.../hojoai-webmvc-omniasst-translate:1.0.0-SNAPSHOT \
  CONTAINER_PORT=12006 CPU_LIMIT=2 MEMORY_LIMIT=2Gi CPU_REQUEST=500m MEMORY_REQUEST=512Mi
envsubst < k8s-Deployment.yaml | kubectl apply -f -
```

清单**不含 Service/Ingress**；暴露 WebSocket 时需按网关调整超时与路由。

---

**建议流水线**：Maven 打包 → Docker build/push → 渲染清单 `kubectl apply`（或 GitOps）。
