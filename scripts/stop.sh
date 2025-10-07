#!/bin/bash

###############################################################################
# 大学教务管理系统 - 停止脚本
# 用途: 停止Spring Boot应用
# 使用: ./stop.sh
###############################################################################

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 应用配置
APP_NAME="academic-system"
APP_DIR="$(cd "$(dirname "$0")/.." && pwd)"
PID_FILE="${APP_DIR}/app.pid"

# 超时时间（秒）
TIMEOUT=30

# 函数: 打印信息
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 函数: 检查应用是否运行
is_running() {
    if [ -f "${PID_FILE}" ]; then
        PID=$(cat "${PID_FILE}")
        if ps -p ${PID} > /dev/null 2>&1; then
            return 0
        else
            rm -f "${PID_FILE}"
            return 1
        fi
    fi
    return 1
}

# 主逻辑
print_info "======================================"
print_info "停止 ${APP_NAME}"
print_info "======================================"

# 检查应用是否运行
if ! is_running; then
    print_warn "应用未运行"
    exit 0
fi

PID=$(cat "${PID_FILE}")
print_info "发现运行中的应用 (PID: ${PID})"

# 尝试优雅关闭（SIGTERM）
print_info "发送停止信号 (SIGTERM)..."
kill ${PID}

# 等待应用停止
COUNTER=0
while [ ${COUNTER} -lt ${TIMEOUT} ]; do
    if ! ps -p ${PID} > /dev/null 2>&1; then
        print_info "应用已成功停止"
        rm -f "${PID_FILE}"
        print_info "======================================"
        exit 0
    fi
    
    echo -n "."
    sleep 1
    COUNTER=$((COUNTER + 1))
done

echo ""
print_warn "应用未在 ${TIMEOUT} 秒内停止"

# 强制停止（SIGKILL）
print_warn "强制停止应用 (SIGKILL)..."
kill -9 ${PID}

# 再次验证
sleep 2
if ! ps -p ${PID} > /dev/null 2>&1; then
    print_info "应用已被强制停止"
    rm -f "${PID_FILE}"
else
    print_error "无法停止应用 (PID: ${PID})"
    print_info "请手动停止: kill -9 ${PID}"
    exit 1
fi

print_info "======================================"

