# hojoai-template

新建 HoJoAI **Spring Boot 微服务**时的参考模板：预置 Web、Actuator、AOP、负载均衡与常用依赖，便于从统一父工程快速拷贝并改造。

## 技术栈

- 继承 [hojoai-parent](../hojoai-parent)
- Spring Boot Web、Actuator、AOP
- Spring Cloud LoadBalancer 等（详见 `pom.xml`）

## 运行约定

| 项目 | 说明 |
|------|------|
| 默认 HTTP 端口 | `12000` |
| Spring 应用名 | `hojoai-template` |

## 快速开始

```bash
cd hojoai-template
mvn clean install
mvn spring-boot:run
```

复制为新服务时，请修改 `artifactId`、`spring.application.name`、包名与业务代码，并接入实际注册中心与配置。

## 文档

- **[API.md](./API.md)** — 示例接口与请求体字段说明。
