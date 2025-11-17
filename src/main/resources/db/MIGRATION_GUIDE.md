# 数据库迁移指南

本文档提供使用Flyway进行数据库迁移的完整指南。

## 快速导航

- [测试数据使用指南](test-data/README.md)

## Flyway配置说明

### 配置文件

Flyway配置位于 `application.yml` 和 `application-dev.yml`：

```yaml
spring:
  flyway:
    enabled: true                    # 启用Flyway
    baseline-on-migrate: true        # 首次迁移时创建基线
    locations: classpath:db/migration # 迁移脚本位置
    encoding: UTF-8                   # 脚本编码
    validate-on-migrate: true         # 迁移前验证
    out-of-order: false              # 不允许乱序执行
```

### 迁移文件命名规范

格式：`V{version}__{description}.sql`

- **V**: 固定前缀，表示版本化迁移
- **{version}**: 版本号，使用数字（如1, 2, 10, 11）
- **__**: 双下划线分隔符
- **{description}**: 描述，使用英文和下划线

示例：
- ✅ `V1__init_schema.sql`
- ✅ `V2__add_user_table.sql`
- ✅ `V10__create_exam_tables.sql`
- ❌ `V1_init.sql` (单下划线)
- ❌ `init_schema.sql` (无版本号)

### 常用Flyway命令

```bash
# 执行数据库迁移
mvn flyway:migrate

# 查看迁移状态和历史
mvn flyway:info

# 验证已应用的迁移
mvn flyway:validate

# 建立基线版本（用于已有数据库）
mvn flyway:baseline

# 修复迁移记录
mvn flyway:repair

# 清空数据库（谨慎使用！仅开发环境）
mvn flyway:clean
```

## 数据库架构演进历史

### 第一阶段：基础架构
**包含内容**：
- 用户、院系、专业、学生、教师基础表
- 课程、学期、选课、成绩管理
- 系统配置、操作日志

---

### 第二阶段：考试与审批模块
**新增功能**：
- 考试管理增强
- 调课申请流程
- 学籍异动管理
- 完整考试系统（考场、监考、座位）
- 通用审批流程框架

---

### 第三阶段：评价与资源管理
**新增功能**：
- 教学评价系统（课程评价、教师评价）
- 教室资源管理（教室借用、教室统计）
- 审批流程修复（外键完整性）
- 学生请假申请
- 智能排课系统（排课约束、教师偏好、排课方案）

---

### 第四阶段：数据完善与标准化
**改进内容**：
- 统一所有表的 `updated_at` 字段和表注释
- 成绩绩点计算自动化（触发器）

---

### 第五阶段：班级与处分管理
**新增功能**：
- 班级管理系统（班级表、数据标准化）
- 学生处分管理（处分记录、申诉、审批流程）
- 表注释补充
- 排课结果详情表

---

## 模块功能矩阵

| 模块 | 主要表 | 状态 |
|-----|-------|------|
| 用户管理 | sys_user | ✅ 稳定 |
| 组织架构 | department, major | ✅ 稳定 |
| 学生管理 | student, student_status_change | ✅ 稳定 |
| 教师管理 | teacher | ✅ 稳定 |
| 课程管理 | course, course_offering, course_prerequisite | ✅ 稳定 |
| 选课管理 | course_selection | ✅ 稳定 |
| 成绩管理 | grade | ✅ 稳定 |
| 考试管理 | exam, exam_room, exam_invigilator | ✅ 稳定 |
| 调课申请 | course_change_request | ✅ 稳定 |
| 审批流程 | approval_configuration, approval_level_approver | ✅ 稳定 |
| 教学评价 | course_evaluation, teacher_evaluation, evaluation_period | ✅ 稳定 |
| 教室管理 | classroom, classroom_booking | ✅ 稳定 |
| 请假管理 | leave_request | ✅ 稳定 |
| 智能排课 | scheduling_constraint, scheduling_solution, teacher_preference, schedule_item | ✅ 稳定 |
| 班级管理 | class | ✅ 稳定 |
| 处分管理 | student_discipline, discipline_appeal | ✅ 稳定 |
| 考勤管理 | attendance_record, attendance_detail, attendance_statistics, attendance_warning | ✅ 稳定 |

## 数据库管理最佳实践

### ✅ 推荐做法

1. **描述清晰**：SQL脚本添加清楚的注释说明变更内容
2. **向后兼容**：避免破坏性变更（如删除列）
3. **幂等性**：使用 `IF NOT EXISTS` 等条件语句
4. **分步变更**：复杂变更拆分为多个小步骤
5. **先测试**：在开发/测试环境充分验证后再发布
6. **定期备份**：生产环境执行变更前必须备份

### ❌ 避免的做法

1. **直接删除表/列**：考虑软删除或分步废弃
2. **生产环境直接测试**：先在测试环境验证
3. **混合 DDL 和大量 DML**：数据变更单独处理
4. **忽略外键约束**：确保数据完整性

## 故障排查

### 问题1：外键约束错误

**原因**：表之间依赖关系不正确

**解决方案**：
1. 检查SQL脚本中的外键定义
2. 确保被引用的表已存在
3. 临时禁用外键检查（仅开发环境）：
   ```sql
   SET FOREIGN_KEY_CHECKS = 0;
   -- 执行SQL
   SET FOREIGN_KEY_CHECKS = 1;
   ```

### 问题2：字符集问题

**原因**：编码不一致

**解决方案**：
- 确保数据库、表、连接都使用 utf8mb4
- 创建表时明确指定字符集：
  ```sql
  CREATE TABLE xxx (...) 
  ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci;
  ```

## 相关资源

- **MySQL 8.0 文档**: https://dev.mysql.com/doc/refman/8.0/en/
- **项目部署指南**: [../../../DEPLOY.md](../../../DEPLOY.md)
- **测试数据指南**: [test-data/README.md](test-data/README.md)

## 维护团队

如有疑问或需要支持，请联系：
- **数据库管理**: traceck1@gmail.com
- **技术支持**: [GitHub Issues](https://github.com/tangqingfeng7/university-academic-system/issues)

## 使用Flyway的最佳实践

### ✅ 推荐做法

1. **版本号连续**：使用连续的版本号，避免跳跃
2. **描述清晰**：文件名和脚本注释清楚说明变更内容
3. **向后兼容**：避免破坏性变更（如删除列）
4. **幂等性**：使用 `IF NOT EXISTS` 等条件语句
5. **分步迁移**：复杂变更拆分为多个小版本
6. **先测试**：在开发/测试环境充分验证后再发布
7. **不修改已执行的脚本**：会导致校验和不匹配

### ❌ 避免的做法

1. **修改已执行的脚本**：会导致校验和不匹配错误
2. **直接删除表/列**：考虑软删除或分步废弃
3. **跳过版本号**：保持版本连续性
4. **生产环境直接测试**：先在测试环境验证
5. **混合 DDL 和大量 DML**：数据迁移单独处理

## Flyway故障排查

### 问题1：校验和不匹配

**错误信息**：
```
Migration checksum mismatch for migration version X
```

**原因**：已执行的迁移脚本被修改

**解决方案**：
```bash
# 选项1：修复校验和（确认修改无误后）
mvn flyway:repair

# 选项2：回滚修改，创建新版本脚本
```

### 问题2：迁移失败

**解决方案**：
1. 查看错误日志确定原因
2. 手动修复数据库问题
3. 使用 `flyway:repair` 清除失败记录
4. 重新执行 `flyway:migrate`

### 问题3：首次使用Flyway（数据库已有表）

**解决方案**：
```bash
# 建立基线版本
mvn flyway:baseline

# 之后新的迁移脚本会正常执行
mvn flyway:migrate
```

---

**最后更新**: 2025-11-17  
**维护者**: traceck1@gmail.com  
**版本**: 3.0
