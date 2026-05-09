# hojoai-parent

HoJoAI Java 工程的 **根 BOM（Bill of Materials）**：继承 `spring-boot-starter-parent`，统一锁定 Spring Cloud、gRPC、常用三方库与本仓库 `hojoai-common` 等版本。

## 职责

- `dependencyManagement`：导入 Spring Cloud BOM、gRPC BOM、Protobuf BOM 及项目内统一版本属性。
- 约定 **Java 21**、**UTF-8** 编码、编译与源码插件版本等。

## 使用方式

业务模块在 `pom.xml` 中以此为 `<parent>`，无需重复声明已由本 BOM 管理的依赖版本：

```xml
<parent>
    <groupId>com.hojoai.parent</groupId>
    <artifactId>hojoai-parent</artifactId>
    <version>3.25.10</version>
</parent>
```

具体版本号以当前 `pom.xml` 为准。

## 说明

- 本工程 **`packaging` 为 `pom`**，不可单独 `spring-boot:run`。
- 版本与依赖矩阵的权威来源为本目录下的 `pom.xml`。
