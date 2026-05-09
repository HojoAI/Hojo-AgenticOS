# hojoai-webmvc-central-account

**Central Domain Account and Authentication Service**: Provides SMS verification code, user registration and query, token issuance/verification/revocation, and other basic authentication capabilities, plus international authentication method support.

## Capabilities

- SMS Verification: Verification code sending and verification
- User Registration/Query: User creation and information query
- Token Management: Token issuance, verification, and revocation
- International Authentication: International third-party authentication integration
- Account Deletion: Account cancellation capability

## Tech Stack

- Java 21, Spring Boot Web
- Inherits [hojoai-parent](../hojoai-parent); typically works with Nacos, MySQL, etc. (per environment configuration)

## Runtime Conventions

| Item | Description |
|------|------|
| Runnable Module | `hojoai-webmvc-central-account-server` |
| Default HTTP Port | `11002` |
| Servlet context-path | None by default; controller paths prefixed with `/central-account` |
| Spring Application Name | `hojoai-webmvc-central-account` |
| Via Gateway | [hojoai-gateway-central](../hojoai-gateway-central) prefix `/central-account/**` |

## Build and Run

```bash
cd hojoai-webmvc-central-account
mvn clean install
cd hojoai-webmvc-central-account-server && mvn spring-boot:run
```

## Documentation

- [API.md](./API.md) — HTTP paths and parameter descriptions.