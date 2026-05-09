# hojoai-webmvc-omniasst-account — API 文档

## 基址

- 默认 **无** servlet context-path
- 经 **hojoai-gateway-omniasst** 访问时，使用前缀 `/omniasst-account`（与控制器 `@RequestMapping` 一致）

---

## 短信验证码 — `/omniasst-account`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/omniasst-account/mobile-captchas` | 发送手机验证码 |

## 用户 — `/omniasst-account`

| 方法 | 路径 | 查询参数 | 说明 |
|------|------|----------|------|
| POST | `/omniasst-account/users` | `mobileCaptcha` | 注册/创建用户 |
| PATCH | `/omniasst-account/users/{userId}` | `patchType` | 更新用户 |
| GET | `/omniasst-account/users/{userId}` | — | 用户详情 |
| GET | `/omniasst-account/users` | `userMobile` | 按手机号查询 |

## Token — `/omniasst-account`

| 方法 | 路径 | 查询参数 | 说明 |
|------|------|----------|------|
| POST | `/omniasst-account/tokens` | `userMobile`, `mobileCaptcha` | 登录签发 |
| DELETE | `/omniasst-account/tokens` | `userId` 或 `userId`+`platformType` | 登出/清理 |
| GET | `/omniasst-account/tokens` | `userId`, `platformType`, `userToken` | Token 校验 |

---

## 海外认证 — `/omniasst-account/auth`

| 方法 | 路径 | 请求头 / 参数 | 说明 |
|------|------|---------------|------|
| POST | `/omniasst-account/auth/login/google` | Body: `idToken`, `platformType` | Google 登录 |
| POST | `/omniasst-account/auth/login/apple` | Body: `identityToken`, `platformType` | Apple 登录 |
| POST | `/omniasst-account/auth/email/send-code` | Body: `email` | 发邮箱验证码 |
| POST | `/omniasst-account/auth/email/login` | Body: `email`, `code`, `platformType` | 邮箱 OTP 登录 |
| POST | `/omniasst-account/auth/email/send-magic-link` | Body: `email`, `platformType` | Magic Link 邮件 |
| GET | `/omniasst-account/auth/magic/status` | `loginToken` | 轮询 Magic Link 状态 |
| GET | `/omniasst-account/auth/magic/verify` | `verifyToken` | 邮件内链接验证，返回 HTML |
| POST | `/omniasst-account/auth/magic/verify` | Body: `token` | JSON 验证 Magic Link |
| POST | `/omniasst-account/auth/logout` | `X-User-Id`, `X-User-Token` | 登出 |
| POST | `/omniasst-account/auth/account/delete` | `X-User-Id`, `X-User-Token` | 注销账号 |

实现类：`UserCaptchaController`、`UserInfoController`、`UserTokenInfoController`、`AuthController`。
