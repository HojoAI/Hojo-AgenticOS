# hojoai-webmvc-omniasst-account

**Omniasst Account Service**: Provides SMS verification code login, user registration and token management, supporting overseas and multi-platform authentication methods such as Google/Apple/Email/Magic Link.

## Capabilities

- Phone login: SMS verification code (OTP) login/register, supports sending and verifying codes
- Overseas authentication: Google OAuth, Apple OAuth, Email verification code, Magic Link
- User management: User registration, information query and update
- Token management: Login token issuance, verification, revocation
- Account deletion: Account cancellation and data cleanup

## Tech Stack

- Java 21, Spring Boot Web
- Inherits from [hojoai-parent](../hojoai-parent)

## Runtime Conventions

| Item | Description |
|------|-------------|
| Runnable module | `hojoai-webmvc-omniasst-account-server` |
| Default HTTP port | `12004` |
| Servlet context-path | None by default; external path prefix is `/omniasst-account` |
| Spring application name | `hojoai-webmvc-omniasst-account` |
| Via gateway | [hojoai-gateway-omniasst](../hojoai-gateway-omniasst) `/omniasst-account/**` |

## Build and Run

```bash
cd hojoai-webmvc-omniasst-account
mvn clean install
cd hojoai-webmvc-omniasst-account-server && mvn spring-boot:run
```

## Documentation

- [API.md](./API.md) — REST API reference.