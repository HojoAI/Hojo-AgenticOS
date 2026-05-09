# Hojo-Agentic-OS 本地开发启动脚本
#
# 使用方法：
#   make build        # 构建所有服务的 jar 包
#   make up          # 启动所有服务（先构建）
#   make down        # 停止所有服务
#   make logs        # 查看所有服务日志
#   make clean       # 清理构建产物

.PHONY: build up down logs clean

# 构建顺序（按依赖关系）
BUILD_ORDER := \
	hojoai-parent/pom.xml \
	hojoai-common/pom.xml \
	hojoai-omniasst-common/pom.xml \
	hojoai-grpc-central-provider/pom.xml \
	hojoai-gateway-omniasst/pom.xml \
	hojoai-gateway-omniasst-voice/pom.xml \
	hojoai-gateway-central/pom.xml \
	hojoai-webmvc-central-account/pom.xml \
	hojoai-webmvc-omniasst-account/pom.xml \
	hojoai-webmvc-omniasst-music/pom.xml \
	hojoai-webmvc-omniasst-weather/pom.xml \
	hojoai-webmvc-omniasst-management/pom.xml \
	hojoai-webmvc-omniasst-translate/pom.xml \
	hojoai-webmvc-omniasst-focus/pom.xml \
	hojoai-routing-engine-contract/pom.xml \
	hojoai-websocket-omniasst-sysagent/pom.xml \
	hojoai-webmvc-omniasst-meeting/pom.xml

build:
	@echo "=== Building all services (Maven) ==="
	@for pom in $(BUILD_ORDER); do \
		echo "Building $$pom..."; \
		mvn -f $$pom clean install -DskipTests -q || exit 1; \
	done
	@echo "=== Build complete ==="

up: build
	@echo "=== Starting all services (Docker Compose) ==="
	docker compose up -d
	@echo "=== All services started ==="
	@echo "View logs: make logs"

down:
	@echo "=== Stopping all services ==="
	docker compose down
	@echo "=== All services stopped ==="

logs:
	docker compose logs -f

clean:
	@echo "=== Cleaning build artifacts ==="
	@for dir in hojoai-*/; do \
		if [ -d "$$dir" ]; then \
			find $$dir -type d -name "target" -exec rm -rf {} + 2>/dev/null || true; \
		fi; \
	done
	docker compose down -v
	@echo "=== Clean complete ==="