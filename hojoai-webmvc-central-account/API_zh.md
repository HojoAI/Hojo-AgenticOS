# hojoai-webmvc-central-account — API 文档

## 基址

- 默认 **无** servlet context-path（根路径 `/`）
- 经 **hojoai-gateway-central** 访问时，路径前缀为 `/central-account/**`，下游服务控制器仍映射在 `/central-account` 下

## 短信验证码 — `/central-account`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/central-account/mobile-captchas` | 发送手机验证码 |

## 用户 — `/central-account`

| 方法 | 路径 | 查询参数 | 说明 |
|------|------|----------|------|
| POST | `/central-account/users` | `mobileCaptcha` | 注册用户等 |
| PATCH | `/central-account/users/{userId}` | `patchType` | 部分更新用户 |
| GET | `/central-account/users/{userId}` | — | 查询用户 |
| GET | `/central-account/users` | `userMobile` | 按手机号查询 |

## Token — `/central-account`

| 方法 | 路径 | 查询参数 | 说明 |
|------|------|----------|------|
| POST | `/central-account/tokens` | `userMobile`, `mobileCaptcha` | 签发 Token |
| DELETE | `/central-account/tokens` | `userId` 或 `userId`+`businessType`+`platformType` | 注销 Token |
| GET | `/central-account/tokens` | `userId`, `businessType`, `platformType`, `userToken` | 校验/查询 Token |

## 海外认证 — `/central-account/auth`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/central-account/auth/oversea-tokens` | 海外 Token 相关 |
| DELETE | `/central-account/auth/oversea-account` | `userId` 查询参数：注销海外账号 |

实现类：`UserCaptchaController`、`UserInfoController`、`UserTokenInfoController`、`OverseaAuthController`。
