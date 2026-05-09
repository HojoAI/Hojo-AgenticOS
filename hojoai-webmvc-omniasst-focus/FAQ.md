# 常见问题（FAQ）

## 启动失败：无法连接 Nacos

- **dev** 默认使用 `optional:nacos:...`，无 Nacos 时通常仍可启动；若改为了非 `optional` 的 `spring.config.import`，需先启动 Nacos 或改回 optional。
- 检查 `HOJOAI_FOCUS_NACOS_SERVER_ADDR` 是否可达。

## 启动失败：数据源或 Redis 连接错误

- 所有敏感连接信息已改为环境变量占位，请对照 `config/application-dev.properties` 设置 `HOJOAI_FOCUS_DATASOURCE_*`、`HOJOAI_FOCUS_REDIS_*`。
- **dev** 下部分项仍无默认值时，必须通过环境变量或 Nacos 下发。

## AI 调用报错

- 配置 `HOJOAI_FOCUS_OPENAI_BASE_URL` 与 `HOJOAI_FOCUS_OPENAI_API_KEY`（或 Nacos 中对应 `spring.ai.openai.*`）。
- 本服务使用 Spring AI OpenAI 兼容客户端，需目标服务兼容 OpenAI API 形态。

## 接口 404

- 请求需带上下文路径前缀 **`/omniasst-focus`**，例如：`http://localhost:12008/omniasst-focus/api/v1/...`。

## 与中央服务的关系

- gRPC 服务名由 `grpc.central.provider.service.name`（环境变量 `HOJOAI_FOCUS_GRPC_CENTRAL_PROVIDER_NAME`）指定，需与集群内 **hojoai-grpc-central-provider** 注册名一致。

## Docker 构建提示找不到 JAR

- 先在根目录执行 `mvn clean package -DskipTests`，确保 `hojoai-webmvc-omniasst-focus-server/target/*.jar` 已生成。
