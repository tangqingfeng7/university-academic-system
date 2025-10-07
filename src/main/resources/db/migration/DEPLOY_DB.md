# 数据库部署指南

## 初始化步骤

### 1. 创建数据库

```sql
CREATE DATABASE academic_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 创建数据库用户（生产环境）

```sql
-- 创建专用用户
CREATE USER 'academic_user'@'localhost' IDENTIFIED BY 'your_strong_password';

-- 授予权限
GRANT ALL PRIVILEGES ON academic_system.* TO 'academic_user'@'localhost';

-- 如果需要远程访问
CREATE USER 'academic_user'@'%' IDENTIFIED BY 'your_strong_password';
GRANT ALL PRIVILEGES ON academic_system.* TO 'academic_user'@'%';

-- 刷新权限
FLUSH PRIVILEGES;
```

### 3. 导入表结构

```bash
mysql -u academic_user -p academic_system < schema.sql
```

### 4. 导入测试数据（可选）

```bash
mysql -u academic_user -p academic_system < test_data.sql
```

## 验证部署

```sql
-- 检查表
USE academic_system;
SHOW TABLES;

-- 检查默认管理员账户
SELECT * FROM sys_user WHERE username = 'admin';

-- 检查院系数据
SELECT * FROM department;

-- 检查系统配置
SELECT * FROM system_config;
```

## 备份与恢复

### 备份数据库

```bash
# 完整备份
mysqldump -u academic_user -p academic_system > backup_$(date +%Y%m%d).sql

# 仅备份结构
mysqldump -u academic_user -p --no-data academic_system > schema_backup.sql

# 仅备份数据
mysqldump -u academic_user -p --no-create-info academic_system > data_backup.sql
```

### 恢复数据库

```bash
# 恢复完整备份
mysql -u academic_user -p academic_system < backup_20251008.sql
```

## 数据迁移

### 从旧版本迁移

如果从旧版本升级，请执行以下步骤：

1. 备份现有数据
2. 执行迁移脚本（如有）
3. 验证数据完整性

## 性能优化

### 添加索引（已在schema.sql中）

```sql
-- 关键索引
CREATE INDEX idx_student_no ON student(student_no);
CREATE INDEX idx_teacher_no ON teacher(teacher_no);
CREATE INDEX idx_course_no ON course(course_no);
-- 更多索引请参考schema.sql
```

### 定期优化

```sql
-- 分析表
ANALYZE TABLE student, teacher, course, grade;

-- 优化表
OPTIMIZE TABLE student, teacher, course, grade;
```

## 安全注意事项

1. ✅ 使用强密码
2. ✅ 限制远程访问
3. ✅ 定期备份
4. ✅ 监控慢查询
5. ✅ 及时更新MySQL版本

## 常见问题

### 字符编码问题

确保所有表使用utf8mb4编码：

```sql
ALTER DATABASE academic_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 连接数限制

```sql
-- 查看最大连接数
SHOW VARIABLES LIKE 'max_connections';

-- 修改最大连接数
SET GLOBAL max_connections = 500;
```

### 慢查询优化

```sql
-- 启用慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;

-- 查看慢查询
SHOW FULL PROCESSLIST;
```

## 监控指标

建议监控以下指标：

- 连接数
- 查询响应时间
- 慢查询数量
- 表锁等待
- 磁盘空间

## 更新日志

- **2025-10-08**: 初始版本
- 包含14张核心表
- 支持三种角色：管理员、教师、学生

