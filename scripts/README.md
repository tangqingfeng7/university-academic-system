# 启动脚本说明

本目录包含用于管理大学教务管理系统的各种脚本。

## Linux/Mac 脚本

### 1. start.sh - 启动脚本

启动应用程序。

**用法：**
```bash
./start.sh [环境]
```

**示例：**
```bash
# 开发环境启动
./start.sh dev

# 生产环境启动
./start.sh prod

# 默认（开发环境）
./start.sh
```

**功能：**
- 检查应用是否已运行
- 创建必要的目录
- 加载环境变量（从.env文件）
- 使用配置的JVM参数启动应用
- 记录PID到app.pid文件
- 验证启动状态

### 2. stop.sh - 停止脚本

停止正在运行的应用程序。

**用法：**
```bash
./stop.sh
```

**功能：**
- 检查应用是否运行
- 发送SIGTERM信号优雅关闭
- 等待最多30秒
- 如果未停止，强制发送SIGKILL
- 清理PID文件

### 3. restart.sh - 重启脚本

重启应用程序。

**用法：**
```bash
./restart.sh [环境]
```

**示例：**
```bash
# 以生产环境重启
./restart.sh prod
```

**功能：**
- 调用stop.sh停止应用
- 等待2秒
- 调用start.sh启动应用

### 4. status.sh - 状态查看脚本

查看应用运行状态和详细信息。

**用法：**
```bash
./status.sh
```

**显示信息：**
- 应用运行状态
- 进程信息（PID、CPU、内存使用）
- 端口监听状态
- 运行环境
- 日志文件信息
- 系统资源使用
- 访问地址
- 常用操作命令

## Windows 脚本

### 1. start.bat - Windows启动脚本

在Windows系统上启动应用程序。

**用法：**
```cmd
start.bat [环境]
```

**示例：**
```cmd
REM 开发环境
start.bat dev

REM 生产环境
start.bat prod
```

### 2. stop.bat - Windows停止脚本

在Windows系统上停止应用程序。

**用法：**
```cmd
stop.bat
```

## 环境变量配置

创建 `.env` 文件在项目根目录：

```bash
# 应用配置
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=academic_system
DB_USERNAME=academic_user
DB_PASSWORD=your_password

# JWT配置
JWT_SECRET=your_secret_key
JWT_EXPIRATION=86400000

# 日志配置
LOG_PATH=logs
LOG_LEVEL=INFO
```

## JVM参数说明

脚本中配置的JVM参数：

- `-Xms512m` : 初始堆内存512MB
- `-Xmx2048m` : 最大堆内存2GB
- `-XX:+UseG1GC` : 使用G1垃圾收集器
- `-XX:MaxGCPauseMillis=200` : 最大GC暂停时间200ms
- `-XX:+HeapDumpOnOutOfMemoryError` : OOM时生成堆转储
- `-XX:HeapDumpPath=logs/heapdump.hprof` : 堆转储文件路径

## 权限设置

在Linux/Mac上，需要给脚本添加执行权限：

```bash
chmod +x scripts/*.sh
```

## 使用systemd（Linux推荐）

对于生产环境，推荐使用systemd管理服务。参考DEPLOY.md中的配置。

## 日志查看

**实时查看日志：**
```bash
tail -f logs/academic-system.log
```

**查看最后100行：**
```bash
tail -n 100 logs/academic-system.log
```

**搜索错误：**
```bash
grep ERROR logs/academic-system.log
```

## 故障排查

### 应用无法启动

1. 检查jar文件是否存在
2. 查看日志文件
3. 检查端口是否被占用
4. 验证数据库连接

**检查端口占用：**
```bash
# Linux/Mac
lsof -i :8080
netstat -tuln | grep 8080

# Windows
netstat -ano | findstr :8080
```

### 应用无法停止

如果stop.sh无法停止应用，手动停止：

```bash
# 查找PID
ps aux | grep academic-system

# 强制停止
kill -9 <PID>

# 清理PID文件
rm app.pid
```

## 最佳实践

1. **开发环境**：使用`./start.sh dev`启动，启用Swagger和详细日志
2. **生产环境**：使用`./start.sh prod`启动，关闭调试功能
3. **监控**：定期运行`./status.sh`检查应用状态
4. **日志管理**：定期清理或归档旧日志文件
5. **备份**：部署前备份数据库

## 自动化部署

可以将脚本集成到CI/CD流程：

```bash
# 示例：Jenkins部署脚本
#!/bin/bash
cd /opt/academic-system
git pull
mvn clean package -DskipTests
./scripts/restart.sh prod
./scripts/status.sh
```

## 支持

如有问题，请参考：
- [README.md](../README.md) - 项目说明
- [DEPLOY.md](../DEPLOY.md) - 部署文档

---

**版本**: v1.0.0  
**更新**: 2025-10-08

