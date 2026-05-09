# hojoai-grpc-central-provider

**Central Domain gRPC Service**: Provides RPC capabilities for orders, movies, flights, trains, push notifications, etc., with a Spring Boot HTTP port for health checks and management.

## Capabilities

- Order RPC: Order query, creation, status update
- Movie RPC: Movie, cinema, showtime, seat query
- Flight RPC: Flight query and booking
- Train RPC: Train ticket query and booking
- Push RPC: Message push capability
- Health Check: HTTP port provides health check and management endpoints

## Module Structure

| Module | Description |
|------|------|
| `hojoai-grpc-central-provider-api` | `.proto` and generated code (for server and client dependencies) |
| `hojoai-grpc-central-provider-server` | Runnable gRPC server |

## Tech Stack

- Java 21, gRPC, Protobuf
- Spring Boot, Nacos (includes `grpc-port` and other metadata)

## Runtime Conventions

| Item | Description |
|------|------|
| Default HTTP Port | `11003` (`server.port`) |
| gRPC Port | `grpc.port` in `config/application-dev.properties` (e.g., **11103**) |
| Spring Application Name | `hojoai-grpc-central-provider` |

Clients should access **`host:grpc.port`**; the HTTP port is generally not used for business APIs.

## Build and Run

```bash
cd hojoai-grpc-central-provider
mvn clean install
cd hojoai-grpc-central-provider-server && mvn spring-boot:run
```

## Documentation

- [API.md](./API.md) — Service/RPC overview and proto location description.
- Sub-module [hojoai-grpc-central-provider-api/README.md](./hojoai-grpc-central-provider-api/README.md) (for separate api package documentation if needed).