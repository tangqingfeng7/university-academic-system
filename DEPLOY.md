# 大学教务管理系统 - 部署文档

本文档详细说明如何将大学教务管理系统部署到生产环境。

## 📋 目录

- [环境要求](#环境要求)
- [部署架构](#部署架构)
- [部署步骤](#部署步骤)
- [配置说明](#配置说明)
- [启动与停止](#启动与停止)
- [监控与日志](#监控与日志)
- [常见问题](#常见问题)

## 环境要求

### 硬件要求

**最低配置：**
- CPU: 2核
- 内存: 4GB
- 磁盘: 50GB

**推荐配置：**
- CPU: 4核及以上
- 内存: 8GB及以上
- 磁盘: 100GB及以上（SSD）

### 软件要求

- **操作系统**: Linux (Ubuntu 20.04+, CentOS 7+) 或 Windows Server
- **Java**: JDK 17 或更高版本
- **MySQL**: 8.0 或更高版本
- **Node.js**: 16.x 或更高版本（仅构建时需要）
- **Maven**: 3.6+ （仅构建时需要）

## 部署架构

### 单机部署架构

```
┌─────────────────────────────────────┐
│         Nginx (可选)                │
│      反向代理 + 静态资源            │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│     Spring Boot Application         │
│  - 后端API (Port 8080)              │
│  - 前端静态资源                      │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         MySQL Database              │
│         (Port 3306)                 │
└─────────────────────────────────────┘
```

### 分布式部署架构（可选）

```
                    ┌──────────┐
                    │   CDN    │
                    └─────┬────┘
                          │
                    ┌─────▼─────┐
                    │   Nginx   │
                    │  负载均衡  │
                    └─────┬─────┘
           ┌──────────────┼──────────────┐
           │              │              │
      ┌────▼────┐    ┌────▼────┐    ┌────▼────┐
      │ App-1   │    │ App-2   │    │ App-3   │
      │(8080)   │    │(8080)   │    │(8080)   │
      └────┬────┘    └────┬────┘    └────┬────┘
           └──────────────┼──────────────┘
                          │
                    ┌─────▼─────┐
                    │   Redis   │
                    │   缓存     │
                    └─────┬─────┘
                          │
                    ┌─────▼─────┐
                    │   MySQL   │
                    │  主从复制  │
                    └───────────┘
```

## 部署步骤

### 1. 准备服务器

```bash
# 更新系统
sudo apt update && sudo apt upgrade -y  # Ubuntu/Debian
# 或
sudo yum update -y                       # CentOS/RHEL

# 安装JDK 17
sudo apt install openjdk-17-jdk -y

# 验证Java安装
java -version
```

### 2. 安装MySQL

```bash
# Ubuntu/Debian
sudo apt install mysql-server -y

# CentOS/RHEL
sudo yum install mysql-server -y

# 启动MySQL
sudo systemctl start mysql
sudo systemctl enable mysql

# 安全配置
sudo mysql_secure_installation
```

### 3. 创建数据库和用户

```sql
-- 登录MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE academic_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建专用用户
CREATE USER 'academic_user'@'localhost' IDENTIFIED BY 'your_strong_password';

-- 授予权限
GRANT ALL PRIVILEGES ON academic_system.* TO 'academic_user'@'localhost';
FLUSH PRIVILEGES;

-- 退出
EXIT;
```

### 4. 导入数据库结构

```bash
# 导入表结构
mysql -u academic_user -p academic_system < db/schema.sql

# 导入初始数据（可选）
mysql -u academic_user -p academic_system < db/test_data.sql
```

### 5. 构建应用

在开发机器上：

```bash
# 克隆代码
git clone <repository-url>
cd university-academic-system

# 构建前端
cd src/frontend
npm install
npm run build

# 构建后端（包含前端静态资源）
cd ../..
mvn clean package -DskipTests

# 生成的jar文件位于：target/academic-system.jar
```

### 6. 上传到服务器

```bash
# 创建应用目录
sudo mkdir -p /opt/academic-system
sudo chown $USER:$USER /opt/academic-system

# 上传jar文件
scp target/academic-system.jar user@server:/opt/academic-system/

# 上传配置文件
scp src/main/resources/application-prod.yml user@server:/opt/academic-system/
```

### 7. 配置环境变量

创建环境变量文件：

```bash
sudo nano /opt/academic-system/.env
```

添加以下内容：

```bash
# 应用配置
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=academic_system
DB_USERNAME=academic_user
DB_PASSWORD=your_strong_password

# JWT配置
JWT_SECRET=your_very_long_and_secure_secret_key_here
JWT_EXPIRATION=86400000

# 日志配置
LOG_PATH=/opt/academic-system/logs
LOG_LEVEL=INFO
```

### 8. 创建systemd服务

创建服务文件：

```bash
sudo nano /etc/systemd/system/academic-system.service
```

添加以下内容：

```ini
[Unit]
Description=University Academic Management System
After=syslog.target network.target mysql.service

[Service]
Type=simple
User=academic
Group=academic
WorkingDirectory=/opt/academic-system
EnvironmentFile=/opt/academic-system/.env
ExecStart=/usr/bin/java -jar -Xms512m -Xmx2048m /opt/academic-system/academic-system.jar
SuccessExitStatus=143
StandardOutput=journal
StandardError=journal
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

### 9. 启动应用

```bash
# 创建日志目录
sudo mkdir -p /opt/academic-system/logs
sudo chown academic:academic /opt/academic-system/logs

# 重载systemd配置
sudo systemctl daemon-reload

# 启动服务
sudo systemctl start academic-system

# 设置开机自启
sudo systemctl enable academic-system

# 查看状态
sudo systemctl status academic-system
```

### 10. 配置Nginx反向代理（可选）

安装Nginx：

```bash
sudo apt install nginx -y
```

创建配置文件：

```bash
sudo nano /etc/nginx/sites-available/academic-system
```

添加以下配置：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 访问日志
    access_log /var/log/nginx/academic-access.log;
    error_log /var/log/nginx/academic-error.log;

    # 前端静态资源
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # API接口
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket支持（如需要）
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # 静态资源缓存
    location ~* \.(jpg|jpeg|png|gif|ico|css|js|svg|woff|woff2|ttf|eot)$ {
        proxy_pass http://localhost:8080;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
```

启用配置：

```bash
sudo ln -s /etc/nginx/sites-available/academic-system /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

### 11. 配置HTTPS（推荐）

使用Let's Encrypt：

```bash
# 安装certbot
sudo apt install certbot python3-certbot-nginx -y

# 获取SSL证书
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo systemctl enable certbot.timer
```

## 配置说明

### application-prod.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:academic_system}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

  jpa:
    hibernate:
      ddl-auto: none  # 生产环境必须为none
    show-sql: false
    properties:
      hibernate:
        format_sql: false

server:
  port: ${SERVER_PORT:8080}
  compression:
    enabled: true
    mime-types: text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json

jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:86400000}

logging:
  level:
    root: ${LOG_LEVEL:INFO}
    com.university.academic: INFO
  file:
    name: ${LOG_PATH:/var/log}/academic-system.log
  logback:
    rollingpolicy:
      max-file-size: 10MB
      max-history: 30
```

### JVM参数调优

推荐的JVM参数：

```bash
java -jar \
  -Xms1g \
  -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/opt/academic-system/logs/heapdump.hprof \
  academic-system.jar
```

## 启动与停止

### 使用systemd（推荐）

```bash
# 启动
sudo systemctl start academic-system

# 停止
sudo systemctl stop academic-system

# 重启
sudo systemctl restart academic-system

# 查看状态
sudo systemctl status academic-system

# 查看日志
sudo journalctl -u academic-system -f
```

### 使用脚本

创建 `start.sh`：

```bash
#!/bin/bash
nohup java -jar \
  -Xms512m -Xmx2048m \
  -Dspring.profiles.active=prod \
  academic-system.jar > /dev/null 2>&1 &
echo $! > app.pid
echo "应用已启动，PID: $(cat app.pid)"
```

创建 `stop.sh`：

```bash
#!/bin/bash
if [ -f app.pid ]; then
  PID=$(cat app.pid)
  kill $PID
  rm app.pid
  echo "应用已停止"
else
  echo "应用未运行"
fi
```

## 监控与日志

### 应用日志

```bash
# 查看实时日志
tail -f /opt/academic-system/logs/academic-system.log

# 查看错误日志
grep ERROR /opt/academic-system/logs/academic-system.log

# 查看最近100行
tail -n 100 /opt/academic-system/logs/academic-system.log
```

### 系统监控

```bash
# 查看进程
ps aux | grep academic-system

# 查看端口
sudo netstat -tulpn | grep 8080

# 查看资源使用
top -p $(cat /opt/academic-system/app.pid)
```

### 数据库备份

创建备份脚本：

```bash
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/opt/backups/academic-system"
mkdir -p $BACKUP_DIR

# 备份数据库
mysqldump -u academic_user -p'password' academic_system > \
  $BACKUP_DIR/academic_system_$DATE.sql

# 压缩备份
gzip $BACKUP_DIR/academic_system_$DATE.sql

# 删除30天前的备份
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete
```

设置定时任务：

```bash
# 编辑crontab
crontab -e

# 添加每天凌晨2点备份
0 2 * * * /opt/academic-system/backup.sh
```

## 常见问题

### 1. 应用无法启动

**问题**：启动失败，日志显示端口被占用

**解决**：
```bash
# 查找占用8080端口的进程
sudo lsof -i :8080
# 或
sudo netstat -tulpn | grep 8080

# 杀死进程
sudo kill -9 <PID>
```

### 2. 数据库连接失败

**问题**：无法连接到MySQL

**解决**：
```bash
# 检查MySQL状态
sudo systemctl status mysql

# 测试连接
mysql -u academic_user -p -h localhost academic_system

# 检查防火墙
sudo ufw status
sudo ufw allow 3306/tcp
```

### 3. 内存溢出

**问题**：OutOfMemoryError

**解决**：
```bash
# 增加堆内存
-Xms2g -Xmx4g

# 分析堆转储
jhat /opt/academic-system/logs/heapdump.hprof
```

### 4. 性能问题

**问题**：响应缓慢

**解决**：
```bash
# 检查数据库连接池
# 查看application-prod.yml中的hikari配置

# 检查慢查询
mysql -u root -p -e "SHOW FULL PROCESSLIST;"

# 启用慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;
```

### 5. 日志文件过大

**问题**：日志占用大量磁盘空间

**解决**：
```bash
# 配置日志滚动（已在application-prod.yml中配置）
# 手动清理旧日志
find /opt/academic-system/logs -name "*.log.*" -mtime +30 -delete
```

## 升级部署

### 零停机升级（使用Nginx）

1. 部署新版本到新端口（如8081）
2. 测试新版本
3. 修改Nginx配置，逐步切换流量
4. 停止旧版本

```bash
# 1. 启动新版本
java -jar -Dserver.port=8081 academic-system-v2.jar &

# 2. 修改Nginx upstream
upstream backend {
    server localhost:8080 weight=1;
    server localhost:8081 weight=9;
}

# 3. 重载Nginx
sudo nginx -s reload

# 4. 观察一段时间后，停止旧版本
kill <old-pid>
```

## 性能调优建议

1. **数据库优化**
   - 添加适当的索引
   - 定期优化表
   - 配置查询缓存

2. **应用优化**
   - 启用缓存（已配置Caffeine）
   - 调整连接池大小
   - 使用异步处理

3. **系统优化**
   - 调整Linux内核参数
   - 使用SSD存储
   - 配置CDN加速静态资源

4. **监控告警**
   - 配置Prometheus + Grafana
   - 设置告警规则
   - 定期查看性能指标

## 安全建议

1. ✅ 使用强密码
2. ✅ 启用HTTPS
3. ✅ 定期更新系统和依赖
4. ✅ 配置防火墙规则
5. ✅ 定期备份数据
6. ✅ 限制数据库访问权限
7. ✅ 启用应用日志审计
8. ✅ 定期安全扫描

## 支持与维护

- **技术支持**: support@example.com
- **文档更新**: 2025-10-08
- **版本**: v1.0.0

---

如有问题，请参考 [README.md](README.md) 或提交 Issue。

