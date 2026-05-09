# hojoai-common

HoJoAI system's **Java shared utility library** (`jar`), managed by [hojoai-parent](../hojoai-parent) for dependency versions.

## Responsibilities

- Provides unified HTTP response wrappers, pagination, assertions, Jackson/date utility types, etc.
- **Not an independent process**: does not listen on ports, does not expose HTTP, gRPC, or WebSocket.

## Usage

Depend on this module in other services' `pom.xml` (version aligned with `hojoai-parent` / `hojoai-common.version`), and reference types and utility classes under `com.hojoai.common.*` as needed.

## Documentation

- [API.md](./API.md) — External reusable types and interface descriptions (source code is authoritative).

## Build

```bash
cd hojoai-common && mvn clean install
```