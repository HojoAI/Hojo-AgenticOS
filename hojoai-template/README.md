# hojoai-template

Reference template when creating a new HoJoAI **Spring Boot microservice**: pre-configured with Web, Actuator, AOP, load balancing, and common dependencies, easy to copy and modify from the unified parent project.

## Tech Stack

- Inherits [hojoai-parent](../hojoai-parent)
- Spring Boot Web, Actuator, AOP
- Spring Cloud LoadBalancer, etc. (see `pom.xml` for details)

## Runtime Conventions

| Item | Description |
|------|------|
| Default HTTP Port | `12000` |
| Spring Application Name | `hojoai-template` |

## Quick Start

```bash
cd hojoai-template
mvn clean install
mvn spring-boot:run
```

When copying to a new service, modify `artifactId`, `spring.application.name`, package name, and business code, and connect to the actual registry and configuration center.

## Documentation

- **[API.md](./API.md)** — Example interfaces and request body field descriptions.