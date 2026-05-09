# 快速开始（详细版）

本地开发与运行说明。精简版 [README_zh.md](../README_zh.md#快速开始)，构建顺序 [BUILD_zh.md](BUILD_zh.md)。

<a id="quickstart-toc"></a>

## 目录

- **前置**
  - [环境要求](#quickstart-env)
  - [默认端口与部署](#quickstart-ports)
  - [安装](#quickstart-install)
- **本地基础设施（Docker）**
  - [总览](#quickstart-infra-docker)
  - [Nacos](#quickstart-nacos-docker)
  - [MySQL 8.0+](#quickstart-mysql-docker)
  - [Redis 6.0+](#quickstart-redis-docker)
  - [PostgreSQL 14+ 与 pgvector](#quickstart-postgresql-docker)
- **配置与运行**
  - [配置](#quickstart-config)
  - [Docker Compose 一键启动（推荐）](#quickstart-compose)
  - [仅验证网关（最简）](#quickstart-gateway-only)
  - [完整联调：最小本地运行拓扑（示例）](#quickstart-full-stack)
  - [运行](#quickstart-run)
  - [验证](#quickstart-verify)

---

<a id="quickstart-env"></a>

## 环境要求

- **JDK**: 21+
- **Maven**: 3.8+
- **Nacos**: 服务注册与配置中心
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **PostgreSQL**: 14+（须 pgvector 扩展）

---

<a id="quickstart-ports"></a>

## 默认端口与部署

**约定**：中台（Central）端口自 **11001** 起（`11xxx`）；Omniasst 自 **12001** 起（`12xxx`），两域不重叠。默认值见 [服务模块](../README.md#服务模块)；冲突时在对应服务 `application-dev.properties` 修改。

---

<a id="quickstart-install"></a>

## 安装

```bash
git clone https://github.com/HojoAI/Hojo-Agentic-OS.git
cd Hojo-Agentic-OS
```

- 本仓库**无**根聚合 `pom.xml`。
- **`mvn install` 顺序与依赖以 [BUILD_zh.md](BUILD_zh.md) 为准**，勿只抄片段命令。
- **勿**配置私有 Maven 仓库。

---

<a id="quickstart-infra-docker"></a>

## 本地基础设施（Docker）

以下为本机 **Docker** 示例；容器名、密码、卷、端口须与 `application-dev.properties`（及环境变量）一致。

<a id="quickstart-nacos-docker"></a>

### Nacos（Docker）

单机 **Nacos v3.x** 示例（客户端默认 **`127.0.0.1:8848`**）。端口：**8080** 控制台、**8848** 注册/配置、**9848** gRPC；单机**勿**映射 **9849**（v2 集群用法）。镜像示例 **`nacos/nacos-server:v3.2.0`**。**v3 须**设置 `NACOS_AUTH_TOKEN`、`NACOS_AUTH_IDENTITY_KEY`、`NACOS_AUTH_IDENTITY_VALUE`（下列与官方开发示例一致，仅本机试跑，上线请替换）。2.x 控制台在 **8848** 时可去掉 **8080**；arm64 可试 **`nacos/nacos-server:v3.2.0-slim`** 或 `--platform linux/amd64`。详见 [Nacos Docker 快速开始](https://nacos.io/docs/latest/quickstart/quick-start-docker/)。

1. **拉取并启动**

```bash
docker pull nacos/nacos-server:v3.2.0
docker run -d \
  --name nacos-standalone \
  --restart unless-stopped \
  -e MODE=standalone \
  -e NACOS_AUTH_TOKEN=VGhpc0lzTXlDdXN0b21TZWNyZXRLZXkwMTIzNDU2Nzg= \
  -e NACOS_AUTH_IDENTITY_KEY=serverIdentity \
  -e NACOS_AUTH_IDENTITY_VALUE=security \
  -p 8080:8080 \
  -p 8848:8848 \
  -p 9848:9848 \
  nacos/nacos-server:v3.2.0
```

2. **就绪**：首次约数十秒；`docker logs -f nacos-standalone` 出现成功日志后再开控制台。

3. **控制台**：访问 [http://127.0.0.1:8080](http://127.0.0.1:8080)。**首次启动需设置管理员密码**，默认账号密码均设为 `nacos`（与其他微服务 dev 配置一致）。后续启动无需重复此步骤。

4. **持久化**（命名卷示例）：

```bash
docker rm -f nacos-standalone 2>/dev/null
docker run -d \
  --name nacos-standalone \
  --restart unless-stopped \
  -e MODE=standalone \
  -e NACOS_AUTH_TOKEN=VGhpc0lzTXlDdXN0b21TZWNyZXRLZXkwMTIzNDU2Nzg= \
  -e NACOS_AUTH_IDENTITY_KEY=serverIdentity \
  -e NACOS_AUTH_IDENTITY_VALUE=security \
  -p 8080:8080 \
  -p 8848:8848 \
  -p 9848:9848 \
  -v ./data/nacos:/home/nacos/data \
  nacos/nacos-server:v3.2.0
```

5. **停止 / 删除**：`docker stop nacos-standalone`；`docker rm nacos-standalone`（未用命名卷则数据随容器清理）。亦可改用 [nacos.io](https://nacos.io/) 发行包本地启动。

<a id="quickstart-mysql-docker"></a>

### MySQL 8.0+（Docker）

官方 [mysql](https://hub.docker.com/_/mysql) 镜像，默认 **3306**。JDBC 变量见各模块 `application-dev.properties`。

1. **启动容器**（`root`（无密码）、`hojoai` 库可改；`8.0` 可换 **8.0+** tag）：

```bash
docker run -d \
  --name mysql-dev \
  --restart unless-stopped \
  -e MYSQL_ROOT_PASSWORD= \
  -e MYSQL_DATABASE=hojoai \
  -p 3306:3306 \
  -v mysql-dev-data:/var/lib/mysql \
  mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci
```

2. **验证**：`docker logs mysql-dev` 正常后，`docker exec -it mysql-dev mysql -uroot -p -e "SHOW DATABASES;"`。

3. **JDBC 示例**：`jdbc:mysql://127.0.0.1:3306/hojoai?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8`。**3306** 被占用时改 `-p` 与 URL 端口。

4. **停止 / 删除**：`docker stop mysql-dev`；`docker rm mysql-dev`（保留卷则数据仍在）。

<a id="quickstart-redis-docker"></a>

### Redis 6.0+（Docker）

官方 [redis](https://hub.docker.com/_/redis) 镜像，默认 **6379**（键名如 `REDIS_HOST` 等见各模块配置）。

1. **无密码**：

```bash
docker run -d \
  --name redis-dev \
  --restart unless-stopped \
  -p 6379:6379 \
  redis:7-alpine
```

2. **带密码**：

```bash
docker run -d \
  --name redis-dev \
  --restart unless-stopped \
  -p 6379:6379 \
  redis:7-alpine \
  redis-server --appendonly yes --requirepass devredis
```

3. **验证**：`docker exec -it redis-dev redis-cli ping`（有密码时先 `redis-cli -a devredis ping`）。

4. **版本 / 端口**：`7-alpine` 已满足 6.0+；需 **6.x** 可换 `redis:6.2-alpine` 等。端口冲突改 `-p` 并在配置里写宿主机端口。

5. **停止 / 删除**：`docker stop redis-dev`；`docker rm redis-dev`。

<a id="quickstart-postgresql-docker"></a>

### PostgreSQL 14+ 与 pgvector（Docker）

[**pgvector/pgvector**](https://github.com/pgvector/pgvector) 镜像（PostgreSQL + pgvector），映射 **5432**。Omniasst：**库与用户均为 `omniasst`**；连接见 `OMNIASST_POSTGRESQL_*`。建表见 [`omniasst.sql`](../db/schema/postgresql/omniasst.sql) → [初始化库表](#quickstart-db-init)。

1. **启动容器**（示例 **pg16**，可换 `pg15` / `pg14`）。`POSTGRES_USER` 与 `POSTGRES_DB` 均为 **`omniasst`** 时首次启动会创建用户与库：

```bash
docker run -d \
  --name postgres-pgvector \
  --restart unless-stopped \
  -e POSTGRES_USER=omniasst \
  -e POSTGRES_PASSWORD=devpass \
  -e POSTGRES_DB=omniasst \
  -p 5432:5432 \
  -v postgres-pgvector-data:/var/lib/postgresql/data \
  pgvector/pgvector:pg16
```

2. **JDBC**：`jdbc:postgresql://127.0.0.1:5432/omniasst`（用户 **`omniasst`**，密码同 `POSTGRES_PASSWORD` / `OMNIASST_POSTGRESQL_DATASOURCE_PASSWORD`）。**5432** 被占用时同步改 `-p` 与 URL。

3. **停止 / 删除**：`docker stop postgres-pgvector`；`docker rm postgres-pgvector`（数据是否保留看命名卷）。

---

<a id="quickstart-config"></a>

## 配置

1. 启动 **Nacos**（[上文 Docker](#quickstart-nacos-docker) 或 [nacos.io](https://nacos.io/) 发行包；控制台地址依版本）。
2. 修改各服务 `config/application-dev.properties`。
3. 配置 **MySQL** / **Redis** / **PostgreSQL**（环境与 Nacos；示例见 [Docker](#quickstart-infra-docker)；键名见各服务 `application-dev.properties`）。

<a id="quickstart-db-init"></a>

4. **初始化库表**（仓库根目录执行 [`db/schema`](../db/schema)）：MySQL → `hojoai-central-account` / `hojoai-omniasst`；PostgreSQL → 用户与库 **`omniasst`**（须已存在；脚本不 `CREATE DATABASE`）。参数按本机改。

   **MySQL（Docker：`mysql-dev`）**

   ```bash
   docker exec -i mysql-dev mysql -uroot -p -e \
     'CREATE DATABASE IF NOT EXISTS `hojoai-central-account` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
      CREATE DATABASE IF NOT EXISTS `hojoai-omniasst` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;'
   docker exec -i mysql-dev mysql -uroot -p hojoai-central-account < db/schema/mysql/hojoai-central-account.sql
   docker exec -i mysql-dev mysql -uroot -p hojoai-omniasst < db/schema/mysql/hojoai-omniasst.sql
   ```

   **MySQL（本机）** — `-p` 与密码无空格；示例同 [上文 MySQL](#quickstart-mysql-docker)。

   ```bash
   mysql -h127.0.0.1 -P3306 -uroot -p -e \
     'CREATE DATABASE IF NOT EXISTS `hojoai-central-account` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
      CREATE DATABASE IF NOT EXISTS `hojoai-omniasst` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;'
   mysql -h127.0.0.1 -P3306 -uroot -p hojoai-central-account < db/schema/mysql/hojoai-central-account.sql
   mysql -h127.0.0.1 -P3306 -uroot -p hojoai-omniasst < db/schema/mysql/hojoai-omniasst.sql
   ```

   **PostgreSQL（Docker）**

   ```bash
   docker exec -i postgres-pgvector psql -U omniasst -d omniasst < db/schema/postgresql/omniasst.sql
   ```

   **PostgreSQL（本机）** — 缺用户或库时先执行：

   ```bash
   psql -U postgres -d postgres -c "CREATE ROLE omniasst WITH LOGIN PASSWORD 'devpass' SUPERUSER;"
   psql -U postgres -d postgres -c "CREATE DATABASE omniasst OWNER omniasst;"
   export PGHOST=127.0.0.1 PGPORT=5432 PGUSER=omniasst PGPASSWORD=devpass PGDATABASE=omniasst
   psql -f db/schema/postgresql/omniasst.sql
   ```

5. 配置第三方（语音、翻译、LLM 等）密钥与 URL。

<a id="quickstart-compose"></a>

## Docker Compose 一键启动（推荐）

项目根目录提供了 `docker-compose.yml`，可一键启动**所有基础设施 + 所有微服务**。

### 前置要求

- **Docker** 20.10+
- **Docker Compose** 2.0+
- **数据库初始化**：首次启动时，会自动创建 `hojoai-central-account` 和 `hojoai-omniasst` 数据库，并执行初始化脚本。PostgreSQL 的 `omniasst` 数据库和用户也需已存在（容器初始化时会自动创建）。
- **数据持久化目录** `data/`（MySQL、Redis、PostgreSQL、Nacos）已加入 `.gitignore`，不会提交到代码仓库

### 快速启动

```bash
# 1. 初始化环境配置（生成 .env 文件）
./setup.sh

# 2. 编辑 .env 配置必要的环境变量（至少配置基础设施地址）

# 3. 构建所有服务的 jar 包
make build

# 4. 启动所有服务（需先在 Nacos 控制台完成初始化）
docker compose up -d

# 5. 查看日志
docker compose logs -f

# 6. 停止
docker compose down
```

### 环境变量配置

首次运行需执行 `./setup.sh` 生成 `.env` 文件，模板见 [`.env.example`](../.env.example)。主要配置项：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `NACOS_SERVER_ADDR` | `nacos:8848` | Nacos 地址 |
| `NACOS_CONSOLE_HOST_PORT` | `8080` | Nacos 控制台映射到本机的端口（与 `docker-compose` 中 nacos 服务一致） |
| `MYSQL_HOST` | `mysql` | MySQL 地址 |
| `REDIS_HOST` | `redis` | Redis 地址 |
| `POSTGRES_HOST` | `postgres` | PostgreSQL 地址 |

第三方服务密钥（语音、翻译、LLM 等）按需填写，文件中已有注释说明。

### 注意事项

- **Nacos 首次启动需初始化**：按 [Nacos Docker 文档](https://nacos.io/docs/latest/quickstart/quick-start-docker/)，**3.x 控制台在独立端口（映射到本机默认 8080）**，请用 **`http://127.0.0.1:8080/`**（根路径，**不要**手动加 `/nacos`、`/index.html`）。若你已在 `.env` 中设置 `NACOS_CONSOLE_HOST_PORT`，则把端口换成该值。优先使用 **`127.0.0.1`** 而非 `localhost`，避免本机解析到非 Docker 的监听。若仍出现 `No static resource …`：① 执行 `lsof -nP -iTCP:8080 -sTCP:LISTEN` 确认只有 Docker 在监听；② 备份后清空 **`./data/nacos`** 再 `docker compose up -d nacos`（排除旧版本残留数据）；③ 将 `NACOS_CONSOLE_HOST_PORT=18080` 后重启，再访问 **`http://127.0.0.1:18080/`**。打开后按提示设置管理员密码；与 dev 配置一致时可使用账号密码均为 `nacos`。
- **首次构建较慢**：每个服务需要执行 `mvn package`，视机器配置可能需要 10-30 分钟
- **数据持久化**：MySQL、Redis、PostgreSQL、Nacos 数据目录已挂载到 `data/` 目录，删除容器后数据不会丢失
- **端口占用**：确保本地 11001-12010 端口未被占用

### 服务列表

| 服务 | 端口 | 说明 |
|------|------|------|
| **基础设施** | | |
| nacos | 8848 / **控制台见上** | 客户端注册与配置 **8848**；控制台默认映射本机 **8080**（`NACOS_CONSOLE_HOST_PORT`） |
| mysql | 3306 | MySQL 8.0 |
| redis | 6379 | Redis 7 |
| postgres | 5432 | PostgreSQL 16 + pgvector |
| **中台服务** | | |
| central-account | 11002 | 中台账户服务 |
| grpc-central-provider | 11003/11103 | gRPC 服务（双端口） |
| central-gateway | 11001 | 中台网关 |
| **Omniasst 业务服务** | | |
| omniasst-account | 12004 | 业务账户服务 |
| management | 12005 | 会话管理与编排支持 |
| sysagent | 12003 | Agent 编排中枢（WebSocket） |
| translate | 12006 | 翻译场景 Agent |
| meeting | 12007 | 会议场景 Agent |
| focus | 12008 | 日程场景 Agent |
| music | 12009 | 音乐场景 Agent |
| weather | 12010 | 天气场景 Agent |
| gateway-omniasst | 12001 | 业务网关（HTTP + WebSocket） |
| gateway-omniasst-voice | 12002 | 语音网关 |

### 验证

```bash
# 查看所有容器状态
docker compose ps

# 健康检查（端口以实际为准）
curl http://localhost:12001/actuator/health   # 业务网关
curl http://localhost:11001/actuator/health   # 中台网关
curl -fI "http://127.0.0.1:8080/"              # 控制台根路径（未改 NACOS_CONSOLE_HOST_PORT 时）
curl -f "http://127.0.0.1:8848/nacos"          # 8848 核心端口（与镜像健康检查一致）
```

---

<a id="quickstart-gateway-only"></a>

## 仅验证网关（最简）

1. 启动 **Nacos**（仅注册发现时可暂不启 MySQL/Redis；以 **dev** 配置为准）。
2. 按 [BUILD_zh.md](BUILD_zh.md) 安装 `hojoai-gateway-omniasst-server` 依赖模块。
3. 端口冲突见 [默认端口与部署](#quickstart-ports)。

```bash
cd hojoai-gateway-omniasst/hojoai-gateway-omniasst-server
mvn spring-boot:run
```

健康检查（网关默认 **12001**）：

```bash
curl http://localhost:12001/actuator/health
```

---

<a id="quickstart-full-stack"></a>

## 完整联调：最小本地运行拓扑（示例）

经 Omniasst 网关联调的参考顺序（无根目录 docker-compose；基础设施可自建或用 [Docker 示例](#quickstart-infra-docker)）：

1. 启动 **Nacos、MySQL、Redis、PostgreSQL**，填好各服务 `application-dev.properties`（及环境变量）。
2. 多服务同机时先按 [默认端口与部署](#quickstart-ports) 错开端口。
3. **启动先后**（按依赖裁剪，非固定一条链）：
   - **中台账户**：需登录/鉴权、调中台或网关访问 `central.account.base.url` 时，先起 **`hojoai-webmvc-central-account`**。`hojoai-gateway-central` 仅路由用，客户端可不经过；本地也可直连账户端口。
   - **Omniasst 下游**：网关 `lb://...` 指向的服务须先 **Nacos** 注册（如 management 调 voice，需 **`hojoai-gateway-omniasst-voice`** 已注册）。
   - **Omniasst 网关**：**`hojoai-gateway-omniasst`** 可较后启；下游未起则对应路由失败。
4. 路由与接口以 [API.md](../hojoai-gateway-omniasst/API.md) 与各服务配置为准；Maven 依赖见 [服务依赖关系](../README.md#服务依赖关系)。

---

<a id="quickstart-run"></a>

## 运行

1. 按 [BUILD_zh.md](BUILD_zh.md) 对要跑的模块做本地 `install`。
2. **Nacos、MySQL、Redis、PostgreSQL** 就绪；端口见 [默认端口与部署](#quickstart-ports)。
3. 在目标 **`*-server`** 目录执行 `mvn spring-boot:run`（或 IDE 跑主类）。示例（Omniasst 网关，端口见 [服务模块](../README.md#服务模块)）：

```bash
cd hojoai-gateway-omniasst/hojoai-gateway-omniasst-server
mvn spring-boot:run
```

其它模块同理进入对应 `*-server` 启动。

---

<a id="quickstart-verify"></a>

## 验证

健康检查（端口以实际为准，网关示例 **12001**）：

```bash
curl http://localhost:12001/actuator/health
```
