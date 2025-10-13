-- =================================================================
-- 排课优化模块测试数据
-- Description: 为排课优化模块插入测试数据
-- Author: Academic System Team
-- Date: 2025-10-12
-- =================================================================

-- -----------------------------------------------------------------
-- 1. 清空现有测试数据（如果存在）
-- -----------------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;
-- TRUNCATE TABLE `teacher_preference`;
-- TRUNCATE TABLE `scheduling_solution`;
-- 注意：不清空 scheduling_constraint，因为它包含系统预置的约束
SET FOREIGN_KEY_CHECKS = 1;

-- -----------------------------------------------------------------
-- 2. 插入教师排课偏好测试数据
-- -----------------------------------------------------------------
-- 假设教师ID从1开始，插入几个教师的偏好设置

-- 教师1的偏好：喜欢周一到周五上课，偏好上午时段
INSERT INTO `teacher_preference` (`teacher_id`, `preferred_days`, `preferred_time_slots`, `max_daily_hours`, `max_weekly_hours`, `notes`) 
VALUES 
(1, '1,2,3,4,5', '1,2', 8, 16, '偏好上午上课，每天最多8节课'),
(2, '1,3,5', '2,3', 6, 12, '周一、三、五有空，下午时段较好'),
(3, '2,4', '1,2,3', 8, 16, '周二、四有空，上午和下午都可以'),
(4, '1,2,3,4,5', '3,4', 10, 20, '下午时段优先，可以接受较多课时'),
(5, '1,2,3', '1,2', 6, 12, '周一到周三，上午时段');

-- -----------------------------------------------------------------
-- 3. 插入排课方案测试数据
-- -----------------------------------------------------------------
-- 假设学期ID为9（2024-2025学年第一学期）

-- 方案1：草稿状态
INSERT INTO `scheduling_solution` (`semester_id`, `name`, `quality_score`, `conflict_count`, `status`, `generated_at`, `applied_at`) 
VALUES 
(9, '2024-2025第一学期排课方案（草稿）', NULL, 0, 'DRAFT', NULL, NULL);

-- 方案2：已完成状态（有质量分数和冲突数）
INSERT INTO `scheduling_solution` (`semester_id`, `name`, `quality_score`, `conflict_count`, `status`, `generated_at`, `applied_at`) 
VALUES 
(9, '2024-2025第一学期排课方案V1', 85.6, 3, 'COMPLETED', NOW() - INTERVAL 2 DAY, NULL);

-- 方案3：已完成状态（更优的方案）
INSERT INTO `scheduling_solution` (`semester_id`, `name`, `quality_score`, `conflict_count`, `status`, `generated_at`, `applied_at`) 
VALUES 
(9, '2024-2025第一学期排课方案V2（优化版）', 92.3, 1, 'COMPLETED', NOW() - INTERVAL 1 DAY, NULL);

-- 方案4：已应用状态
INSERT INTO `scheduling_solution` (`semester_id`, `name`, `quality_score`, `conflict_count`, `status`, `generated_at`, `applied_at`) 
VALUES 
(9, '2024-2025第一学期正式排课方案', 90.5, 0, 'APPLIED', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 2 DAY);

-- 如果有其他学期，可以添加更多方案
-- 方案5：2023-2024第二学期的方案（假设学期ID为8）
INSERT INTO `scheduling_solution` (`semester_id`, `name`, `quality_score`, `conflict_count`, `status`, `generated_at`, `applied_at`) 
VALUES 
(8, '2023-2024第二学期排课方案', 88.7, 2, 'APPLIED', NOW() - INTERVAL 180 DAY, NOW() - INTERVAL 179 DAY);

-- -----------------------------------------------------------------
-- 4. 验证数据插入
-- -----------------------------------------------------------------
-- 查询插入的数据
SELECT '=== 排课约束数量 ===' AS 'Info';
SELECT COUNT(*) AS constraint_count FROM `scheduling_constraint`;

SELECT '=== 教师偏好数量 ===' AS 'Info';
SELECT COUNT(*) AS preference_count FROM `teacher_preference`;

SELECT '=== 排课方案数量 ===' AS 'Info';
SELECT COUNT(*) AS solution_count FROM `scheduling_solution`;

SELECT '=== 排课方案详情 ===' AS 'Info';
SELECT 
    id,
    semester_id,
    name,
    quality_score,
    conflict_count,
    status,
    DATE_FORMAT(generated_at, '%Y-%m-%d %H:%i') AS generated_at,
    DATE_FORMAT(applied_at, '%Y-%m-%d %H:%i') AS applied_at
FROM `scheduling_solution`
ORDER BY semester_id DESC, id DESC;

-- =================================================================
-- 测试数据插入完成
-- 说明：
-- 1. 已插入12条预置的排课约束
-- 2. 已插入5个教师的排课偏好示例
-- 3. 已插入5个排课方案示例（涵盖不同状态）
-- =================================================================

