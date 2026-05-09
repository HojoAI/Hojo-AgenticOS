# hojoai-webmvc-central-account — API Documentation

## Base URL

- Default **no** servlet context-path (root path `/`)
- When accessed via **hojoai-gateway-central**, the path prefix is `/central-account/**`, and downstream service controllers are still mapped under `/central-account`

## SMS Verification — `/central-account`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/central-account/mobile-captchas` | Send mobile verification code |

## User — `/central-account`

| Method | Path | Query Params | Description |
|--------|------|--------------|-------------|
| POST | `/central-account/users` | `mobileCaptcha` | Register user etc. |
| PATCH | `/central-account/users/{userId}` | `patchType` | Partial update user |
| GET | `/central-account/users/{userId}` | — | Query user |
| GET | `/central-account/users` | `userMobile` | Query by mobile |

## Token — `/central-account`

| Method | Path | Query Params | Description |
|--------|------|--------------|-------------|
| POST | `/central-account/tokens` | `userMobile`, `mobileCaptcha` | Issue token |
| DELETE | `/central-account/tokens` | `userId` or `userId`+`businessType`+`platformType` | Revoke token |
| GET | `/central-account/tokens` | `userId`, `businessType`, `platformType`, `userToken` | Validate/query token |

## Overseas Authentication — `/central-account/auth`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/central-account/auth/oversea-tokens` | Overseas token related |
| DELETE | `/central-account/auth/oversea-account` | `userId` query param: revoke overseas account |

Implementation classes: `UserCaptchaController`, `UserInfoController`, `UserTokenInfoController`, `OverseaAuthController`.