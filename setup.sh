#!/bin/bash

# ============================================================
# HojoAI 本地开发环境初始化脚本
# ============================================================

set -e

ENV_FILE=".env"
ENV_EXAMPLE=".env.example"

echo "==> HojoAI 本地开发环境初始化"

# 检查 .env 是否存在
if [ -f "$ENV_FILE" ]; then
    echo "==> 发现已有 .env 文件"
    echo "是否重新生成 .env 文件？这将覆盖现有配置。"
    read -p "按 Ctrl+C 取消，或按回车继续重新生成... " -r
    echo
fi

# 检查 .env.example 是否存在
if [ ! -f "$ENV_EXAMPLE" ]; then
    echo "错误: $ENV_EXAMPLE 文件不存在"
    exit 1
fi

# 复制模板文件
echo "==> 复制 $ENV_EXAMPLE -> $ENV_FILE"
cp "$ENV_EXAMPLE" "$ENV_FILE"

echo ""
echo "==> 初始化完成！"
echo ""
echo "请编辑 .env 文件，配置必要的环境变量（至少需要配置以下内容）："
echo "  - NACOS_SERVER_ADDR (默认: nacos:8848)"
echo "  - MYSQL_HOST (默认: mysql)"
echo "  - REDIS_HOST (默认: redis)"
echo ""
echo "编辑完成后，运行以下命令启动所有服务："
echo "  docker compose up -d"
echo ""
echo "查看服务状态："
echo "  docker compose ps"
echo ""
echo "查看日志："
echo "  docker compose logs -f [服务名]"
echo ""
echo "停止所有服务："
echo "  docker compose down"
