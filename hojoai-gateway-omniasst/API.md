# hojoai-gateway-omniasst — API Documentation

## Overview

Omniasst Business Gateway (Spring Cloud Gateway), default port as specified in `application.properties` (e.g., `12001`). Responsibilities include: **Routing**, **User Token Validation for Some Paths** (`UserTokenGlobalFilter`), and **Digital Asset Links** static declarations.

## HTTP Endpoints Exposed by the Gateway

| Method | Path | Description |
|--------|------|-------------|
| GET | `/.well-known/assetlinks.json` | Android App Links declaration, content served from classpath `static/.well-known/assetlinks.json`; returns `[]` if missing |

Implementation class: `WellKnownController`.

## Route Table (Consistent with `application-*.properties`)

| Route ID | Match Path | Downstream |
|----------|------------|------------|
| `hojoai-websocket-omniasst-sysagent` | `/omniasst-sysagent/**` | `lb:ws://hojoai-websocket-omniasst-sysagent` (WebSocket) |
| `hojoai-webmvc-omniasst-account` | `/omniasst-account/**` | `lb://hojoai-webmvc-omniasst-account` |
| `hojoai-webmvc-omniasst-management` | `/omniasst-management/**` | `lb://hojoai-webmvc-omniasst-management` |
| `hojoai-webmvc-omniasst-voice` | `/omniasst-voice/**` | `lb://hojoai-webmvc-omniasst-voice` |
| `hojoai-webmvc-omniasst-translate` | `/omniasst-translate/**` | `lb://hojoai-webmvc-omniasst-translate` |
| `hojoai-webmvc-omniasst-meeting` | `/omniasst-meeting/**` | `lb://hojoai-webmvc-omniasst-meeting` |
| `hojoai-webmvc-omniasst-focus` | `/omniasst-focus/**` | `lb://hojoai-webmvc-omniasst-focus` |
| `hojoai-webmvc-omniasst-rtc` | `/omniasst-rtc/**` | `lb://hojoai-webmvc-omniasst-rtc` |
| `hojoai-webmvc-omniasst-music` | `/omniasst-music/**` | `lb://hojoai-webmvc-omniasst-music` |
| `hojoai-websocket-omniasst-news` | `/omniasst-news/**` | `lb://ws://hojoai-websocket-omniasst-news` |
| `hojoai-webmvc-omniasst-knowledge` | `/omniasst-knowledge/**` | `lb://hojoai-webmvc-omniasst-knowledge` |
| `hojoai-webmvc-omniasst-model` | `/omniasst-model/**` | `lb://hojoai-webmvc-omniasst-model` |

> Some environment configuration files (e.g., `application-dev.properties`) may contain only a subset of routes; refer to the currently active profile.

## Token Validation Summary

`UserTokenGlobalFilter` requires requests to certain paths and methods in the configuration table to carry headers `X-User-Id`, `X-User-Token`, and `X-Platform-Type`, and validates them via the account service. Routes **not** listed in the token validation table are **not** processed by this filter. See source code `UserTokenGlobalFilter.USER_TOKEN_REQUIRED_ROUTES` for details.

## Related Documentation

REST/WebSocket definitions for each downstream service are located in the respective module's `API.md`.