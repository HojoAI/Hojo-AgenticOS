# Quick Start (Detailed Version)

Local development and runtime notes. Concise version: [README.md](../README.md#quick-start). Build order: [BUILD.md](BUILD.md).

<a id="quickstart-toc"></a>

## Table of Contents

- **Prerequisites**
  - [Environment Requirements](#quickstart-env)
  - [Default Ports and Deployment](#quickstart-ports)
  - [Installation](#quickstart-install)
- **Local Infrastructure (Docker)**
  - [Overview](#quickstart-infra-docker)
  - [Nacos](#quickstart-nacos-docker)
  - [MySQL 8.0+](#quickstart-mysql-docker)
  - [Redis 6.0+](#quickstart-redis-docker)
  - [PostgreSQL 14+ and pgvector](#quickstart-postgresql-docker)
- **Configuration and Runtime**
  - [Configuration](#quickstart-config)
  - [Docker Compose One-Click Start (Recommended)](#quickstart-compose)
  - [Gateway-Only Verification (Minimal)](#quickstart-gateway-only)
  - [Full Integration: Minimal Local Runtime Topology (Example)](#quickstart-full-stack)
  - [Running](#quickstart-run)
  - [Verification](#quickstart-verify)

---

<a id="quickstart-env"></a>

## Environment Requirements

- **JDK**: 21+
- **Maven**: 3.8+
- **Nacos**: Service registration and configuration center
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **PostgreSQL**: 14+ (pgvector extension required)

---

<a id="quickstart-ports"></a>

## Default Ports and Deployment

**Convention**: Central ports start at **11001** (`11xxx`); Omniasst at **12001** (`12xxx`); the two domains do not overlap. Defaults are in the [Services](../README.md#services) table; change conflicting ports in each service `application-dev.properties`.

---

<a id="quickstart-install"></a>

## Installation

```bash
git clone https://github.com/HojoAI/Hojo-Agentic-OS.git
cd Hojo-Agentic-OS
```

- This repository has **no** root aggregate `pom.xml`.
- **`mvn install` order and dependencies follow [BUILD.md](BUILD.md)** — do not copy only fragment commands.
- Do **NOT** configure private Maven artifact repositories.

---

<a id="quickstart-infra-docker"></a>

## Local Infrastructure (Docker)

Local **Docker** examples below; container names, passwords, volumes, and ports must match `application-dev.properties` (and environment variables).

<a id="quickstart-nacos-docker"></a>

### Nacos (Docker)

**Nacos v3.x** standalone (clients default **`127.0.0.1:8848`**). Ports: **8080** console, **8848** registry/config, **9848** gRPC; do **not** map **9849** on standalone (v2 cluster pattern). Example image **`nacos/nacos-server:v3.2.0`**. **v3** requires `NACOS_AUTH_TOKEN`, `NACOS_AUTH_IDENTITY_KEY`, `NACOS_AUTH_IDENTITY_VALUE` (values below match official dev samples — **local trial only**; replace for production). For 2.x with console on **8848**, drop **8080**; on arm64 try **`nacos/nacos-server:v3.2.0-slim`** or `--platform linux/amd64`. See [Nacos Docker quick start](https://nacos.io/docs/latest/quickstart/quick-start-docker/).

1. **Pull and start**

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

2. **Ready**: first boot can take tens of seconds; `docker logs -f nacos-standalone` until success logs appear, then open the console.

3. **Console**: access [http://127.0.0.1:8080](http://127.0.0.1:8080). **First boot requires setting an admin password** — set both username and password to `nacos` (consistent with other services' dev config). Subsequent restarts skip this step.

4. **Persistence** (named volume example):

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

5. **Stop / remove**: `docker stop nacos-standalone`; `docker rm nacos-standalone` (without a named volume, data is discarded). Or run a package from [nacos.io](https://nacos.io/) locally.

<a id="quickstart-mysql-docker"></a>

### MySQL 8.0+ (Docker)

Official [mysql](https://hub.docker.com/_/mysql) image, default **3306**. JDBC variable names are in each module `application-dev.properties`.

1. **Start the container** (`root` (no password), `hojoai` DB, and `8.0` tag can be changed):

```bash
docker run -d \
  --name mysql-dev \
  --restart unless-stopped \
  -e MYSQL_ROOT_PASSWORD= \
  -e MYSQL_DATABASE=hojoai \
  -p 3306:3306 \
  -v ./data/mysql:/var/lib/mysql \
  mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci
```

2. **Verify**: when `docker logs mysql-dev` looks healthy, `docker exec -it mysql-dev mysql -uroot -p -e "SHOW DATABASES;"`.

3. **JDBC example**: `jdbc:mysql://127.0.0.1:3306/hojoai?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8`. If **3306** is taken, change `-p` and the URL port.

4. **Stop / remove**: `docker stop mysql-dev`; `docker rm mysql-dev` (data remains if the volume is kept).

<a id="quickstart-redis-docker"></a>

### Redis 6.0+ (Docker)

Official [redis](https://hub.docker.com/_/redis) image, default **6379** (keys such as `REDIS_HOST` per module).

1. **No password**:

```bash
docker run -d \
  --name redis-dev \
  --restart unless-stopped \
  -p 6379:6379 \
  redis:7-alpine
```

2. **With password**:

```bash
docker run -d \
  --name redis-dev \
  --restart unless-stopped \
  -p 6379:6379 \
  redis:7-alpine \
  redis-server --appendonly yes --requirepass devredis
```

3. **Verify**: `docker exec -it redis-dev redis-cli ping` (with password: `redis-cli -a devredis ping`).

4. **Version / ports**: `7-alpine` satisfies 6.0+; for **6.x** use tags like `redis:6.2-alpine`. On port conflict, change `-p` and set the host port in config.

5. **Stop / remove**: `docker stop redis-dev`; `docker rm redis-dev`.

<a id="quickstart-postgresql-docker"></a>

### PostgreSQL 14+ and pgvector (Docker)

[**pgvector/pgvector**](https://github.com/pgvector/pgvector) image (PostgreSQL + pgvector), map **5432**. Omniasst: **database and user `omniasst`**; connection via `OMNIASST_POSTGRESQL_*`. Tables: [`omniasst.sql`](../db/schema/postgresql/omniasst.sql) → [Initialize schema](#quickstart-db-init).

1. **Start the container** (example **pg16**; use `pg15` / `pg14`). With `POSTGRES_USER` and `POSTGRES_DB` **`omniasst`**, first boot creates user and database:

```bash
docker run -d \
  --name postgres-pgvector \
  --restart unless-stopped \
  -e POSTGRES_USER=omniasst \
  -e POSTGRES_PASSWORD=devpass \
  -e POSTGRES_DB=omniasst \
  -p 5432:5432 \
  -v ./data/postgres:/var/lib/postgresql/data \
  pgvector/pgvector:pg16
```

2. **JDBC**: `jdbc:postgresql://127.0.0.1:5432/omniasst` (user **`omniasst`**, password same as `POSTGRES_PASSWORD` / `OMNIASST_POSTGRESQL_DATASOURCE_PASSWORD`). If **5432** is taken, change `-p` and the URL together.

3. **Stop / remove**: `docker stop postgres-pgvector`; `docker rm postgres-pgvector` (data retention depends on the named volume).

---

<a id="quickstart-config"></a>

## Configuration

1. Start **Nacos** ([Docker above](#quickstart-nacos-docker) or a [nacos.io](https://nacos.io/) package; console URL depends on version).
2. Edit each service `config/application-dev.properties`.
3. Configure **MySQL** / **Redis** / **PostgreSQL** (environment and Nacos; examples in [Docker](#quickstart-infra-docker); keys per service `application-dev.properties`).

<a id="quickstart-db-init"></a>

4. **Initialize schema** (from repo root, run [`db/schema`](../db/schema)): MySQL → `hojoai-central-account` / `hojoai-omniasst`; PostgreSQL → user and database **`omniasst`** (must exist; scripts do not `CREATE DATABASE`). Tune host, port, and passwords locally.

   **MySQL (Docker: `mysql-dev`)**

   ```bash
   docker exec -i mysql-dev mysql -uroot -p -e \
     'CREATE DATABASE IF NOT EXISTS `hojoai-central-account` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
      CREATE DATABASE IF NOT EXISTS `hojoai-omniasst` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;'
   docker exec -i mysql-dev mysql -uroot -p hojoai-central-account < db/schema/mysql/hojoai-central-account.sql
   docker exec -i mysql-dev mysql -uroot -p hojoai-omniasst < db/schema/mysql/hojoai-omniasst.sql
   ```

   **MySQL (host)** — no space after `-p`; sample matches [MySQL Docker](#quickstart-mysql-docker).

   ```bash
   mysql -h127.0.0.1 -P3306 -uroot -p -e \
     'CREATE DATABASE IF NOT EXISTS `hojoai-central-account` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
      CREATE DATABASE IF NOT EXISTS `hojoai-omniasst` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;'
   mysql -h127.0.0.1 -P3306 -uroot -p hojoai-central-account < db/schema/mysql/hojoai-central-account.sql
   mysql -h127.0.0.1 -P3306 -uroot -p hojoai-omniasst < db/schema/mysql/hojoai-omniasst.sql
   ```

   **PostgreSQL (Docker)**

   ```bash
   docker exec -i postgres-pgvector psql -U omniasst -d omniasst < db/schema/postgresql/omniasst.sql
   ```

   **PostgreSQL (host)** — if user or database is missing, run first:

   ```bash
   psql -U postgres -d postgres -c "CREATE ROLE omniasst WITH LOGIN PASSWORD 'devpass' SUPERUSER;"
   psql -U postgres -d postgres -c "CREATE DATABASE omniasst OWNER omniasst;"
   export PGHOST=127.0.0.1 PGPORT=5432 PGUSER=omniasst PGPASSWORD=devpass PGDATABASE=omniasst
   psql -f db/schema/postgresql/omniasst.sql
   ```

5. Configure third-party keys and URLs (voice, translation, LLM, etc.).

<a id="quickstart-compose"></a>

## Docker Compose One-Click Start (Recommended)

The project root provides `docker-compose.yml` to start **all infrastructure + all microservices** in one command.

### Prerequisites

- **Docker** 20.10+
- **Docker Compose** 2.0+
- **Database init**: first boot auto-creates `hojoai-central-account` and `hojoai-omniasst` databases and runs init scripts. PostgreSQL `omniasst` database and user must exist (container init creates them).
- **Data persistence**: `data/` directory (MySQL, Redis, PostgreSQL, Nacos) is in `.gitignore` and won't be committed.

### Quick Start

```bash
# 1. Initialize environment config (generates .env file)
./setup.sh

# 2. Edit .env to configure required environment variables

# 3. Build all service jars
make build

# 4. Start all services (Nacos console init required first)
docker compose up -d

# 5. View logs
docker compose logs -f

# 6. Stop
docker compose down
```

### Environment Variables

First run requires `./setup.sh` to generate the `.env` file from [`.env.example`](../.env.example). Key config items:

| Config | Default | Description |
|--------|---------|-------------|
| `NACOS_SERVER_ADDR` | `nacos:8848` | Nacos address |
| `NACOS_CONSOLE_HOST_PORT` | `8080` | Host port mapped to the Nacos console (must match `docker-compose` nacos service) |
| `MYSQL_HOST` | `mysql` | MySQL address |
| `REDIS_HOST` | `redis` | Redis address |
| `POSTGRES_HOST` | `postgres` | PostgreSQL address |

Third-party keys (voice, translation, LLM, etc.) fill in as needed, with comments in the file.

### Notes

- **Nacos first boot requires init**: per [Nacos Docker docs](https://nacos.io/docs/latest/quickstart/quick-start-docker/), the **3.x console** listens on the mapped host port (default **8080**) at **`http://127.0.0.1:8080/`** (root — **do not** add `/nacos` or `/index.html`). If you set `NACOS_CONSOLE_HOST_PORT` in `.env`, use that port. Prefer **`127.0.0.1`** over `localhost`. If you still see `No static resource …`: (1) `lsof -nP -iTCP:8080 -sTCP:LISTEN` to ensure Docker owns the port; (2) back up and clear **`./data/nacos`**, then `docker compose up -d nacos`; (3) set `NACOS_CONSOLE_HOST_PORT=18080`, restart, open **`http://127.0.0.1:18080/`**. Then set the admin password; for local dev aligned with defaults, username and password can both be `nacos`.
- **First build is slow**: each service runs `mvn package`, may take 10-30 minutes depending on hardware
- **Data persistence**: MySQL, Redis, PostgreSQL, Nacos data dirs mounted to `data/` — deleting containers won't lose data
- **Port conflicts**: ensure local ports 11001-12010 are available

### Service List

| Service | Port | Description |
|---------|------|-------------|
| **Infrastructure** | | |
| nacos | 8848 / **console see above** | Client registry/config **8848**; console default host **8080** (`NACOS_CONSOLE_HOST_PORT`) |
| mysql | 3306 | MySQL 8.0 |
| redis | 6379 | Redis 7 |
| postgres | 5432 | PostgreSQL 16 + pgvector |
| **Central Services** | | |
| central-account | 11002 | Central account service |
| grpc-central-provider | 11003/11103 | gRPC service (dual ports) |
| central-gateway | 11001 | Central gateway |
| **Omniasst Business Services** | | |
| omniasst-account | 12004 | Business account service |
| management | 12005 | Session management and orchestration |
| sysagent | 12003 | Agent orchestration hub (WebSocket) |
| translate | 12006 | Translation scenario Agent |
| meeting | 12007 | Meeting scenario Agent |
| focus | 12008 | Schedule scenario Agent |
| music | 12009 | Music scenario Agent |
| weather | 12010 | Weather scenario Agent |
| gateway-omniasst | 12001 | Business gateway (HTTP + WebSocket) |
| gateway-omniasst-voice | 12002 | Voice gateway |

### Verification

```bash
# View all container status
docker compose ps

# Health check (ports per actual config)
curl http://localhost:12001/actuator/health   # Omniasst gateway
curl http://localhost:11001/actuator/health   # Central gateway
curl -fI "http://127.0.0.1:8080/"              # console root (default NACOS_CONSOLE_HOST_PORT)
curl -f "http://127.0.0.1:8848/nacos"           # core port (same idea as image healthcheck)
```

---

<a id="quickstart-gateway-only"></a>

## Gateway-Only Verification (Minimal)

1. Start **Nacos** (registration/discovery only can skip MySQL/Redis for now; follow **dev** config).
2. Per [BUILD.md](BUILD.md), install modules required by `hojoai-gateway-omniasst-server`.
3. Port conflicts: [Default Ports and Deployment](#quickstart-ports).

```bash
cd hojoai-gateway-omniasst/hojoai-gateway-omniasst-server
mvn spring-boot:run
```

Health check (gateway default **12001**):

```bash
curl http://localhost:12001/actuator/health
```

---

<a id="quickstart-full-stack"></a>

## Full Integration: Minimal Local Runtime Topology (Example)

Reference order for Omniasst gateway integration (no root `docker-compose`; infra self-hosted or use [Docker examples](#quickstart-infra-docker)):

1. Start **Nacos, MySQL, Redis, PostgreSQL** and fill each service `application-dev.properties` (and env vars).
2. Multiple services on one host: stagger ports per [Default Ports and Deployment](#quickstart-ports).
3. **Startup order** (trim by dependency; not a fixed chain):
   - **Central account**: For login/auth, calling central, or gateway filters hitting `central.account.base.url`, start **`hojoai-webmvc-central-account`** first. `hojoai-gateway-central` is routing-only; clients may bypass it; locally you can hit the account port directly.
   - **Omniasst downstream**: Targets of gateway `lb://...` must be in **Nacos** (e.g. management → voice needs **`hojoai-gateway-omniasst-voice`** registered).
   - **Omniasst gateway**: **`hojoai-gateway-omniasst`** can start later; routes fail until downstream registers.
4. Routes and APIs: [API.md](../hojoai-gateway-omniasst/API.md) and per-service config; Maven: [Service Dependencies](../README.md#service-dependencies).

---

<a id="quickstart-run"></a>

## Running

1. Local `install` in [BUILD.md](BUILD.md) for the modules you need.
2. **Nacos, MySQL, Redis, PostgreSQL** ready; ports per [Default Ports and Deployment](#quickstart-ports).
3. In the target **`*-server`** directory run `mvn spring-boot:run` (or the Spring Boot main class from IDE). Example (Omniasst gateway; port in [Services](../README.md#services)):

```bash
cd hojoai-gateway-omniasst/hojoai-gateway-omniasst-server
mvn spring-boot:run
```

Repeat for other modules under their `*-server` folders.

---

<a id="quickstart-verify"></a>

## Verification

Health check (use your real ports; gateway example **12001**):

```bash
curl http://localhost:12001/actuator/health
```
