# 数据库初始化说明

## 初始化步骤

### 1. 创建数据库

```sql
CREATE DATABASE academic_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 执行初始化脚本

```bash
mysql -u root -p academic_system < schema.sql
```

或在MySQL客户端中执行：

```sql
USE academic_system;
SOURCE /path/to/schema.sql;
```

### 3. 验证数据

```sql
USE academic_system;

-- 查看所有表
SHOW TABLES;

-- 查看默认管理员账户
SELECT * FROM sys_user;

-- 查看默认院系
SELECT * FROM department;

-- 查看默认专业
SELECT * FROM major;

-- 查看系统配置
SELECT * FROM system_config;
```

## 默认账户

### 管理员账户
- 用户名: `admin`
- 密码: `admin123`
- 角色: ADMIN

**注意：** 首次登录后系统会要求修改密码。

## 数据库表说明

| 表名 | 说明 | 记录数 |
|-----|------|--------|
| sys_user | 用户表 | 1（默认管理员） |
| department | 院系表 | 5 |
| major | 专业表 | 5 |
| student | 学生表 | 0 |
| teacher | 教师表 | 0 |
| course | 课程表 | 0 |
| course_prerequisite | 先修课程表 | 0 |
| semester | 学期表 | 0 |
| course_offering | 开课计划表 | 0 |
| course_selection | 选课记录表 | 0 |
| grade | 成绩表 | 0 |
| notification | 通知公告表 | 0 |
| system_config | 系统配置表 | 8 |
| operation_log | 操作日志表 | 0 |

## 视图说明

系统创建了3个视图用于简化查询：

1. `v_student_detail` - 学生详细信息视图（包含专业、院系信息）
2. `v_teacher_detail` - 教师详细信息视图（包含院系信息）
3. `v_course_detail` - 课程详细信息视图（包含院系信息）

## 索引说明

系统为常用查询字段创建了索引，包括：
- 用户名、角色
- 学号、工号
- 课程编号
- 学期、选课状态
- 成绩状态
- 等等

## 注意事项

1. **密码加密**：用户密码使用BCrypt加密存储
2. **软删除**：学生表使用软删除机制（deleted字段）
3. **乐观锁**：开课计划表使用version字段实现乐观锁
4. **外键约束**：所有关联表都设置了外键约束
5. **唯一约束**：用户名、学号、工号等字段设置了唯一约束

## 清空数据

如果需要清空所有数据重新初始化：

```sql
-- 警告：此操作会删除所有数据！
DROP DATABASE IF EXISTS academic_system;
CREATE DATABASE academic_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE academic_system;
SOURCE /path/to/schema.sql;
```

## 备份与恢复

### 备份数据库

```bash
mysqldump -u root -p academic_system > backup_$(date +%Y%m%d_%H%M%S).sql
```

### 恢复数据库

```bash
mysql -u root -p academic_system < backup_20240101_120000.sql
```

