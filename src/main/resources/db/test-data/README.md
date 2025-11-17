# 测试数据文件说明

本目录包含用于开发和测试环境的测试数据脚本。

## ⚠️ 重要提示

**本目录中的 SQL 文件仅用于开发和测试环境，请勿在生产环境执行！**

## 目录结构

```
test-data/
├── test_data.sql                              # 基础测试数据（核心）
├── additional_test_data.sql                   # 附加测试数据
├── complete_class_data.sql                    # 完整班级数据
├── classroom_test_data.sql                    # 教室测试数据
├── classroom_utilization_test_data.sql        # 教室利用率测试数据
├── evaluation_test_data.sql                   # 教学评价测试数据
├── exam_test_data.sql                         # 考试测试数据
├── notifications_and_prerequisites.sql        # 通知和先修课程数据
├── test_data_scheduling_fixed.sql             # 排课模块测试数据（修正版）
└── deprecated/                                # 已废弃的旧版本文件
    ├── test_data_scheduling.sql               # 旧版排课测试数据
    ├── simple_additional_data.sql             # 简化版附加数据
    ├── status_change_test_data.sql            # 旧版学籍异动数据
    ├── student_status_change_test_data.sql    # 旧版学生状态变更数据
    ├── insert_status_change_test_data.sql     # 旧版学籍异动插入数据
    ├── test_data_class.sql                    # 旧版班级数据
    └── unify_class_data.sql                   # 旧版班级统一数据
```

## 测试数据文件说明

### 核心文件（按执行顺序）

#### 1. test_data.sql
**描述**：基础测试数据，包含最核心的数据  
**包含内容**：
- 院系数据（计算机学院、数学学院、物理学院等）
- 专业数据（计算机科学与技术、软件工程等）
- 学期数据（2023-2024、2024-2025 学年）
- 基础用户、教师、学生数据
- 核心课程数据

**推荐使用场景**：快速启动开发环境

#### 2. additional_test_data.sql
**描述**：补充测试数据  
**包含内容**：
- 更多学生数据
- 课程开课计划
- 选课记录
- 成绩数据

**依赖**：需要先执行 `test_data.sql`

#### 3. complete_class_data.sql
**描述**：完整的班级数据  
**包含内容**：
- 所有专业的标准班级（2021-2024 级）
- 班级-专业-年级关联

**依赖**：需要先执行 `test_data.sql`

### 模块化测试数据

#### classroom_test_data.sql
**模块**：教室管理  
**包含内容**：
- 各类教室（教学楼、实验楼）
- 教室设施信息
- 教室容量数据

#### classroom_utilization_test_data.sql
**模块**：教室利用率  
**包含内容**：
- 教室使用记录
- 教室预定数据
- 利用率统计相关数据

#### evaluation_test_data.sql
**模块**：教学评价  
**包含内容**：
- 评价周期配置
- 课程评价记录
- 教师评价记录
- 评价指标数据

#### exam_test_data.sql
**模块**：考试管理  
**包含内容**：
- 考试安排
- 考场分配
- 监考安排
- 考生座位分配

#### notifications_and_prerequisites.sql
**模块**：通知与先修课程  
**包含内容**：
- 系统通知
- 课程先修关系
- 通知阅读记录

#### test_data_scheduling_fixed.sql
**模块**：智能排课  
**包含内容**：
- 排课约束配置
- 教师排课偏好
- 排课方案数据

**说明**：此文件为修正版，替代 `deprecated/test_data_scheduling.sql`

## 使用指南

### 开发环境初始化

#### 方式一：快速启动（推荐）
执行最小测试数据集，快速搭建开发环境：

```bash
mysql -u root -p academic_system < test_data.sql
```

#### 方式二：完整数据
按顺序执行所有测试数据文件：

```bash
# 1. 基础数据
mysql -u root -p academic_system < test_data.sql

# 2. 附加数据
mysql -u root -p academic_system < additional_test_data.sql

# 3. 班级数据
mysql -u root -p academic_system < complete_class_data.sql

# 4. 模块测试数据（按需）
mysql -u root -p academic_system < classroom_test_data.sql
mysql -u root -p academic_system < classroom_utilization_test_data.sql
mysql -u root -p academic_system < evaluation_test_data.sql
mysql -u root -p academic_system < exam_test_data.sql
mysql -u root -p academic_system < notifications_and_prerequisites.sql
mysql -u root -p academic_system < test_data_scheduling_fixed.sql
```

#### 方式三：通过 Spring Boot 自动加载
在 `application-dev.yml` 中配置：

```yaml
spring:
  datasource:
    data: 
      - classpath:db/test-data/test_data.sql
      - classpath:db/test-data/additional_test_data.sql
      - classpath:db/test-data/complete_class_data.sql
```

### 测试环境重置

清空数据库并重新导入测试数据：

```bash
# 1. 清空数据库（谨慎操作！）
mysql -u root -p -e "DROP DATABASE IF EXISTS academic_system; CREATE DATABASE academic_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 重新创建数据库架构（需要有完整的schema.sql）
# mysql -u root -p academic_system < schema.sql

# 3. 导入测试数据
mysql -u root -p academic_system < test_data.sql
mysql -u root -p academic_system < additional_test_data.sql
```

## 测试账号

### 管理员账号
- **用户名**: `admin`
- **密码**: `admin123`
- **角色**: ADMIN

### 教师账号示例
- **用户名**: `T2024001` (张伟)
- **密码**: `password123`
- **角色**: TEACHER

### 学生账号示例
- **用户名**: `2024001001` (李明)
- **密码**: `password123`
- **角色**: STUDENT

**说明**：所有测试账号的默认密码均为 `password123`（BCrypt 加密）

## 数据量统计

| 数据类型 | 数量（test_data.sql） | 数量（完整） |
|---------|---------------------|------------|
| 院系 | 10 | 10 |
| 专业 | 29 | 29 |
| 教师 | 20+ | 50+ |
| 学生 | 50+ | 200+ |
| 课程 | 30+ | 100+ |
| 班级 | 5 | 100+ |
| 选课记录 | 100+ | 500+ |

## 注意事项

### 1. 数据一致性
- 所有测试数据脚本假设数据库架构已经创建完成
- 请确保先创建数据库表结构，再导入测试数据

### 2. 外键约束
测试数据严格遵循外键约束，执行顺序很重要：
- 先导入基础数据（院系、专业、用户）
- 再导入关联数据（学生、教师、课程）
- 最后导入业务数据（选课、成绩、考试）

### 3. 重复执行
大多数测试数据脚本**不支持**幂等性（重复执行会报错）。  
如需重新导入，请先清空相关表数据：

```sql
-- 示例：清空学生数据
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE student;
TRUNCATE TABLE sys_user WHERE role = 'STUDENT';
SET FOREIGN_KEY_CHECKS = 1;
```

### 4. 生产环境
**严禁**在生产环境执行本目录中的任何 SQL 文件！

## 废弃文件说明

`deprecated/` 目录包含已被替换或不再使用的旧版本测试数据文件：

| 文件名 | 原因 | 替代方案 |
|-------|------|---------|
| test_data_scheduling.sql | 数据不完整 | test_data_scheduling_fixed.sql |
| simple_additional_data.sql | 功能重复 | additional_test_data.sql |
| status_change_test_data.sql | 数据冗余 | 已整合到 additional_test_data.sql |
| student_status_change_test_data.sql | 数据冗余 | 已整合到 additional_test_data.sql |
| insert_status_change_test_data.sql | 数据冗余 | 已整合到 additional_test_data.sql |
| test_data_class.sql | 数据不全 | complete_class_data.sql |
| unify_class_data.sql | 功能重复 | complete_class_data.sql |

这些文件保留用于历史参考，不建议使用。

## 自定义测试数据

如需添加自定义测试数据：

1. 创建新的 SQL 文件，命名格式：`{module}_test_data.sql`
2. 在文件开头添加注释说明：
   ```sql
   -- =================================================================
   -- {模块名称}测试数据
   -- Description: {详细描述}
   -- Dependencies: {依赖的其他文件}
   -- Author: {作者}
   -- Date: {日期}
   -- =================================================================
   ```
3. 确保数据符合外键约束
4. 更新本 README.md，添加文件说明

## 相关文档

- [数据库架构指南](../MIGRATION_GUIDE.md)
- [MySQL 8.0 官方文档](https://dev.mysql.com/doc/refman/8.0/en/)
- [项目开发环境搭建指南](../../../../DEPLOY.md)

## 更新日志

- **2025-11-17**: 清理Flyway相关引用
- **2025-10-13**: 整理测试数据，创建文档，移动废弃文件到 deprecated/
- **2025-10-12**: 添加排课模块测试数据（test_data_scheduling_fixed.sql）
- **2025-10-10**: 添加教室利用率测试数据
- **2025-10-09**: 添加考试和评价模块测试数据

