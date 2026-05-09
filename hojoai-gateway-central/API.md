# hojoai-gateway-central — API Documentation

## Overview

Central Gateway based on **Spring Cloud Gateway (WebFlux)**, default listening port as specified in `application.properties` (e.g., `11001`). The gateway itself implements minimal business REST logic; its primary role is to **route requests by path** to downstream services.

## Downstream Routes (Excerpt)

Configuration is located in `hojoai-gateway-central-server/src/main/resources/config/application-*.properties`.

| Route ID | Match Path | Downstream Service (Example) |
|----------|------------|------------------------------|
| `hojoai-webmvc-central-account` | `/central-account/**` | `lb://hojoai-webmvc-central-account` |

> Actual host and port depend on the deployment environment; when clients access through the gateway, the path prefix remains consistent with the table above.

## Filters

- `CentralGlobalFilter`: Global pre-processing logic (currently primarily for sample logging), extensible for authentication, instrumentation, etc.

## Related Documentation

For business interfaces such as central account, refer to **hojoai-webmvc-central-account**'s `API.md`.