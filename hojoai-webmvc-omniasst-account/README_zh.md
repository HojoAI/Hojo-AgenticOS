# hojoai-webmvc-omniasst-account

**Omniasst 账户服务**：提供手机号验证码登录、用户注册与 Token 管理，支持 Google/Apple/邮箱/Magic Link 等海外与多端认证方式。

## 能力列表

- 手机号登录：短信验证码（OTP）登录/注册，支持验证码发送与校验
- 海外认证：Google OAuth、Apple OAuth、邮箱验证码、Magic Link
- 用户管理：用户注册、信息查询与更新
- Token 管理：登录 Token 签发、校验、注销
- 账户删除：账户注销与数据清理

## 技术栈

- Java 21、Spring Boot Web
- 继承 [hojoai-parent](../hojoai-parent)

## 运行约定

| 项目 | 说明 |
|------|------|
| 可运行模块 | `hojoai-webmvc-omniasst-account-server` |
| 默认 HTTP 端口 | `12004` |
| Servlet context-path | 默认无；对外路径前缀 `/omniasst-account` |
| Spring 应用名 | `hojoai-webmvc-omniasst-account` |
| 经网关 | [hojoai-gateway-omniasst](../hojoai-gateway-omniasst) `/omniasst-account/**` |

## 构建与运行

```bash
cd hojoai-webmvc-omniasst-account
mvn clean install
cd hojoai-webmvc-omniasst-account-server && mvn spring-boot:run
```

## 文档

- [API.md](./API.md) — REST 接口一览。
