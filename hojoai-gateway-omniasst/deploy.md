# 部署说明：hojoai-gateway-omniasst

可运行模块为 **`hojoai-gateway-omniasst-server`**（Spring Cloud Gateway），默认端口 **`12001`**，未配置 servlet context-path 时路由在根路径，产物：`hojoai-gateway-omniasst-server/target/hojoai-gateway-omniasst-server-1.0.0-SNAPSHOT.jar`。

运行依赖：**Nacos**（注册与配置）、中央账号 **`CENTRAL_ACCOUNT_BASE_URL`** 等。完整键名与默认值以 `hojoai-gateway-omniasst-server/src/main/resources/config/application-dev.properties` 为准；也可用 Nacos 覆盖。构建需能解析父 POM `com.hojoai.parent:hojoai-parent`（通常配置私服）。

## 环境变量（速查）

| 分组 | 变量 |
| --- | --- |
| Nacos | `NACOS_SERVER_ADDR`、`NACOS_USERNAME`、`NACOS_PASSWORD`；**可选**：`NACOS_CONFIG_*`（不设则回退） |
| 路由 / 下游 | `CENTRAL_ACCOUNT_BASE_URL` |
| 常用 | **可选**：`SPRING_PROFILES_ACTIVE`、`SERVER_PORT` |

敏感信息用 **Secret** / Nacos，勿写进镜像。`k8s-Deployment.yaml` 中 `${APP_NAME}` 等为 **`envsubst` 渲染 YAML** 用。

---

## Maven 与本地运行

- **要求**：JDK 21、Maven 3.6+
- **根目录**：`mvn clean package -DskipTests`（默认 **dev**，可选 `-Pdev`）
- 运行：`java -jar hojoai-gateway-omniasst-server/target/hojoai-gateway-omniasst-server-1.0.0-SNAPSHOT.jar`

访问：`http://localhost:12001/`（具体路由以网关配置为准）。

---

## Docker

根目录先 `mvn clean package -DskipTests`，再：

```bash
docker build -t hojoai-gateway-omniasst:1.0.0-SNAPSHOT .
docker run --rm -p 12001:12001 hojoai-gateway-omniasst:1.0.0-SNAPSHOT
```

可按需增加 `-e SPRING_PROFILES_ACTIVE=dev` 等。

---

## Kubernetes

`k8s-Deployment.yaml` 为 **Deployment 模板**，占位符：`APP_NAME`、`NAMESPACE`、`REPLICAS`、`IMAGE_URL`、`CONTAINER_PORT`（默认 **12001**）、`CPU_LIMIT` / `MEMORY_LIMIT` / `CPU_REQUEST` / `MEMORY_REQUEST`。

```bash
export APP_NAME=hojoai-gateway-omniasst NAMESPACE=default REPLICAS=2 \
  IMAGE_URL=your-registry.example.com/.../hojoai-gateway-omniasst:1.0.0-SNAPSHOT \
  CONTAINER_PORT=12001 CPU_LIMIT=1 MEMORY_LIMIT=1Gi CPU_REQUEST=200m MEMORY_REQUEST=512Mi
envsubst < k8s-Deployment.yaml | kubectl apply -f -
```

清单**不含 Service/Ingress**；集群内需自行补 **Service**（指向 **12001**）等。

---

**建议流水线**：Maven 打包 → Docker build/push → 渲染清单 `kubectl apply`（或 GitOps）。
