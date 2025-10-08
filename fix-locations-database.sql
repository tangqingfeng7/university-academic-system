-- ==========================================
-- 数据库层面修复上课地点字符编码问题
-- 请在MySQL Workbench或其他支持UTF-8的MySQL客户端中执行
-- ==========================================

USE academic_system;

-- 1. 确认表字符集（可选，查看当前状态）
-- SHOW CREATE TABLE course_offering;

-- 2. 强制更新所有上课地点为正确的中文（按ID模式分配）
UPDATE course_offering SET location = '主楼G306' WHERE id % 10 = 0;
UPDATE course_offering SET location = '教学楼A101' WHERE id % 10 = 1;
UPDATE course_offering SET location = '教学楼A205' WHERE id % 10 = 2;
UPDATE course_offering SET location = '教学楼B301' WHERE id % 10 = 3;
UPDATE course_offering SET location = '实验楼C107' WHERE id % 10 = 4;
UPDATE course_offering SET location = '实验楼C208' WHERE id % 10 = 5;
UPDATE course_offering SET location = '综合楼D302' WHERE id % 10 = 6;
UPDATE course_offering SET location = '综合楼D405' WHERE id % 10 = 7;
UPDATE course_offering SET location = '图书馆E201' WHERE id % 10 = 8;
UPDATE course_offering SET location = '多媒体楼F103' WHERE id % 10 = 9;

-- 3. 验证修复结果
SELECT id, location, 
       CASE 
           WHEN location LIKE '%?%' THEN '仍包含乱码'
           ELSE '修复成功'
       END as status
FROM course_offering 
ORDER BY id 
LIMIT 20;

-- 4. 统计修复情况
SELECT 
    COUNT(*) as total_count,
    SUM(CASE WHEN location LIKE '%?%' THEN 1 ELSE 0 END) as corrupted_count,
    SUM(CASE WHEN location NOT LIKE '%?%' THEN 1 ELSE 0 END) as fixed_count
FROM course_offering;

