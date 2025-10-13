# 数据库迁移指南

本文档提供数据库架构迁移的完整指南，包括迁移历史、执行顺序和版本说明。

## 快速导航

- [迁移文件详细说明](migration/README.md)
- [测试数据使用指南](test-data/README.md)

## 迁移版本总览

### 当前版本：V28
### 最后更新：2025-10-13

| 版本 | 文件名 | 描述 | 状态 |
|-----|-------|------|------|
| V5 | add_exam_room_version.sql | 添加考场版本号字段 | ✅ 已发布 |
| V6 | add_course_selection_enabled.sql | 添加选课启用标志 | ✅ 已发布 |
| V7 | create_course_change_request.sql | 创建调课申请表 | ✅ 已发布 |
| V8 | create_student_status_change_tables.sql | 创建学籍异动相关表 | ✅ 已发布 |
| V9 | add_student_status_column.sql | 添加学生状态列 | ✅ 已发布 |
| V10 | create_exam_tables.sql | 创建考试管理表 | ✅ 已发布 |
| V11 | create_approval_enhancement_tables.sql | 创建审批流程增强表 | ✅ 已发布 |
| V12 | create_evaluation_tables.sql | 创建教学评价表 | ✅ 已发布 |
| V13 | create_classroom_tables.sql | 创建教室管理表 | ✅ 已发布 |
| V14 | fix_approval_tables_foreign_keys.sql | 修复审批表外键约束 | ✅ 已发布 |
| V15 | create_leave_request_table.sql | 创建学生请假申请表 | ✅ 已发布 |
| V16 | create_scheduling_tables.sql | 创建智能排课模块表 | ✅ 已发布 |
| V17 | placeholder_reserved.sql | 预留版本号 | ⚠️ 占位符 |
| V18 | placeholder_reserved.sql | 预留版本号（原测试数据） | ⚠️ 占位符 |
| V19 | add_missing_comments_and_timestamps.sql | 统一添加表注释和时间戳 | ✅ 已发布 |
| V20 | add_grade_point_column.sql | 添加绩点字段及自动计算触发器 | ✅ 已发布 |
| V21 | placeholder_reserved.sql | 预留版本号 | ⚠️ 占位符 |
| V22 | create_class_table.sql | 创建班级管理表 | ✅ 已发布 |
| V23 | unify_class_names.sql | 统一班级名称规范 | ✅ 已发布 |
| V24 | populate_standard_classes.sql | 填充标准班级数据 | ✅ 已发布 |
| V25 | create_discipline_tables.sql | 创建学生处分管理表 | ✅ 已发布 |
| V26 | add_discipline_approval_status.sql | 添加处分审批状态字段 | ✅ 已发布 |
| V27 | add_missing_table_comments.sql | 补充缺失的表注释 | ✅ 已发布 |
| V28 | create_schedule_item_table.sql | 创建排课结果详情表 | ✅ 已发布 |

## 数据库架构演进历史

### 第一阶段：基础架构 (V1-V4)
**时间**：2024年初  
**包含内容**：
- 用户、院系、专业、学生、教师基础表
- 课程、学期、选课、成绩管理
- 系统配置、操作日志

**说明**：这些表在初始 `schema.sql` 中创建，未单独版本化

---

### 第二阶段：考试与审批模块 (V5-V11)
**时间**：2024年10月  
**新增功能**：
- **V5-V6**: 考试管理增强
- **V7**: 调课申请流程
- **V8-V9**: 学籍异动管理
- **V10**: 完整考试系统（考场、监考、座位）
- **V11**: 通用审批流程框架

---

### 第三阶段：评价与资源管理 (V12-V16)
**时间**：2024年10月  
**新增功能**：
- **V12**: 教学评价系统（课程评价、教师评价）
- **V13**: 教室资源管理（教室借用、教室统计）
- **V14**: 审批流程修复（外键完整性）
- **V15**: 学生请假申请
- **V16**: 智能排课系统（排课约束、教师偏好、排课方案）

---

### 第四阶段：数据完善与标准化 (V17-V21)
**时间**：2025年10月  
**改进内容**：
- **V17-V18**: 版本号预留（保持连续性）
- **V19**: 统一所有表的 `updated_at` 字段和表注释
- **V20**: 成绩绩点计算自动化（触发器）
- **V21**: 版本号预留

---

### 第五阶段：班级与处分管理 (V22-V28)
**时间**：2025年10月  
**新增功能**：
- **V22-V24**: 班级管理系统（班级表、数据标准化、批量填充）
- **V25-V26**: 学生处分管理（处分记录、申诉、审批流程）
- **V27**: 表注释补充（退款审批、排课结果）
- **V28**: 排课结果详情表（与 V16 配套）

---

## 模块功能矩阵

| 模块 | 主要表 | 相关迁移版本 | 状态 |
|-----|-------|------------|------|
| 用户管理 | sys_user | V1-V4 (schema.sql) | ✅ 稳定 |
| 组织架构 | department, major | V1-V4 (schema.sql) | ✅ 稳定 |
| 学生管理 | student, student_status_change | V1-V4, V8-V9 | ✅ 稳定 |
| 教师管理 | teacher | V1-V4 (schema.sql) | ✅ 稳定 |
| 课程管理 | course, course_offering, course_prerequisite | V1-V4 (schema.sql) | ✅ 稳定 |
| 选课管理 | course_selection | V1-V4, V6 | ✅ 稳定 |
| 成绩管理 | grade | V1-V4, V20 | ✅ 稳定 |
| 考试管理 | exam, exam_room, exam_invigilator | V5, V10 | ✅ 稳定 |
| 调课申请 | course_change_request | V7 | ✅ 稳定 |
| 审批流程 | approval_configuration, approval_level_approver | V11, V14 | ✅ 稳定 |
| 教学评价 | course_evaluation, teacher_evaluation, evaluation_period | V12 | ✅ 稳定 |
| 教室管理 | classroom, classroom_booking | V13 | ✅ 稳定 |
| 请假管理 | leave_request | V15 | ✅ 稳定 |
| 智能排课 | scheduling_constraint, scheduling_solution, teacher_preference, schedule_item | V16, V28 | ✅ 稳定 |
| 班级管理 | class | V22-V24 | ✅ 稳定 |
| 处分管理 | student_discipline, discipline_appeal | V25-V26 | ✅ 稳定 |
| 毕业审核 | graduation_audit, graduation_requirement | archive/ | 📦 归档 |
| 奖学金 | scholarship, scholarship_application | archive/ | 📦 归档 |
| 学费管理 | tuition_bill, tuition_payment, tuition_standard | archive/ | 📦 归档 |

## 执行迁移

### 1. 首次部署（全新数据库）

```bash
# 使用 Maven
mvn flyway:migrate

# 或使用 Spring Boot
mvn spring-boot:run
```

Flyway 会自动按版本号顺序执行所有迁移脚本。

### 2. 增量更新（已有数据库）

当有新的迁移脚本时：

```bash
# 查看待执行的迁移
mvn flyway:info

# 执行迁移
mvn flyway:migrate

# 验证迁移状态
mvn flyway:validate
```

### 3. 查看迁移历史

```sql
SELECT 
    installed_rank,
    version,
    description,
    type,
    script,
    installed_on,
    execution_time,
    success
FROM flyway_schema_history
ORDER BY installed_rank;
```

### 4. 回滚迁移

⚠️ **Flyway 社区版不支持自动回滚**

如需回滚，请：
1. 手动编写回滚 SQL
2. 使用数据库备份恢复
3. 考虑升级到 Flyway Teams 版本

## 最佳实践

### ✅ 推荐做法

1. **版本号连续**：使用连续的版本号，避免跳跃
2. **描述清晰**：文件名和脚本注释清楚说明变更内容
3. **向后兼容**：避免破坏性变更（如删除列）
4. **幂等性**：使用 `IF NOT EXISTS` 等条件语句
5. **分步迁移**：复杂变更拆分为多个小版本
6. **先测试**：在开发/测试环境充分验证后再发布

### ❌ 避免的做法

1. **修改已执行的脚本**：会导致校验和不匹配
2. **直接删除表/列**：考虑软删除或分步废弃
3. **跳过版本号**：保持版本连续性
4. **生产环境直接测试**：先在测试环境验证
5. **混合 DDL 和大量 DML**：数据迁移单独处理

## 故障排查

### 问题1：校验和不匹配

**错误信息**：
```
Migration checksum mismatch for migration version X
```

**原因**：已执行的迁移脚本被修改

**解决方案**：
```bash
# 选项1：修复校验和（不推荐）
mvn flyway:repair

# 选项2：回滚修改，创建新版本
```

---

### 问题2：迁移失败

**错误信息**：
```
Migration V20__xxx failed
```

**解决方案**：
1. 查看错误日志确定原因
2. 手动修复数据库问题
3. 使用 `flyway:repair` 清除失败记录
4. 重新执行 `flyway:migrate`

---

### 问题3：外键约束错误

**原因**：表之间依赖关系不正确

**解决方案**：
1. 检查迁移脚本中的外键定义
2. 确保被引用的表已存在
3. 临时禁用外键检查（仅开发环境）：
   ```sql
   SET FOREIGN_KEY_CHECKS = 0;
   -- 执行迁移
   SET FOREIGN_KEY_CHECKS = 1;
   ```

## 环境配置

### 开发环境 (application-dev.yml)

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    encoding: UTF-8
    validate-on-migrate: true
```

### 生产环境 (application-prod.yml)

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: false  # 生产环境建议设为 false
    locations: classpath:db/migration
    encoding: UTF-8
    validate-on-migrate: true
    out-of-order: false         # 严格按顺序执行
```

## 相关资源

- **Flyway 官方文档**: https://flywaydb.org/documentation/
- **MySQL 8.0 文档**: https://dev.mysql.com/doc/refman/8.0/en/
- **项目部署指南**: [../../../DEPLOY.md](../../../DEPLOY.md)
- **迁移文件详情**: [migration/README.md](migration/README.md)
- **测试数据指南**: [test-data/README.md](test-data/README.md)

## 维护团队

如有疑问或需要支持，请联系：
- **数据库管理**: traceck1@gmail.com
- **技术支持**: [GitHub Issues](https://github.com/tangqingfeng7/university-academic-system/issues)

---

**最后更新**: 2025-10-13  
**维护者**: traceck1@gmail.com  
**版本**: 1.0

