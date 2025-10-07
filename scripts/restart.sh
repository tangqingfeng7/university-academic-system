#!/bin/bash

###############################################################################
# 大学教务管理系统 - 重启脚本
# 用途: 重启Spring Boot应用
# 使用: ./restart.sh [环境]
# 例如: ./restart.sh prod
###############################################################################

# 颜色定义
GREEN='\033[0;32m'
NC='\033[0m' # No Color

# 应用配置
APP_DIR="$(cd "$(dirname "$0")/.." && pwd)"
PROFILE=${1:-dev}

# 函数: 打印信息
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_info "======================================"
print_info "重启应用 (环境: ${PROFILE})"
print_info "======================================"

# 停止应用
${APP_DIR}/scripts/stop.sh

# 等待一下确保完全停止
sleep 2

# 启动应用
${APP_DIR}/scripts/start.sh ${PROFILE}

