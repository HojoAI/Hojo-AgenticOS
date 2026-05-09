# 本地构建说明（Maven）

本仓库**无**根聚合 `pom.xml`。请在各子目录执行 `mvn clean install -DskipTests`（或 `mvn install`），将制品安装到本地 `~/.m2`。

构建**不依赖**私有 Maven 制品库（例如已移除的 `repo-ppkyi`）；依赖应来自 **Maven Central** 或**本机已由源码安装的模块**。

## 前置：`hojoai-omniasst-common`

源码位于本仓库目录 **`hojoai-omniasst-common/`**，父 POM 为 **`hojoai-parent`**，构建前须已将该父工程安装到本地 `~/.m2`。

以下模块的 `*-server` 依赖 `com.hojoai.omniasst.common:hojoai-omniasst-common`（版本以各模块 `pom.xml` 为准）：

- `hojoai-webmvc-omniasst-management`
- `hojoai-webmvc-omniasst-focus`
- `hojoai-webmvc-omniasst-translate`
- `hojoai-websocket-omniasst-sysagent`
- `hojoai-webmvc-omniasst-meeting`

若本地尚未安装该 `jar`，在**仓库根目录**执行（须先完成下文步骤 1，必要时步骤 2）：

```bash
mvn -f hojoai-omniasst-common/pom.xml clean install -DskipTests
```

确认 `~/.m2/repository/com/hojoai/omniasst/common/hojoai-omniasst-common/` 下已存在对应版本后再继续。

## 推荐安装顺序（本仓库内模块）

在仓库根目录依次执行（路径相对于仓库根）：

```bash
# 1. 父 BOM（其余模块的 parent）
mvn -f hojoai-parent/pom.xml clean install -DskipTests

# 2. 公共库
mvn -f hojoai-common/pom.xml clean install -DskipTests

# 2b. Omniasst 域公共库（多个 omniasst *-server 依赖；仅依赖 hojoai-parent）
mvn -f hojoai-omniasst-common/pom.xml clean install -DskipTests

# 3. 中台 gRPC（含 api + server）
mvn -f hojoai-grpc-central-provider/pom.xml clean install -DskipTests

# 4. 不依赖 hojoai-omniasst-common 的网关与业务（可按需裁剪）
mvn -f hojoai-gateway-omniasst/pom.xml clean install -DskipTests
mvn -f hojoai-gateway-omniasst-voice/pom.xml clean install -DskipTests
mvn -f hojoai-gateway-central/pom.xml clean install -DskipTests
mvn -f hojoai-webmvc-central-account/pom.xml clean install -DskipTests
mvn -f hojoai-webmvc-omniasst-account/pom.xml clean install -DskipTests
mvn -f hojoai-webmvc-omniasst-music/pom.xml clean install -DskipTests
mvn -f hojoai-webmvc-omniasst-weather/pom.xml clean install -DskipTests

# 5. 依赖 hojoai-omniasst-common 的模块（须已完成步骤 2b 或上文「前置」中的 install）
mvn -f hojoai-webmvc-omniasst-management/pom.xml clean install -DskipTests
mvn -f hojoai-webmvc-omniasst-translate/pom.xml clean install -DskipTests
mvn -f hojoai-webmvc-omniasst-focus/pom.xml clean install -DskipTests
mvn -f hojoai-websocket-omniasst-sysagent/pom.xml clean install -DskipTests
mvn -f hojoai-webmvc-omniasst-meeting/pom.xml clean install -DskipTests

# 6. 模板（可选）
mvn -f hojoai-template/pom.xml clean install -DskipTests
```

若某一步报错缺少依赖，请根据报错中的 `groupId:artifactId` 回退到对应模块目录先执行 `install`。

## 与 CI 的关系

仓库内 [`.github/workflows/ci.yml`](../.github/workflows/ci.yml) 对**不依赖** `hojoai-omniasst-common` 的模块做持续集成；依赖该库的模块需在本地或通过后续补充的制品源满足依赖后，方可纳入同一流水线。
