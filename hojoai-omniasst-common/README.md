# hojoai-omniasst-common

**Omniasst Shared Library**: Java shared library within the Omniasst business domain, providing session and message DTOs, avatar configuration, Memory service client, domain constants and utilities.

## Responsibilities

- **Session and Message DTOs**: Push, message, free dialogue, message extension request/response and content item types under `com.hojoai.omniasst.common.api.conversation`.
- **Avatar Configuration**: `com.hojoai.omniasst.common.api.avatar` (e.g., `AvatarItem`).
- **Memory Service Client**: `MemoryApiClient` based on **Spring Cloud OpenFeign** (`/api/v1/memories`), with request/response DTOs, independent Feign configuration, and **Spring Boot 3** auto-configuration (`MemoryAutoConfiguration`).
- **Domain Constants**: Enums for message types, roles, Agent types, etc. under `com.hojoai.omniasst.common.constans.conversation` (package name per source code).
- **Utilities**: `MemoryUtils`, `SpringContextHolder`, etc. (`com.hojoai.omniasst.common.utils`).

This module is a **library**, **not** an independent Spring Boot executable application; does not listen on ports. Consumers must already have OpenFeign and Web dependencies introduced (most in this library are `provided` scope).

## Usage

1. Depend on this artifact in the business service's `pom.xml` (`groupId` / `artifactId` / `version` should be consistent with each `*-server` module).
2. Ensure the host application has Feign enabled (e.g., `@EnableFeignClients` or auto-configured by this library).
3. Memory capability: configure `hojoai.memory.api.*` in `application.yml`; full key descriptions see [application-example.yml](./application-example.yml) and [API.md](./API.md).

## Documentation

- [API.md](./API.md) — Package structure, auto-configuration, and configuration prefix descriptions (**source code is authoritative**).

## Build

Must first install parent BOM **`hojoai-parent`**, then execute in this directory:

```bash
mvn clean install -DskipTests
```

For full repo multi-module build order, see [docs/BUILD.md](../docs/BUILD.md).