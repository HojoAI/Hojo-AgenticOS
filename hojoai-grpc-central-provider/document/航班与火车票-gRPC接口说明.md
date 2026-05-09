# 航班与火车票 gRPC 接口说明

本文档面向**调用方**，说明如何通过 gRPC 调用 central-provider 提供的航班、火车票查询能力。数据来源为飞友科技开放平台，认证与 HTTP 调用由本服务内部完成，调用方仅需依赖 api 模块并调用对应 Client 即可。

---

## 一、概述

| 能力 | 服务 / 包 | 接口数量 | 说明 |
|------|------------|----------|------|
| 航班 | `FlightService`（proto 包 `flight`） | 1 个 | 按航班号 + 日期查询航班动态 |
| 火车票 | `TrainService`（proto 包 `train`） | 4 个 | 三种查询方式 + 统一入口 |

- **Proto / 生成类包名**：`com.hojoai.central.provider.grpc`
- **Client 包名**：航班 `com.hojoai.central.provider.service.flight.FlightClient`，火车票 `com.hojoai.central.provider.service.train.TrainClient`
- 调用方需依赖 **hojoai-grpc-central-provider-api**，并具备可访问本 provider 的 **CentralProviderChannelProvider**（或等价 gRPC 通道管理）。

---

## 二、航班接口

### 2.1 接口定义

- **RPC**：`getFlightByNumberAndDate`
- **含义**：按航班号 + 日期查询航班动态（飞友「按航班号+日期方式查询」）

### 2.2 请求

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| fnum | string | 是 | 航班号，如 `SC7621`、`GX8915` |
| date | string | 是 | 航班日期，格式 `yyyy-MM-dd`，如 `2023-12-22` |

### 2.3 响应（GetFlightByNumberAndDateResponse）

根级字段均为 string，按需取用；未返回时为空字符串。

| 字段 | 说明 |
|------|------|
| flight_no | 航班号 |
| flight_company | 航空公司名称 |
| flight_depcode / flight_arrcode | 出发/目的地机场三字码 |
| flight_dep / flight_arr | 出发/到达城市名 |
| flight_dep_airport / flight_arr_airport | 出发/到达机场名 |
| flight_deptime_plan_date / flight_arrtime_plan_date | 计划起飞/到达时间 |
| flight_deptime_ready_date / flight_arrtime_ready_date | 预计起飞/到达时间 |
| flight_deptime_date / flight_arrtime_date | 实际起飞/到达时间 |
| flight_state | 航班状态，如「计划」 |
| board_gate / reach_exit / baggage_id | 登机口、到达口、行李转盘 |
| board_state | 乘机状态，如「开始值机」 |
| flight_h_terminal / flight_terminal | 出发/到达航站楼 |
| fcategory / ftype / fservice | 航班属性、机型、服务类型 |
| aircraft_number / stop_flag / share_flight_no | 飞机编号、经停次数、共享航班号 |
| dst_timezone / org_timezone | 目的地/出发地时区 |
| fill_flight_info | 补班航班信息（嵌套对象，含补班航班号、起降站、时间等） |

### 2.4 调用示例（Java）

```java
@Resource
private FlightClient flightClient;

GetFlightByNumberAndDateRequest request = GetFlightByNumberAndDateRequest.newBuilder()
    .setFnum("SC7621")
    .setDate("2023-12-22")
    .build();

GetFlightByNumberAndDateResponse response = flightClient.getFlightByNumberAndDate(request);

String flightNo = response.getFlightNo();
String flightState = response.getFlightState();
String depTime = response.getFlightDeptimeDate();
if (response.hasFillFlightInfo()) {
    FillFlightInfo fill = response.getFillFlightInfo();
    // 补班航班号、起降时间等
}
```

---

## 三、火车票接口

火车票共 **4 个 gRPC 接口**：3 种固定查询方式 + 1 个统一入口（根据参数自动选择方式）。

### 3.1 方式一：车次 + 出发站 + 到达站 + 日期

- **RPC**：`getByTrainNumberAndStations`
- **请求**：`TrainNumberStationsRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| tnum | string | 是 | 车次，如 `G103` |
| tdep | string | 是 | 出发站中文名，如 `北京南` |
| tarr | string | 是 | 到达站中文名，如 `上海虹桥` |
| date | string | 是 | 出发日期，`yyyy-MM-dd`，如 `2023-12-23` |

### 3.2 方式二：出发站 + 到达站 + 日期

- **RPC**：`getByStationsAndDate`
- **请求**：`StationsDateRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| tdep | string | 是 | 出发站中文名 |
| tarr | string | 是 | 到达站中文名 |
| date | string | 是 | 出发日期，`yyyy-MM-dd` |

不指定车次，返回该区段当日车次列表。

### 3.3 方式三：出发城市 + 到达城市 + 日期

- **RPC**：`getByCitiesAndDate`
- **请求**：`CitiesDateRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cdep | string | 是 | 出发城市中文名，如 `合肥` |
| carr | string | 是 | 到达城市中文名，如 `六安` |
| date | string | 是 | 出发日期，`yyyy-MM-dd` |

### 3.4 统一接口（自动选择方式）

- **RPC**：`getTrainTable`
- **请求**：`TrainTableUnifiedRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| date | string | 是 | 出发日期，`yyyy-MM-dd` |
| tnum | string | 否 | 车次（与 tdep、tarr 一起时走方式一） |
| tdep | string | 否 | 出发站中文名 |
| tarr | string | 否 | 到达站中文名 |
| cdep | string | 否 | 出发城市中文名 |
| carr | string | 否 | 到达城市中文名 |

**选择规则**（按优先级）：

1. 若 **tnum、tdep、tarr、date** 均非空 → 按**方式一**查询；
2. 否则若 **tdep、tarr、date** 均非空 → 按**方式二**查询；
3. 否则若 **cdep、carr、date** 均非空 → 按**方式三**查询；
4. 否则返回 `INVALID_ARGUMENT`（参数不满足任一种方式）。

### 3.5 火车票统一响应（TrainTableResponse）

四个接口的响应结构相同。

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int32 | 状态码，0 表示成功 |
| msg | string | 描述，如 `success` |
| data | TrainTableData | 见下表 |

**TrainTableData**

| 字段 | 类型 | 说明 |
|------|------|------|
| count | int32 | 车次数量 |
| trainlist | repeated TrainListItem | 车次列表 |

**TrainListItem**

| 字段 | 类型 | 说明 |
|------|------|------|
| trainnum | string | 车次，如 `G103` |
| depstan | string | 出发站中文名 |
| arrstan | string | 到达站中文名 |
| depstancode | string | 出发站三字码 |
| arrstancode | string | 到达站三字码 |
| plandeptime | string | 计划出发时间，如 `2023-12-23 06:20:00` |
| planarrtime | string | 计划到达时间 |
| duration | int32 | 运行时长（分钟） |

### 3.6 调用示例（Java）

```java
@Resource
private TrainClient trainClient;

// 方式一：车次 + 出发站 + 到达站 + 日期
TrainTableResponse r1 = trainClient.getByTrainNumberAndStations(
    TrainNumberStationsRequest.newBuilder()
        .setTnum("G103")
        .setTdep("北京南")
        .setTarr("上海虹桥")
        .setDate("2023-12-23")
        .build());

// 方式二：出发站 + 到达站 + 日期
TrainTableResponse r2 = trainClient.getByStationsAndDate(
    StationsDateRequest.newBuilder()
        .setTdep("北京南")
        .setTarr("上海虹桥")
        .setDate("2023-12-23")
        .build());

// 方式三：出发城市 + 到达城市 + 日期
TrainTableResponse r3 = trainClient.getByCitiesAndDate(
    CitiesDateRequest.newBuilder()
        .setCdep("合肥")
        .setCarr("六安")
        .setDate("2024-07-06")
        .build());

// 统一接口：只填 tdep + tarr + date，内部按方式二查询
TrainTableResponse r4 = trainClient.getTrainTable(
    TrainTableUnifiedRequest.newBuilder()
        .setTdep("北京南")
        .setTarr("上海虹桥")
        .setDate("2023-12-23")
        .build());

// 使用响应
int code = r1.getCode();
String msg = r1.getMsg();
for (TrainListItem t : r1.getData().getTrainlistList()) {
    String trainnum = t.getTrainnum();
    String plandeptime = t.getPlandeptime();
    int duration = t.getDuration();
}
```

---

## 四、依赖与 Client 使用

- 调用方工程需依赖 **hojoai-grpc-central-provider-api**（含 proto 生成类及 FlightClient、TrainClient）。
- 使用前需具备可访问本 central-provider 的 gRPC 通道（如通过 Nacos 发现或固定地址），并注入或构造 **CentralProviderChannelProvider**，再注入 **FlightClient** / **TrainClient**（构造函数传入 CentralProviderChannelProvider）。
- 航班、火车票的飞友 appId/appSecurity 与请求地址由 central-provider 配置，调用方**无需**配置任何飞友参数。

---

## 五、错误说明

| gRPC 状态 | 含义 |
|-----------|------|
| OK | 调用成功，按上述响应结构解析即可。 |
| INVALID_ARGUMENT | 请求参数不合法（如必填为空、火车票统一接口不满足任一种查询方式）。描述信息中会给出简要说明。 |
| INTERNAL | 服务内部异常（如飞友接口报错、网络异常）。描述信息可能包含飞友返回的错误说明。 |

调用方可根据 Status 与 description 做重试、降级或提示用户。
