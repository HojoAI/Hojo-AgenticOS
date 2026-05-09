# hojoai-webmvc-omniasst-account — API Documentation

## Base Address

- Default **no** servlet context-path
- When accessed through **hojoai-gateway-omniasst**, use prefix `/omniasst-account` (consistent with controller `@RequestMapping`)

---

## SMS Verification Code — `/omniasst-account`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/omniasst-account/mobile-captchas` | Send mobile verification code |

## User — `/omniasst-account`

| Method | Path | Query Parameters | Description |
|--------|------|------------------|-------------|
| POST | `/omniasst-account/users` | `mobileCaptcha` | Register/create user |
| PATCH | `/omniasst-account/users/{userId}` | `patchType` | Update user |
| GET | `/omniasst-account/users/{userId}` | — | User details |
| GET | `/omniasst-account/users` | `userMobile` | Query by mobile number |

## Token — `/omniasst-account`

| Method | Path | Query Parameters | Description |
|--------|------|------------------|-------------|
| POST | `/omniasst-account/tokens` | `userMobile`, `mobileCaptcha` | Login/issue token |
| DELETE | `/omniasst-account/tokens` | `userId` or `userId`+`platformType` | Logout/cleanup |
| GET | `/omniasst-account/tokens` | `userId`, `platformType`, `userToken` | Token validation |

---

## International Authentication — `/omniasst-account/auth`

| Method | Path | Request Header / Parameters | Description |
|--------|------|-----------------------------|-------------|
| POST | `/omniasst-account/auth/login/google` | Body: `idToken`, `platformType` | Google login |
| POST | `/omniasst-account/auth/login/apple` | Body: `identityToken`, `platformType` | Apple login |
| POST | `/omniasst-account/auth/email/send-code` | Body: `email` | Send email verification code |
| POST | `/omniasst-account/auth/email/login` | Body: `email`, `code`, `platformType` | Email OTP login |
| POST | `/omniasst-account/auth/email/send-magic-link` | Body: `email`, `platformType` | Magic Link email |
| GET | `/omniasst-account/auth/magic/status` | `loginToken` | Poll Magic Link status |
| GET | `/omniasst-account/auth/magic/verify` | `verifyToken` | Email link verification, returns HTML |
| POST | `/omniasst-account/auth/magic/verify` | Body: `token` | JSON verification of Magic Link |
| POST | `/omniasst-account/auth/logout` | `X-User-Id`, `X-User-Token` | Logout |
| POST | `/omniasst-account/auth/account/delete` | `X-User-Id`, `X-User-Token` | Delete account |

Implementation classes: `UserCaptchaController`, `UserInfoController`, `UserTokenInfoController`, `AuthController`.