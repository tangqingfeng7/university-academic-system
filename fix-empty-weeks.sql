-- 修复 course_offering 表中 schedule 字段的空 weeks 问题
-- 将所有 "weeks": [] 替换为默认的 1-16 周

-- 查看有问题的记录
SELECT id, course_id, schedule 
FROM course_offering 
WHERE schedule LIKE '%"weeks": []%';

-- 修复方法：将空的 weeks 数组替换为默认值 [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]
UPDATE course_offering 
SET schedule = REPLACE(
    schedule, 
    '"weeks": []', 
    '"weeks": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16]'
)
WHERE schedule LIKE '%"weeks": []%';

-- 验证修复结果
SELECT id, course_id, schedule 
FROM course_offering 
WHERE schedule LIKE '%"weeks"%'
ORDER BY id;

