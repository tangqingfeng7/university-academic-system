-- =================================================================
-- 排课优化模块数据库迁移脚本
-- Version: V16
-- Description: 创建排课约束、排课方案、教师偏好表
-- Author: Academic System Team
-- Date: 2025-10-12
-- =================================================================

-- -----------------------------------------------------------------
-- 1. 排课约束表
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `scheduling_constraint` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` VARCHAR(100) NOT NULL COMMENT '约束名称',
    `type` VARCHAR(20) NOT NULL COMMENT '约束类型：HARD-硬约束，SOFT-软约束',
    `description` TEXT COMMENT '约束描述',
    `weight` INT COMMENT '权重（仅软约束使用，1-100）',
    `active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_constraint_name` (`name`),
    KEY `idx_type_active` (`type`, `active`),
    KEY `idx_weight` (`weight`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排课约束表';

-- -----------------------------------------------------------------
-- 2. 排课方案表
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `scheduling_solution` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `semester_id` BIGINT NOT NULL COMMENT '学期ID',
    `name` VARCHAR(100) NOT NULL COMMENT '方案名称',
    `quality_score` DOUBLE COMMENT '质量分数',
    `conflict_count` INT NOT NULL DEFAULT 0 COMMENT '冲突数量',
    `status` VARCHAR(20) NOT NULL COMMENT '状态：DRAFT-草稿，OPTIMIZING-排课中，COMPLETED-已完成，APPLIED-已应用',
    `generated_at` DATETIME COMMENT '生成时间',
    `applied_at` DATETIME COMMENT '应用时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_semester` (`semester_id`),
    KEY `idx_status` (`status`),
    KEY `idx_semester_status` (`semester_id`, `status`),
    CONSTRAINT `fk_solution_semester` FOREIGN KEY (`semester_id`) REFERENCES `semester` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排课方案表';

-- -----------------------------------------------------------------
-- 3. 教师排课偏好表
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `teacher_preference` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `teacher_id` BIGINT NOT NULL COMMENT '教师ID',
    `preferred_days` VARCHAR(50) COMMENT '偏好星期（逗号分隔：1,2,3,4,5）',
    `preferred_time_slots` VARCHAR(100) COMMENT '偏好时段（逗号分隔：1,2,3,4,5）',
    `max_daily_hours` INT COMMENT '每天最多课时',
    `max_weekly_hours` INT COMMENT '每周最多课时',
    `notes` TEXT COMMENT '备注说明',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_teacher` (`teacher_id`),
    CONSTRAINT `fk_preference_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师排课偏好表';

-- -----------------------------------------------------------------
-- 4. 插入默认的排课约束
-- -----------------------------------------------------------------
INSERT INTO `scheduling_constraint` (`name`, `type`, `description`, `weight`, `active`) VALUES
('教师时间不冲突', 'HARD', '同一教师不能在同一时间段教授多门课程', NULL, 1),
('教室时间不冲突', 'HARD', '同一教室不能在同一时间段安排多门课程', NULL, 1),
('学生课表不冲突', 'HARD', '学生的课程时间不能冲突', NULL, 1),
('教室容量限制', 'HARD', '课程人数不能超过教室容量', NULL, 1),
('教师偏好星期', 'SOFT', '尽量安排在教师偏好的星期上课', 80, 1),
('教师偏好时段', 'SOFT', '尽量安排在教师偏好的时段上课', 70, 1),
('教师每天课时限制', 'SOFT', '尽量不超过教师设定的每天最多课时', 90, 1),
('教师每周课时限制', 'SOFT', '尽量不超过教师设定的每周最多课时', 85, 1),
('避免连续多节课', 'SOFT', '尽量避免学生连续上3节以上同一门课', 60, 1),
('避免孤立单节课', 'SOFT', '尽量避免单独一节课，建议连续2节', 50, 1),
('均衡分布', 'SOFT', '课程尽量在一周内均匀分布', 40, 1),
('教室利用率', 'SOFT', '提高教室利用率，减少闲置', 30, 1);

-- -----------------------------------------------------------------
-- 5. 添加索引说明和优化
-- -----------------------------------------------------------------
-- 为了提高查询性能，已创建以下索引：
-- scheduling_constraint:
--   - uk_constraint_name: 约束名称唯一索引
--   - idx_type_active: 按类型和状态查询的复合索引
--   - idx_weight: 权重索引（用于排序）
--
-- scheduling_solution:
--   - idx_semester: 学期索引
--   - idx_status: 状态索引
--   - idx_semester_status: 学期+状态复合索引（常用查询）
--
-- teacher_preference:
--   - uk_teacher: 教师唯一索引（每个教师只能有一条偏好设置）

-- =================================================================
-- 脚本执行完成
-- =================================================================

