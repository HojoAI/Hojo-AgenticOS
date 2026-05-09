# 海外 App 登录认证 - 接口文档

接口基础路径：`/omniasst-account/auth`

## 一、Google 登录

### 1. 接口简介

客户端通过 Google Sign-In SDK 获取 ID Token 后，调用此接口完成登录。后端验证 Token 有效性，自动注册（首次）或查找已有用户，返回 userId 和 userToken。

### 2. 请求说明

| 名称 | 内容 |
|------|------|
| 接口地址 | /omniasst-account/auth/login/google |
| 请求方式 | POST |
| Content-Type | application/json |

### 3. 请求参数

#### 3.1 Header 参数

无

#### 3.2 Query 参数

无

#### 3.3 Body 参数

| 参数 | 可选/必选 | 类型 | 说明 |
|------|-----------|------|------|
| idToken | 必选 | String | 客户端从 Google SDK 获取的 ID Token（JWT 格式） |
| platformType | 必选 | Integer | 平台类型（1=iOS, 2=Android） |

### 4. 返回参数

| 参数 | 是否必返回 | 类型 | 说明 |
|------|------------|------|------|
| resCode | 是 | String | 状态码，成功为 OK |
| resMessage | 是 | String | 状态描述 |
| resContent.userId | 是 | String | 用户 ID |
| resContent.userToken | 是 | String | 用户令牌（SHA-256），后续请求放入 Header |
| resContent.businessType | 是 | Integer | 业务类型，固定为 1 |
| resContent.platformType | 是 | Integer | 平台类型，与请求一致 |

### 5. 请求&返回完整示例

#### 5.1 请求示例

```json
{
    "idToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "platformType": 1
}
```

#### 5.2 返回示例（成功）

```json
{
    "resCode": "OK",
    "resMessage": "成功",
    "resContent": {
        "userId": "1234567890123456",
        "userToken": "A1B2C3D4E5F6...",
        "businessType": 1,
        "platformType": 1
    }
}
```

#### 5.3 返回示例（失败）

```json
{
    "resCode": "INVALID_GOOGLE_TOKEN",
    "resMessage": "Google Token verification failed"
}
```

---

## 二、Apple 登录

### 1. 接口简介

iOS 客户端通过 Apple Sign-In SDK 获取 identityToken 后，调用此接口完成登录。Apple 仅在首次授权时返回 fullName 和 email，客户端必须在首次时一并传入。

### 2. 请求说明

| 名称 | 内容 |
|------|------|
| 接口地址 | /omniasst-account/auth/login/apple |
| 请求方式 | POST |
| Content-Type | application/json |

### 3. 请求参数

#### 3.1 Header 参数

无

#### 3.2 Query 参数

无

#### 3.3 Body 参数

| 参数 | 可选/必选 | 类型 | 说明 |
|------|-----------|------|------|
| identityToken | 必选 | String | 客户端从 Apple SDK 获取的 identityToken（JWT 格式） |
| fullName | 可选 | String | 用户姓名，仅首次授权时由客户端传入，后续传 null |
| email | 可选 | String | 用户邮箱，仅首次授权时由客户端传入，后续传 null |
| platformType | 必选 | Integer | 平台类型（1=iOS, 2=Android） |

### 4. 返回参数

| 参数 | 是否必返回 | 类型 | 说明 |
|------|------------|------|------|
| resCode | 是 | String | 状态码，成功为 OK |
| resMessage | 是 | String | 状态描述 |
| resContent.userId | 是 | String | 用户 ID |
| resContent.userToken | 是 | String | 用户令牌（SHA-256），后续请求放入 Header |
| resContent.businessType | 是 | Integer | 业务类型，固定为 1 |
| resContent.platformType | 是 | Integer | 平台类型，与请求一致 |

### 5. 请求&返回完整示例

#### 5.1 请求示例（首次授权）

```json
{
    "identityToken": "eyJraWQiOiJZdXlYb1kiLCJhbGciOiJSUzI1NiJ9...",
    "fullName": "John Doe",
    "email": "john@example.com",
    "platformType": 1
}
```

#### 5.2 请求示例（非首次授权）

```json
{
    "identityToken": "eyJraWQiOiJZdXlYb1kiLCJhbGciOiJSUzI1NiJ9...",
    "platformType": 1
}
```

#### 5.3 返回示例（成功）

```json
{
    "resCode": "OK",
    "resMessage": "成功",
    "resContent": {
        "userId": "1234567890123456",
        "userToken": "A1B2C3D4E5F6...",
        "businessType": 1,
        "platformType": 1
    }
}
```

#### 5.4 返回示例（失败）

```json
{
    "resCode": "INVALID_APPLE_TOKEN",
    "resMessage": "Apple Token verification failed"
}
```

---

## 三、发送邮件验证码

### 1. 接口简介

向指定邮箱发送 6 位数字验证码，用于 Email OTP 登录。同一邮箱 60 秒内只能发送一次，验证码有效期 5 分钟。

### 2. 请求说明

| 名称 | 内容 |
|------|------|
| 接口地址 | /omniasst-account/auth/email/send-code |
| 请求方式 | POST |
| Content-Type | application/json |

### 3. 请求参数

#### 3.1 Header 参数

无

#### 3.2 Query 参数

无

#### 3.3 Body 参数

| 参数 | 可选/必选 | 类型 | 说明 |
|------|-----------|------|------|
| email | 必选 | String | 目标邮箱地址 |

### 4. 返回参数

| 参数 | 是否必返回 | 类型 | 说明 |
|------|------------|------|------|
| resCode | 是 | String | 状态码，成功为 OK |
| resMessage | 是 | String | 状态描述 |

### 5. 请求&返回完整示例

#### 5.1 请求示例

```json
{
    "email": "user@example.com"
}
```

#### 5.2 返回示例（成功）

```json
{
    "resCode": "OK",
    "resMessage": "成功"
}
```

#### 5.3 返回示例（频率限制）

```json
{
    "resCode": "OTP_RATE_LIMITED",
    "resMessage": "Please wait before requesting a new code"
}
```

---

## 四、Email OTP 登录

### 1. 接口简介

使用邮箱和验证码完成登录。验证码由「发送邮件验证码」接口发送，同一验证码最多尝试 5 次，超过后作废。

### 2. 请求说明

| 名称 | 内容 |
|------|------|
| 接口地址 | /omniasst-account/auth/email/login |
| 请求方式 | POST |
| Content-Type | application/json |

### 3. 请求参数

#### 3.1 Header 参数

无

#### 3.2 Query 参数

无

#### 3.3 Body 参数

| 参数 | 可选/必选 | 类型 | 说明 |
|------|-----------|------|------|
| email | 必选 | String | 邮箱地址 |
| code | 必选 | String | 6 位验证码 |
| platformType | 必选 | Integer | 平台类型（1=iOS, 2=Android） |

### 4. 返回参数

| 参数 | 是否必返回 | 类型 | 说明 |
|------|------------|------|------|
| resCode | 是 | String | 状态码，成功为 OK |
| resMessage | 是 | String | 状态描述 |
| resContent.userId | 是 | String | 用户 ID |
| resContent.userToken | 是 | String | 用户令牌（SHA-256），后续请求放入 Header |
| resContent.businessType | 是 | Integer | 业务类型，固定为 1 |
| resContent.platformType | 是 | Integer | 平台类型，与请求一致 |

### 5. 请求&返回完整示例

#### 5.1 请求示例

```json
{
    "email": "user@example.com",
    "code": "385720",
    "platformType": 2
}
```

#### 5.2 返回示例（成功）

```json
{
    "resCode": "OK",
    "resMessage": "成功",
    "resContent": {
        "userId": "1234567890123456",
        "userToken": "A1B2C3D4E5F6...",
        "businessType": 1,
        "platformType": 2
    }
}
```

#### 5.3 返回示例（验证码错误）

```json
{
    "resCode": "INVALID_OTP",
    "resMessage": "Invalid verification code"
}
```

#### 5.4 返回示例（验证码过期）

```json
{
    "resCode": "OTP_EXPIRED",
    "resMessage": "Verification code expired"
}
```

#### 5.5 返回示例（尝试次数超限）

```json
{
    "resCode": "OTP_ATTEMPTS_EXCEEDED",
    "resMessage": "Too many attempts, please request a new code"
}
```

---

## 五、发送 Magic Link

### 1. 接口简介

向指定邮箱发送登录链接（Magic Link）。邮件中链接由配置项 `oversea.auth.magic-link.base-url` + `?token=` + loginToken 组成，指向 `GET /auth/magic/verify`，用户点击即触发后端校验，无需再点 H5 按钮。App 通过轮询接口获取登录结果。与验证码共用 60 秒发送频率限制。

### 2. 请求说明

| 名称 | 内容 |
|------|------|
| 接口地址 | /omniasst-account/auth/email/send-magic-link |
| 请求方式 | POST |
| Content-Type | application/json |

### 3. 请求参数

#### 3.1 Header 参数

无

#### 3.2 Query 参数

无

#### 3.3 Body 参数

| 参数 | 可选/必选 | 类型 | 说明 |
|------|-----------|------|------|
| email | 必选 | String | 目标邮箱地址 |
| platformType | 必选 | Integer | 平台类型（1=iOS, 2=Android） |

### 4. 返回参数

| 参数 | 是否必返回 | 类型 | 说明 |
|------|------------|------|------|
| resCode | 是 | String | 状态码，成功为 OK |
| resMessage | 是 | String | 状态描述 |
| resContent.loginToken | 是 | String | 登录令牌（UUID），App 使用此值轮询状态 |

### 5. 请求&返回完整示例

#### 5.1 请求示例

```json
{
    "email": "user@example.com",
    "platformType": 1
}
```

#### 5.2 返回示例（成功）

```json
{
    "resCode": "OK",
    "resMessage": "成功",
    "resContent": {
        "loginToken": "550e8400-e29b-41d4-a716-446655440000"
    }
}
```

#### 5.3 返回示例（频率限制）

```json
{
    "resCode": "OTP_RATE_LIMITED",
    "resMessage": "Please wait before requesting a new code"
}
```

---

## 六、Magic Link 轮询

### 1. 接口简介

App 发送 Magic Link 后，每 3 秒调用此接口轮询登录状态。当用户在邮件中点击链接（后端完成校验）后，此接口返回 `success` 状态与登录凭证。链接有效期 15 分钟。

**状态说明（`resContent.status`）**

| 值 | 含义 |
|----|------|
| `pending` | 待验证：用户尚未完成链接校验，或校验尚未写入 Redis |
| `success` | 成功：本次轮询返回 `userId` / `userToken` 等（仅会出现一次，随后同 token 会变为 `failed`） |
| `failed` | 失败：见 `resContent.failureReason`（`expired`：过期或无效；`consumed`：凭证已被取走） |

### 2. 请求说明

| 名称 | 内容 |
|------|------|
| 接口地址 | /omniasst-account/auth/magic/status |
| 请求方式 | GET |
| Content-Type | 无（GET 请求） |

### 3. 请求参数

#### 3.1 Header 参数

无

#### 3.2 Query 参数

| 参数 | 可选/必选 | 类型 | 说明 |
|------|-----------|------|------|
| loginToken | 必选 | String | 发送 Magic Link 时返回的登录令牌 |

#### 3.3 Body 参数

无

### 4. 返回参数

| 参数 | 是否必返回 | 类型 | 说明 |
|------|------------|------|------|
| resCode | 是 | String | 轮询语义下恒为 OK（含 pending/success/failed）；业务结果看 `resContent.status` |
| resMessage | 是 | String | 状态描述 |
| resContent.status | 是 | String | `pending` / `success` / `failed` |
| resContent.failureReason | 否 | String | 仅 `failed` 时：`expired` 或 `consumed` |
| resContent.userId | 否 | String | 用户 ID，仅 `success` 时返回 |
| resContent.userToken | 否 | String | 用户令牌（SHA-256），仅 `success` 时返回 |
| resContent.businessType | 否 | Integer | 业务类型，仅 `success` 时返回 |
| resContent.platformType | 否 | Integer | 平台类型，仅 `success` 时返回 |

### 5. 请求&返回完整示例

#### 5.1 请求示例

```
GET /omniasst-account/auth/magic/status?loginToken=550e8400-e29b-41d4-a716-446655440000
```

#### 5.2 返回示例（等待中）

```json
{
    "resCode": "OK",
    "resMessage": "成功",
    "resContent": {
        "status": "pending"
    }
}
```

#### 5.3 返回示例（验证成功）

```json
{
    "resCode": "OK",
    "resMessage": "成功",
    "resContent": {
        "status": "success",
        "userId": "1234567890123456",
        "userToken": "A1B2C3D4E5F6...",
        "businessType": 1,
        "platformType": 1
    }
}
```

#### 5.4 返回示例（失败：过期或无效 token）

```json
{
    "resCode": "OK",
    "resMessage": "成功",
    "resContent": {
        "status": "failed",
        "failureReason": "expired"
    }
}
```

#### 5.5 返回示例（失败：凭证已被取走）

再次轮询同一 `loginToken` 时，在短时窗口内会返回 `consumed`（Redis 墓碑约 120 秒）；之后与 5.4 相同为 `expired`。

```json
{
    "resCode": "OK",
    "resMessage": "成功",
    "resContent": {
        "status": "failed",
        "failureReason": "consumed"
    }
}
```

---

## 七、Magic Link 验证

### 1. 接口简介

完成邮箱 Magic Link 登录的最后一步。支持两种调用方式：

- **GET**（备用/直连后端场景）：`GET /omniasst-account/auth/magic/verify?verifyToken={loginToken}`，浏览器直接请求后端完成校验，返回 HTML 提示页。
- **POST**（主流程，前端 H5 调用）：前端确认页从 URL 中取 `verifyToken`，再 POST 到本接口，body 为 `{ "token": "..." }`。

**当前主流程**：邮件链接 → 前端 H5 确认页（`/pa/general/magic-link?verifyToken=xxx`）→ H5 调 POST → App 轮询拿结果。token 一次性使用，验证成功后不可重复调用。

### 2. 请求说明（GET，备用）

| 名称 | 内容 |
|------|------|
| 接口地址 | /omniasst-account/auth/magic/verify |
| 请求方式 | GET |
| Content-Type | 无 |

Query 参数：`verifyToken`（必选）= 邮件中的 loginToken。

**返回**：`Content-Type: text/html`，成功为简单成功页，失败为错误说明页（非 JSON）。

### 3. 请求说明（POST，前端 H5 调用）

| 名称 | 内容 |
|------|------|
| 接口地址 | /omniasst-account/auth/magic/verify |
| 请求方式 | POST |
| Content-Type | application/json |

#### 3.1 Header 参数

无

#### 3.2 Query 参数

无

#### 3.3 Body 参数

| 参数 | 可选/必选 | 类型 | 说明 |
|------|-----------|------|------|
| token | 必选 | String | 前端从 URL `verifyToken` 参数取到的值 |

### 4. 返回参数（仅 POST 为 JSON）

与轮询接口一致，`resCode` 永远返回 `OK`，以 `resContent.status` 区分结果。

| 参数 | 是否必返回 | 类型 | 说明 |
|------|------------|------|------|
| resCode | 是 | String | 永远为 `OK`（业务异常也如此）；以 `resContent.status` 判断结果 |
| resMessage | 是 | String | 状态描述 |
| resContent.status | 是 | String | `success`（验证通过）/ `failed`（失败，见 failureReason） |
| resContent.failureReason | 否 | String | 仅 `failed` 时：`expired`（链接过期或无效）/ `consumed`（链接已被使用） |

### 5. 请求&返回完整示例

#### 5.1 GET 请求示例（备用，直连后端）

```
GET /omniasst-account/auth/magic/verify?verifyToken=550e8400-e29b-41d4-a716-446655440000
```

#### 5.2 POST 请求示例（前端 H5 调用）

```json
{
    "token": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### 5.3 POST 返回示例（成功）

```json
{
    "resCode": "OK",
    "resMessage": "成功",
    "resContent": {
        "status": "success"
    }
}
```

#### 5.4 POST 返回示例（失败：链接过期或无效）

```json
{
    "resCode": "OK",
    "resMessage": "成功",
    "resContent": {
        "status": "failed",
        "failureReason": "expired"
    }
}
```

#### 5.5 POST 返回示例（失败：链接已被使用）

```json
{
    "resCode": "OK",
    "resMessage": "成功",
    "resContent": {
        "status": "failed",
        "failureReason": "consumed"
    }
}
```

---

## 八、登出

### 1. 接口简介

登出当前用户。删除该用户在 user_token_info 表中的所有 token 记录，后续使用已有 token 的请求会因校验失败而被拒绝。

### 2. 请求说明

| 名称 | 内容 |
|------|------|
| 接口地址 | /omniasst-account/auth/logout |
| 请求方式 | POST |
| Content-Type | 无（无请求体） |

### 3. 请求参数

#### 3.1 Header 参数

| 参数 | 可选/必选 | 类型 | 说明 |
|------|-----------|------|------|
| X-User-Id | 必选 | String | 用户 ID |
| X-User-Token | 必选 | String | 用户令牌 |

#### 3.2 Query 参数

无

#### 3.3 Body 参数

无

### 4. 返回参数

| 参数 | 是否必返回 | 类型 | 说明 |
|------|------------|------|------|
| resCode | 是 | String | 状态码，成功为 OK |
| resMessage | 是 | String | 状态描述 |

### 5. 请求&返回完整示例

#### 5.1 请求示例

```
POST /omniasst-account/auth/logout
X-User-Id: 1234567890123456
X-User-Token: A1B2C3D4E5F6...
```

#### 5.2 返回示例（成功）

```json
{
    "resCode": "OK",
    "resMessage": "成功"
}
```

---

## 九、账号注销

### 1. 接口简介

永久注销当前账号。将海外用户状态标记为 deleted，同时删除 user_token_info 中所有 token 记录。Apple App Store 和 Google Play 强制要求支持此功能。

### 2. 请求说明

| 名称 | 内容 |
|------|------|
| 接口地址 | /omniasst-account/auth/account/delete |
| 请求方式 | POST |
| Content-Type | 无（无请求体） |

### 3. 请求参数

#### 3.1 Header 参数

| 参数 | 可选/必选 | 类型 | 说明 |
|------|-----------|------|------|
| X-User-Id | 必选 | String | 用户 ID |
| X-User-Token | 必选 | String | 用户令牌 |

#### 3.2 Query 参数

无

#### 3.3 Body 参数

无

### 4. 返回参数

| 参数 | 是否必返回 | 类型 | 说明 |
|------|------------|------|------|
| resCode | 是 | String | 状态码，成功为 OK |
| resMessage | 是 | String | 状态描述 |

### 5. 请求&返回完整示例

#### 5.1 请求示例

```
POST /omniasst-account/auth/account/delete
X-User-Id: 1234567890123456
X-User-Token: A1B2C3D4E5F6...
```

#### 5.2 返回示例（成功）

```json
{
    "resCode": "OK",
    "resMessage": "成功"
}
```

#### 5.3 返回示例（已注销）

```json
{
    "resCode": "ACCOUNT_DELETED",
    "resMessage": "Account has been deleted"
}
```

---

## 附录一：登录后 Header 约定

海外登录成功后，客户端在后续所有请求的 Header 中携带以下两个字段（与国内登录一致）：

| Header 名称 | 说明 |
|-------------|------|
| X-User-Id | 用户 ID（登录返回的 userId） |
| X-User-Token | 用户令牌（登录返回的 userToken） |

---

## 附录二：错误码汇总

| resCode | resMessage | 触发场景 |
|---------|------------|----------|
| OK | 成功 | 请求成功 |
| INVALID_GOOGLE_TOKEN | Google Token verification failed | Google ID Token 签名/audience/过期校验不通过 |
| INVALID_APPLE_TOKEN | Apple Token verification failed | Apple identityToken 签名/iss/aud/过期校验不通过 |
| INVALID_OTP | Invalid verification code | Email 验证码不匹配 |
| OTP_EXPIRED | Verification code expired | Redis 中验证码已过期 |
| OTP_RATE_LIMITED | Please wait before requesting a new code | 同一邮箱 60 秒内重复请求 |
| OTP_ATTEMPTS_EXCEEDED | Too many attempts, please request a new code | 同一验证码错误尝试超过 5 次 |
| TOKEN_REVOKED | Token has been revoked | token 校验失败 |
| MAGIC_LINK_EXPIRED | Login link expired | loginToken 在 Redis 中不存在或已过期 |
| MAGIC_LINK_USED | Login link already used | 同一 loginToken 被二次验证 |
| ACCOUNT_DISABLED | Account is disabled | 用户状态为 disabled |
| ACCOUNT_DELETED | Account has been deleted | 用户状态为 deleted |
| GENERAL_ERROR | 系统异常 | 未处理的 Exception |
| ILLEGAL_ARGUMENT | 非法参数 | 必填参数缺失或格式错误 |
