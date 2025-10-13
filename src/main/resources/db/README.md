# 数据库资源目录

本目录包含学术管理系统的所有数据库相关资源，包括迁移脚本和测试数据。

## 📁 目录结构

```
db/
├── README.md                    # 本文件 - 目录概述
├── MIGRATION_GUIDE.md           # 数据库迁移完整指南
├── migration/                   # Flyway 迁移脚本目录
│   ├── README.md               # 迁移文件详细说明
│   ├── V5__*.sql ~ V28__*.sql  # 版本化迁移脚本
│   └── archive/                # 归档的旧版本文件
│       ├── schema.sql          # 原始完整架构
│       ├── schema_*.sql        # 各模块架构
│       ├── init_*.sql          # 初始化数据
│       └── tuition/            # 学费模块迁移
└── test-data/                  # 测试数据目录
    ├── README.md               # 测试数据使用指南
    ├── test_data.sql           # 核心测试数据
    ├── *.sql                   # 各模块测试数据
    └── deprecated/             # 废弃的旧版测试数据
```

## 🚀 快速开始

### 新环境初始化

#### 1. 执行数据库迁移
```bash
# 使用 Maven
mvn flyway:migrate

# 或通过 Spring Boot 启动时自动执行
mvn spring-boot:run
```

#### 2. 导入测试数据（可选，仅开发环境）
```bash
# 导入基础测试数据
mysql -u root -p academic_system < src/main/resources/db/test-data/test_data.sql

# 导入附加测试数据
mysql -u root -p academic_system < src/main/resources/db/test-data/additional_test_data.sql
```

### 查看迁移状态
```bash
# 查看迁移信息
mvn flyway:info

# 验证迁移状态
mvn flyway:validate
```

## 📖 详细文档

### 核心文档

1. **[MIGRATION_GUIDE.md](MIGRATION_GUIDE.md)**  
   数据库迁移完整指南，包含：
   - 所有迁移版本总览（V5-V28）
   - 模块功能矩阵
   - 架构演进历史
   - 执行迁移步骤
   - 故障排查指南

2. **[migration/README.md](migration/README.md)**  
   迁移脚本详细说明，包含：
   - Flyway 版本命名规范
   - 每个迁移文件的详细说明
   - 最佳实践和注意事项
   - 归档文件说明

3. **[test-data/README.md](test-data/README.md)**  
   测试数据使用指南，包含：
   - 测试数据文件说明
   - 按模块分类的测试数据
   - 执行顺序和依赖关系
   - 测试账号信息

## 🎯 当前状态

- **最新迁移版本**: V28
- **总迁移数**: 24 个（V5-V28）
- **数据库表数**: 50+ 张
- **测试数据文件**: 8 个活跃文件
- **最后更新**: 2025-10-13

## 🗂️ 迁移版本概览

| 阶段 | 版本范围 | 主要功能 |
|-----|---------|---------|
| 基础架构 | V1-V4 | 用户、院系、专业、课程、成绩 |
| 考试审批 | V5-V11 | 考试管理、调课申请、审批流程 |
| 评价资源 | V12-V16 | 教学评价、教室管理、智能排课 |
| 数据标准化 | V17-V21 | 表注释、时间戳、绩点计算 |
| 班级处分 | V22-V28 | 班级管理、学生处分、排课详情 |

详细信息请参考：[MIGRATION_GUIDE.md](MIGRATION_GUIDE.md)

## 📊 数据库模块

### 核心模块
- ✅ 用户管理 (sys_user)
- ✅ 组织架构 (department, major)
- ✅ 学生管理 (student)
- ✅ 教师管理 (teacher)
- ✅ 课程管理 (course, course_offering)
- ✅ 选课管理 (course_selection)
- ✅ 成绩管理 (grade)

### 业务模块
- ✅ 考试管理 (exam, exam_room, exam_invigilator)
- ✅ 教学评价 (course_evaluation, teacher_evaluation)
- ✅ 教室管理 (classroom, classroom_booking)
- ✅ 智能排课 (scheduling_solution, schedule_item)
- ✅ 班级管理 (class)
- ✅ 学籍异动 (student_status_change)
- ✅ 学生处分 (student_discipline, discipline_appeal)

### 流程模块
- ✅ 审批流程 (approval_configuration)
- ✅ 调课申请 (course_change_request)
- ✅ 请假管理 (leave_request)

### 归档模块
- 📦 毕业审核 (archive/schema_graduation.sql)
- 📦 奖学金 (archive/schema_scholarship.sql)
- 📦 学费管理 (archive/tuition/)

## ⚙️ 技术栈

- **数据库**: MySQL 8.0+
- **迁移工具**: Flyway 9.x
- **字符集**: UTF-8 (utf8mb4)
- **引擎**: InnoDB
- **排序规则**: utf8mb4_unicode_ci

## 🔧 常用命令

### Flyway 命令
```bash
# 迁移到最新版本
mvn flyway:migrate

# 查看迁移状态
mvn flyway:info

# 验证迁移
mvn flyway:validate

# 修复失败的迁移
mvn flyway:repair

# 清空数据库（谨慎使用！）
mvn flyway:clean
```

### MySQL 命令
```bash
# 连接数据库
mysql -u root -p academic_system

# 查看所有表
SHOW TABLES;

# 查看迁移历史
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

# 导出数据库结构
mysqldump -u root -p --no-data academic_system > schema.sql

# 导出数据库数据
mysqldump -u root -p academic_system > backup.sql
```

## 📝 文件组织规则

### 迁移文件命名
格式：`V{version}__{description}.sql`

示例：
- ✅ `V10__create_exam_tables.sql`
- ✅ `V20__add_grade_point_column.sql`
- ❌ `V10_create_table.sql` (单下划线)
- ❌ `add_column.sql` (无版本号)

### 测试数据命名
格式：`{module}_test_data.sql`

示例：
- ✅ `classroom_test_data.sql`
- ✅ `exam_test_data.sql`
- ✅ `evaluation_test_data.sql`

## 🛡️ 安全注意事项

### ⚠️ 生产环境
1. **禁止执行测试数据**：test-data 目录中的所有文件仅用于开发/测试
2. **备份优先**：执行迁移前必须备份数据库
3. **逐步发布**：重要变更分多个小版本逐步发布
4. **权限控制**：限制数据库用户的 DDL 权限

### 🔒 数据保护
1. **软删除**：使用 `deleted` 标志而非直接删除数据
2. **审计日志**：重要操作记录在 `operation_log` 表
3. **敏感数据**：密码使用 BCrypt 加密
4. **定期备份**：建议每日备份数据库

## 🐛 故障排查

### 常见问题

**Q1: 迁移校验和不匹配**
```
A: 已执行的迁移脚本被修改。解决：不要修改已执行的脚本，创建新版本。
```

**Q2: 外键约束错误**
```
A: 表依赖关系问题。解决：检查迁移顺序，确保被引用的表已存在。
```

**Q3: 字符集问题**
```
A: 编码不一致。解决：确保数据库、表、连接都使用 utf8mb4。
```

详细故障排查请参考：[MIGRATION_GUIDE.md](MIGRATION_GUIDE.md#故障排查)

## 📞 支持与反馈

- **技术支持**: traceck1@gmail.com
- **问题反馈**: [GitHub Issues](https://github.com/tangqingfeng7/university-academic-system/issues)
- **文档更新**: 欢迎提交 Pull Request

## 📅 更新日志

- **2025-10-13**: 整理数据库文件结构，创建完整文档
  - 解决迁移版本冲突（V20, V21）
  - 归档未版本化的 SQL 文件
  - 整理测试数据，移动废弃文件
  - 创建三份详细文档（README, MIGRATION_GUIDE, 模块 README）

- **2025-10-12**: 添加智能排课模块 (V16, V28)

- **2025-10-10**: 添加教室管理和审批增强 (V13-V14)

- **2025-10-09**: 添加考试和评价模块 (V10-V12)

---

**版本**: 1.0  
**维护**: traceck1@gmail.com  
**最后更新**: 2025-10-13

