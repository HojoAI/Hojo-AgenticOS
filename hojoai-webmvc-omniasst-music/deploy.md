# 部署说明：hojoai-webmvc-omniasst-music

可运行模块为 **`hojoai-webmvc-omniasst-music-server`**，默认端口 **`12009`**，上下文 **`/omniasst-music`**，产物：`hojoai-webmvc-omniasst-music-server/target/hojoai-webmvc-omniasst-music-server-1.0.0-SNAPSHOT.jar`。

运行依赖：**Nacos**、**Redis**（Lettuce + Redisson，库号固定为 **5**）、网易云开放平台、**Dify**。完整键名与默认值以 `hojoai-webmvc-omniasst-music-server/src/main/resources/config/application-dev.properties` 为准；也可用 Nacos 下发同名配置覆盖。

## 环境变量（速查）

未标「可选」的通常**无默认值**，部署前需设置或通过 Nacos 注入。

| 分组 | 变量 |
| --- | --- |
| Nacos | `NACOS_SERVER_ADDR`、`NACOS_USERNAME`、`NACOS_PASSWORD`；配置中心可另设 `NACOS_CONFIG_SERVER_ADDR` / `NACOS_CONFIG_USERNAME` / `NACOS_CONFIG_PASSWORD`（不设则回退到 discovery 对应项） |
| Redis | `MUSIC_REDIS_HOST`、`MUSIC_REDIS_PASSWORD`；**可选**：`MUSIC_REDIS_PORT`（默认 6379）、`MUSIC_REDIS_USERNAME` |
| 网易云 | **可选**：`NETEASE_API_BASE_URL`、`NETEASE_API_TIMEOUT`；**必填**：`NETEASE_APP_ID`、`NETEASE_APP_SECRET`、`NETEASE_PRIVATE_KEY` |
| Dify | `DIFY_API_URL`、`DIFY_API_TOKEN` |
| OAuth state | `NETEASE_OAUTH_STATE_ENCRYPT_KEY`（AES-128，**16 字节 UTF-8**） |
| 常用 | **可选**：`SPRING_PROFILES_ACTIVE`（如 `dev`）、`SERVER_PORT` |

敏感信息用 **Secret** / Nacos 加密配置，勿写进镜像。`k8s-Deployment.yaml` 里的 `${APP_NAME}` 等是给 **`envsubst` 渲染 YAML** 的，与上表 Spring 环境变量不是同一套。

---

## Maven 与本地运行

- **要求**：JDK 21、Maven 3.6+
- **根目录**打包（默认 **dev** Profile，会写 `spring.profiles.active=dev`）：

```bash
mvn clean package -DskipTests
```

- 运行：`java -jar hojoai-webmvc-omniasst-music-server/target/hojoai-webmvc-omniasst-music-server-1.0.0-SNAPSHOT.jar`  
  端口可用 `SERVER_PORT=12009` 覆盖。

本地示例：`http://localhost:12009/omniasst-music/`（Actuator 健康检查路径以实际配置为准）。

---

## Docker

根目录先 `mvn clean package -DskipTests`，再：

```bash
docker build -t hojoai-webmvc-omniasst-music:1.0.0-SNAPSHOT .
docker run --rm -p 12009:12009 -e SPRING_PROFILES_ACTIVE=dev \
  hojoai-webmvc-omniasst-music:1.0.0-SNAPSHOT
```

按环境补上表中的环境变量（或挂载 / Nacos）。

---

## Kubernetes

`k8s-Deployment.yaml` 为 **Deployment 模板**，占位符：`APP_NAME`、`NAMESPACE`、`REPLICAS`、`IMAGE_URL`、`CONTAINER_PORT`（建议与 `server.port` 一致，如 **12009**）、`CPU_LIMIT` / `MEMORY_LIMIT` / `CPU_REQUEST` / `MEMORY_REQUEST`。

```bash
export APP_NAME=hojoai-webmvc-omniasst-music NAMESPACE=default REPLICAS=2 \
  IMAGE_URL=your-registry.example.com/.../hojoai-webmvc-omniasst-music:1.0.0-SNAPSHOT \
  CONTAINER_PORT=12009 CPU_LIMIT=1 MEMORY_LIMIT=1Gi CPU_REQUEST=200m MEMORY_REQUEST=512Mi
envsubst < k8s-Deployment.yaml | kubectl apply -f -
```

清单里**没有 Service/Ingress**，需自行补全；`imagePullPolicy: Always` 可按版本策略改为 `IfNotPresent`。

---

**建议流水线**：Maven 打包 → Docker build/push → 渲染后的清单 `kubectl apply`（或 GitOps）。
