# hojoai-common — API Documentation

## Overview

`hojoai-common` is a **Java shared utility library** (not a standalone process), **does not expose HTTP, gRPC, or WebSocket services**.

## External Interfaces (for reuse by other services)

When implementing REST interfaces in other microservices, the types in this module are often used as unified response wrappers or utilities:

| Type | Description |
|------|-------------|
| `com.hojoai.common.response.Response` | Unified HTTP response wrapper |
| `com.hojoai.common.response.ResponseStatus` / `ResponseStatusBasic` | Response status enums |
| `com.hojoai.common.page.IPage` | Pagination structure |
| `com.hojoai.common.asserts.Asserts` | Parameter assertions |
| `com.hojoai.common.jackson.JacksonUtil` | JSON utility |
| `com.hojoai.common.date.DateUtil` | Date utility |

For specific fields and usage, refer to the source code.