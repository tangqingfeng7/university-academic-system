# 数据库迁移文件说明

本目录包含基于 Flyway 的数据库迁移脚本。

## 目录结构

```
migration/
├── V5__add_exam_room_version.sql                      # 添加考场版本号
├── V6__add_course_selection_enabled.sql               # 添加选课启用标志
├── V7__create_course_change_request.sql               # 创建调课申请表
├── V8__create_student_status_change_tables.sql        # 创建学籍异动表
├── V9__add_student_status_column.sql                  # 添加学生状态列
├── V10__create_exam_tables.sql                        # 创建考试相关表
├── V11__create_approval_enhancement_tables.sql        # 创建审批增强表
├── V12__create_evaluation_tables.sql                  # 创建教学评价表
├── V13__create_classroom_tables.sql                   # 创建教室管理表
├── V14__fix_approval_tables_foreign_keys.sql          # 修复审批表外键
├── V15__create_leave_request_table.sql                # 创建请假申请表
├── V16__create_scheduling_tables.sql                  # 创建排课约束、方案、教师偏好表
├── V17__placeholder_reserved.sql                      # 预留版本号
├── V18__placeholder_reserved.sql                      # 预留版本号（原测试数据已移至 test-data）
├── V19__add_missing_comments_and_timestamps.sql       # 添加缺失的注释和时间戳
├── V20__add_grade_point_column.sql                    # 添加绩点字段
├── V21__placeholder_reserved.sql                      # 预留版本号
├── V22__create_class_table.sql                        # 创建班级表
├── V23__unify_class_names.sql                         # 统一班级名称
├── V24__populate_standard_classes.sql                 # 填充标准班级数据
├── V25__create_discipline_tables.sql                  # 创建学生处分表
├── V26__add_discipline_approval_status.sql            # 添加处分审批状态
├── V27__add_missing_table_comments.sql                # 添加缺失的表注释
├── V28__create_schedule_item_table.sql                # 创建排课结果表
└── archive/                                           # 已归档的旧版本文件
    ├── schema.sql                                     # 原始完整数据库架构
    ├── schema_graduation.sql                          # 毕业审核相关架构
    ├── schema_scholarship.sql                         # 奖学金相关架构
    ├── schema_scholarship_only.sql                    # 仅奖学金架构
    ├── add_gpa_column.sql                             # 添加 GPA 列（已合并到 V20）
    ├── init_graduation_requirements.sql               # 初始化毕业要求
    ├── init_scholarship_data.sql                      # 初始化奖学金数据
    ├── test_data_*.sql                                # 各类测试数据（已移至 test-data 目录）
    └── tuition/                                       # 学费模块（V1.x 版本）
        ├── V1.0__schema_tuition.sql
        └── V1.1__test_data_tuition.sql
```

## Flyway 版本命名规范

文件命名格式：`V{version}__{description}.sql`

- **V**: 版本前缀（必需）
- **{version}**: 版本号，例如：1, 2, 3, 10, 20（整数，自动排序）
- **__**: 双下划线分隔符（必需）
- **{description}**: 描述性名称（使用下划线连接单词）

### 示例
- ✅ `V1__create_user_table.sql`
- ✅ `V10__add_email_column.sql`
- ❌ `V1_create_table.sql` （单下划线）
- ❌ `v1__create_table.sql` （小写 v）

## 迁移脚本执行顺序

Flyway 会按照版本号**升序**自动执行迁移脚本：

1. V5 → V6 → V7 → V8 → V9 → V10
2. V11 → V12 → V13 → V14 → V15
3. V16 → V17 → V18 → V19 → V20
4. V21 → V22 → V23 → V24 → V25
5. V26 → V27 → V28

**注意**：
- V1-V4 在初始 schema.sql 中已包含
- V17、V18、V21 为预留版本号（placeholder）
- 每个脚本只执行一次，并记录在 `flyway_schema_history` 表中

## 迁移历史记录

Flyway 会在数据库中创建 `flyway_schema_history` 表，记录每次迁移：

```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

该表包含以下信息：
- `installed_rank`: 安装顺序
- `version`: 版本号
- `description`: 描述
- `script`: 脚本文件名
- `checksum`: 校验和（确保脚本未被修改）
- `installed_on`: 安装时间
- `execution_time`: 执行时间（毫秒）
- `success`: 是否成功

## 最佳实践

### 1. 不要修改已执行的迁移脚本
一旦迁移脚本已经在生产环境执行，**不要修改**其内容。如需更改，请创建新的迁移脚本。

### 2. 保持迁移脚本幂等性
尽量使用 `IF NOT EXISTS`、`IF EXISTS` 等条件语句，确保脚本可以安全重复执行：

```sql
CREATE TABLE IF NOT EXISTS my_table (...);
ALTER TABLE my_table ADD COLUMN IF NOT EXISTS new_column VARCHAR(100);
```

### 3. 向后兼容
- 避免删除或重命名列/表（可能导致旧版本应用崩溃）
- 优先使用 `ADD COLUMN`，而非 `DROP COLUMN`
- 考虑使用软删除（`deleted` 标志）

### 4. 事务控制
大多数 DDL 语句在 MySQL 中会隐式提交事务，但仍建议：

```sql
-- 开始事务
START TRANSACTION;

-- 执行变更
ALTER TABLE ...;

-- 提交事务
COMMIT;
```

### 5. 测试迁移脚本
在开发/测试环境充分测试后，再应用到生产环境。

### 6. 数据迁移与结构迁移分离
- 结构变更（DDL）放在迁移脚本中
- 大量数据初始化（DML）放在 `test-data` 目录中

## 归档文件说明

`archive/` 目录包含已废弃或已整合的旧版本文件：

- **schema.sql**: 原始完整数据库架构（V1-V4 的基础）
- **schema_*.sql**: 特定模块的架构文件（已整合到版本化迁移中）
- **add_gpa_column.sql**: 已整合到 V20__add_grade_point_column.sql
- **init_*.sql**: 初始化数据脚本（供参考）
- **test_data_*.sql**: 测试数据（已移至 `../test-data/` 目录）

这些文件保留用于历史参考，但不会被 Flyway 执行。

## 故障排查

### 迁移失败
如果迁移失败，Flyway 会在 `flyway_schema_history` 表中标记为失败：

```sql
SELECT * FROM flyway_schema_history WHERE success = 0;
```

**解决方法**：
1. 修复失败的脚本
2. 手动执行修复语句
3. 使用 Flyway repair 命令清除失败记录：
   ```bash
   mvn flyway:repair
   ```

### 校验和不匹配
如果已执行的脚本被修改，Flyway 会报错。

**解决方法**：
- **不推荐**：修改 `flyway_schema_history` 表中的 `checksum` 值
- **推荐**：回滚修改，创建新的迁移脚本

## 相关文档

- [Flyway 官方文档](https://flywaydb.org/documentation/)
- [MySQL 数据类型参考](https://dev.mysql.com/doc/refman/8.0/en/data-types.html)
- [项目测试数据说明](../test-data/README.md)

## 更新日志

- **2025-10-13**: 整理迁移文件，解决版本冲突，创建文档
- **2025-10-12**: 添加排课优化模块（V16）
- **2025-10-10**: 添加教室管理、审批增强（V13-V14）
- **2025-10-09**: 添加考试、评价模块（V10-V12）

