# hojoai-grpc-central-provider — API Documentation

## Overview

This service exposes **gRPC** interfaces (另有 HTTP port `server.port` for Spring Boot management/health checks, default such as `11003`).
gRPC listening port is specified in each environment's `config/application-*.properties` as **`grpc.port`** (e.g., `11103`), and can be discovered via Nacos metadata `grpc-port`.

## Services and RPC Summary

Proto files are located in `hojoai-grpc-central-provider-api/src/main/proto/`.

### `hello.HelloWorldService` (package `hello`)

| RPC | Request | Response |
|-----|---------|----------|
| `sayHello` | `HelloWorldSayHelloRequest` | `HelloWorldDTO` |

Java package: `com.hojoai.central.provider.service.hello`.

### `order.OrderService`

| RPC | Description |
|-----|-------------|
| `createOrder` | Create order |
| `getOrderDetail` | Order detail |
| `alipayNotify` | Alipay callback |
| `panheNotify` | Panhe callback |

### `movie.MovieService`

| RPC | Description |
|-----|-------------|
| `getFilmList` | Hot/upcoming film list |
| `getScheduleCinemaList` | Film schedule cinemas |
| `getFilmShowList` | Showtimes |
| `getShowSeatList` | Seats |
| `getCinemaList` | City cinema list |
| `getCinemaFilmList` | Cinema details and films |
| `getFilmScheduleList` | Film info and upcoming schedules |

### `flight.FlightService`

| RPC | Description |
|-----|-------------|
| `getFlightByNumberAndDate` | Query by flight number and date |

### `train.TrainService`

| RPC | Description |
|-----|-------------|
| `getByTrainNumberAndStations` | Train number + departure/arrival stations |
| `getByStationsAndDate` | Stations + date |
| `getByCitiesAndDate` | Cities + date |
| `getTrainTable` | Unified query entry |

### `push.PushService`

| RPC | Description |
|-----|-------------|
| `push` | Push (request body is JSON string field `pushReqStr`) |

## Client Invocation

Use stubs generated from the corresponding `.proto`, with target address **`host:grpc.port`**. Full message definitions are as per each `.proto` file.