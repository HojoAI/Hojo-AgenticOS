# hojoai-gateway-omniasst

**Omniasst API Gateway**: Built on Spring Cloud Gateway, responsible for routing forwarding, user token validation, Digital Asset Links, and other gateway-side capabilities, providing a unified entry point for the Omniasst omnidirectional assistant business line.

## Capabilities

- Routing Forwarding: Routes requests to 12 downstream microservices by path (including HTTP and WebSocket)
- Token Validation: Global user token validation filter to protect sensitive paths such as login/register
- Digital Asset Links: Provides `/.well-known/assetlinks.json` to support Android App Links verification
- Brand Response Headers: Injects brand-related response headers
- Access Logging: Records all request access logs

## Tech Stack

- Java 21, Spring Cloud Gateway (WebFlux)
- Nacos; Global filters such as `UserTokenGlobalFilter` (see source code and [API.md](./API.md))

## Running Conventions

| Item | Description |
|------|------|
| Runnable Module | `hojoai-gateway-omniasst-server` |
| Default HTTP Port | `12001` |
| Spring Application Name | `hojoai-gateway-omniasst` |

## Build and Run

```bash
cd hojoai-gateway-omniasst
mvn clean install
cd hojoai-gateway-omniasst-server && mvn spring-boot:run
```

## Documentation

- [API.md](./API.md) — Gateway's own paths, routing table, token validation summary, and downstream `API.md` index.