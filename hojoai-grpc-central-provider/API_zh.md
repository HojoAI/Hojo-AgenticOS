# hojoai-grpc-central-provider — API 文档

## 概述

本服务对外提供 **gRPC** 接口（另有 HTTP 端口 `server.port` 用于 Spring Boot 管理/健康检查等，默认如 `11003`）。  
gRPC 监听端口见各环境 `config/application-*.properties` 中的 **`grpc.port`**（如 `11103`），并可通过 Nacos metadata `grpc-port` 发现。

## 服务与 RPC 一览

Proto 位于 `hojoai-grpc-central-provider-api/src/main/proto/`。

### `hello.HelloWorldService`（包名 `hello`）

| RPC | 请求 | 响应 |
|-----|------|------|
| `sayHello` | `HelloWorldSayHelloRequest` | `HelloWorldDTO` |

Java 包：`com.hojoai.central.provider.service.hello`。

### `order.OrderService`

| RPC | 说明 |
|-----|------|
| `createOrder` | 创建订单 |
| `getOrderDetail` | 订单详情 |
| `alipayNotify` | 支付宝回调 |
| `panheNotify` | 磐河回调 |

### `movie.MovieService`

| RPC | 说明 |
|-----|------|
| `getFilmList` | 热门/待映影片列表 |
| `getScheduleCinemaList` | 影片排期影院 |
| `getFilmShowList` | 场次 |
| `getShowSeatList` | 座位 |
| `getCinemaList` | 城市影院列表 |
| `getCinemaFilmList` | 影院详情与影片 |
| `getFilmScheduleList` | 影片信息与近期排期 |

### `flight.FlightService`

| RPC | 说明 |
|-----|------|
| `getFlightByNumberAndDate` | 按航班号与日期查询 |

### `train.TrainService`

| RPC | 说明 |
|-----|------|
| `getByTrainNumberAndStations` | 车次+发到站 |
| `getByStationsAndDate` | 发到站+日期 |
| `getByCitiesAndDate` | 城市+日期 |
| `getTrainTable` | 统一查询入口 |

### `push.PushService`

| RPC | 说明 |
|-----|------|
| `push` | 推送（请求体为 JSON 字符串字段 `pushReqStr`） |

## 客户端调用

使用对应 `.proto` 生成的 stub，目标地址为 **`host:grpc.port`**。完整 message 定义以各 `.proto` 文件为准。
