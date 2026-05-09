# hojoai-gateway-central

**Central Domain API Gateway**: Built on Spring Cloud Gateway, responsible for forwarding external requests to downstream microservices (such as the Central Account Service) by path, providing a unified entry point and basic filtering capabilities.

## Capabilities

- Routing Forwarding: Forwards requests to Central Domain microservices
- Health Check: Spring Boot Actuator health endpoint

## Tech Stack

- Java 21, Spring Boot (version see [hojoai-parent](../hojoai-parent))
- Spring Cloud Gateway, Nacos (service discovery and configuration, enabled per environment)

## Running Conventions

| Item | Description |
|------|------|
| Module Structure | Aggregator project, runnable module is `hojoai-gateway-central-server` |
| Default HTTP Port | `11001` (see `hojoai-gateway-central-server/.../application.properties`) |
| Spring Application Name | `hojoai-gateway-central` |

## Build and Run

```bash
cd hojoai-gateway-central
mvn clean install
cd hojoai-gateway-central-server && mvn spring-boot:run
```

## Documentation

- [API.md](./API.md) — Routing table, filter description, and downstream documentation index.