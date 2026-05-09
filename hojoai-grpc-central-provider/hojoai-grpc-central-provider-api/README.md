# hojoai-grpc-central-provider-api

[hojoai-grpc-central-provider](../) 的 **API 子模块**：存放 `.proto` 与由 Protobuf/gRPC 生成的 Java 类型，供服务端与客户端共同依赖。

## Maven 依赖

```xml
<dependency>
    <groupId>com.hojoai.central.provider</groupId>
    <artifactId>hojoai-grpc-central-provider-api</artifactId>
    <version><!-- 与 hojoai-grpc-central-provider 父 POM 版本一致 --></version>
</dependency>
```

## 服务发现

```properties
grpc.central.provider.service.name=hojoai-grpc-central-provider
```

客户端需根据 Nacos 等服务发现结果，使用实例 **`grpc-port`** metadata 连接 **`host:grpc-port`**（见父工程 [API.md](../API.md)）。

## Spring 中注册客户端（示例）

```java
@Configuration
public class IConfigurationForGrpc {

    @Resource
    private DiscoveryClient discoveryClient;

    @Value("${grpc.central.provider.service.name}")
    private String grpcCentralProviderServiceName;

    @Bean
    public CentralProviderChannelProvider centralProviderChannelProvider() {
        return new CentralProviderChannelProvider(discoveryClient, grpcCentralProviderServiceName);
    }

    @Bean
    public HelloWorldClient helloWorldClient() {
        return new HelloWorldClient(centralProviderChannelProvider());
    }
}
```

## 调用示例

```java
@Resource
private HelloWorldClient helloWorldClient;

HelloWorldDTO dto = helloWorldClient.sayHello(
    HelloWorldSayHelloRequest.newBuilder().setAsking("Hello").build());
```

`HelloWorldClient`、`CentralProviderChannelProvider` 等类名以实现仓库中的实际包名为准；完整 RPC 列表见 [../API.md](../API.md)。
