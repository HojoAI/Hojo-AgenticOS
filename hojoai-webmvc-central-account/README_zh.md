# hojoai-webmvc-central-account

**中央域账户与鉴权服务**：提供短信验证码、用户注册与查询、Token 签发/校验/注销等基础鉴权能力，以及海外认证方式支持。

## 能力列表

- 短信验证码：验证码发送与校验
- 用户注册/查询：用户创建与信息查询
- Token 管理：Token 签发、校验与注销
- 海外认证：海外第三方认证集成
- 账户删除：账户注销能力

## 技术栈

- Java 21、Spring Boot Web
- 继承 [hojoai-parent](../hojoai-parent)；通常与 Nacos、MySQL 等配合（以各环境配置为准）

## 运行约定

| 项目 | 说明 |
|------|------|
| 可运行模块 | `hojoai-webmvc-central-account-server` |
| 默认 HTTP 端口 | `11002` |
| Servlet context-path | 默认无；控制器路径以 `/central-account` 为前缀 |
| Spring 应用名 | `hojoai-webmvc-central-account` |
| 经网关访问 | [hojoai-gateway-central](../hojoai-gateway-central) 前缀 `/central-account/**` |

## 构建与运行

```bash
cd hojoai-webmvc-central-account
mvn clean install
cd hojoai-webmvc-central-account-server && mvn spring-boot:run
```

## 文档

- [API.md](./API.md) — HTTP 路径与参数说明。
