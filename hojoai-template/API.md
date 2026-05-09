# hojoai-template — API Documentation

## Base URL

- Default **no** `server.servlet.context-path` (root path `/`)
- Default port is in `application.properties` (e.g., `12000`)

## REST Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/template/helloWorld` | Example greeting endpoint; request body `HelloWorldRequest` (requires non-empty `asking`); response is `Response<HelloWorldResponse>` |

Implementation class: `HelloWorldController`.

## Description

This module is a **project template/example**. For production, replace paths and implementation according to business requirements.