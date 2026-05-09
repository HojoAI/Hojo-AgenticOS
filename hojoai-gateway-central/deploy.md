# 部署说明：hojoai-gateway-central

可运行模块为 **`hojoai-gateway-central-server`**（Spring Cloud Gateway），默认端口 **`11001`**，产物：`hojoai-gateway-central-server/target/hojoai-gateway-central-server-1.0.0-SNAPSHOT.jar`。

运行依赖：**Nacos**（服务发现与配置，`spring.config.import` 等以 properties 为准）。完整键名与默认值以 `hojoai-gateway-central-server/src/main/resources/config/application-dev.properties` 为准。

## 环境变量（速查）

| 分组 | 变量 |
| --- | --- |
| Nacos | `NACOS_SERVER_ADDR`、`NACOS_USERNAME`、`NACOS_PASSWORD`；**可选**：`NACOS_CONFIG_*`（不设则回退） |
| 常用 | **可选**：`SPRING_PROFILES_ACTIVE`、`SERVER_PORT`、`JAVA_OPTS` |

敏感信息用 **Secret** / Nacos，勿写进镜像。`k8s-Deployment.yaml` 中 `${APP_NAME}` 等为 **`envsubst` 渲染 YAML** 用。

---

## Maven 与本地运行

- **要求**：JDK 与 Maven 版本满足父 POM `hojoai-parent`
- **根目录**：`mvn clean package -DskipTests`（默认 **dev**）
- 运行：`java -jar hojoai-gateway-central-server/target/hojoai-gateway-central-server-1.0.0-SNAPSHOT.jar`

---

## Docker

根目录先 `mvn clean package -DskipTests`，再：

```bash
docker build -t hojoai-gateway-central:1.0.0-SNAPSHOT .
docker run --rm -p 11001:11001 -e JAVA_OPTS="-Xms512m -Xmx512m" \
  hojoai-gateway-central:1.0.0-SNAPSHOT
```

容器内需能访问 Nacos；私服构建需在 CI 中配置 Maven 凭证。

---

## Kubernetes

`k8s-Deployment.yaml` 为 **Deployment 模板**，占位符：`APP_NAME`、`NAMESPACE`、`REPLICAS`、`IMAGE_URL`、`CONTAINER_PORT`（默认 **11001**）、`CPU_LIMIT` / `MEMORY_LIMIT` / `CPU_REQUEST` / `MEMORY_REQUEST`。

```bash
export APP_NAME=hojoai-gateway-central NAMESPACE=default REPLICAS=2 \
  IMAGE_URL=your-registry.example.com/.../hojoai-gateway-central:1.0.0-SNAPSHOT \
  CONTAINER_PORT=11001 CPU_LIMIT=1 MEMORY_LIMIT=1Gi CPU_REQUEST=200m MEMORY_REQUEST=512Mi
envsubst < k8s-Deployment.yaml | kubectl apply -f -
```

清单**不含 Service/Ingress**；按需补 **Service**（`targetPort` 与 **11001** 一致）及 Actuator 探针。

---

**建议流水线**：Maven 打包 → Docker build/push → 渲染清单 `kubectl apply`（或 GitOps）。
