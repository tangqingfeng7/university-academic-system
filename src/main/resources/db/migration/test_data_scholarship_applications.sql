-- 奖学金申请测试数据
-- 为已有的学生创建奖学金申请

-- 首先查看现有学生和奖学金数据
-- 假设student表中已有学生数据，我们为前几个学生创建申请

-- 插入奖学金申请（使用学生ID 3-8，奖学金ID 1-8）
INSERT INTO scholarship_application (
    scholarship_id, 
    student_id, 
    academic_year, 
    gpa, 
    total_credits, 
    comprehensive_score,
    personal_statement, 
    status, 
    submitted_at,
    created_at
) VALUES
-- 国家奖学金申请（ID=1）
(1, 3, '2024-2025', 3.85, 125.0, 92.5, '我是一名品学兼优的学生，在过去的学年中取得了优异的成绩。我积极参加各类学术竞赛和社会实践活动，曾获得省级数学建模竞赛一等奖。我希望能够获得国家奖学金的荣誉，这将是对我努力的最大肯定。', 'PENDING', NOW(), NOW()),

(1, 4, '2024-2025', 3.90, 130.0, 94.0, '作为一名计算机专业的学生，我在学业上一直保持优秀。同时我也热心公益，担任学生会干部，组织过多次志愿服务活动。获得国家奖学金是我的目标，我会继续努力，为学校争光。', 'PENDING', NOW(), NOW()),

-- 国家励志奖学金申请（ID=2）
(2, 5, '2024-2025', 3.65, 115.0, 88.0, '我来自农村家庭，家庭经济困难，但我从未放弃对知识的追求。通过勤工俭学和努力学习，我取得了不错的成绩。希望能获得国家励志奖学金，减轻家庭负担，让我更专注于学业。', 'PENDING', NOW(), NOW()),

(2, 6, '2024-2025', 3.70, 118.0, 89.5, '虽然家境贫寒，但我始终保持乐观向上的态度。在学习之余，我还通过兼职工作帮助家里分担经济压力。我相信通过自己的努力，一定能够改变命运。', 'COUNSELOR_APPROVED', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),

-- 省政府奖学金申请（ID=3）
(3, 7, '2024-2025', 3.75, 120.0, 90.0, '我在科研方面有一定的成果，参与了导师的国家级科研项目，并在核心期刊上发表了一篇论文。希望能够获得省政府奖学金，继续在科研道路上前进。', 'PENDING', NOW(), NOW()),

-- 校级一等奖学金申请（ID=4）
(4, 8, '2024-2025', 3.60, 110.0, 87.0, '在过去的学年中，我的成绩保持在班级前列，同时我也积极参与学校的各类活动，担任班级学习委员，帮助同学们共同进步。', 'PENDING', NOW(), NOW()),

(4, 9, '2024-2025', 3.68, 112.0, 88.5, '我热爱学习，热爱生活。在学好专业课的同时，我也培养了广泛的兴趣爱好，参加了学校的文艺社团，在校园文化建设中贡献了自己的力量。', 'PENDING', NOW(), NOW()),

-- 校级二等奖学金申请（ID=5）
(5, 10, '2024-2025', 3.35, 95.0, 82.0, '虽然我的成绩不是最优秀的，但我一直在努力进步。这学期我的成绩相比上学期有了明显提升，我会继续保持这种上升的势头。', 'PENDING', NOW(), NOW()),

(5, 11, '2024-2025', 3.40, 98.0, 83.5, '我是一个踏实认真的学生，课堂上认真听讲，课后及时复习。我相信只要坚持不懈，一定能够取得更好的成绩。', 'COUNSELOR_APPROVED', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),

-- 校级三等奖学金申请（ID=6）
(6, 12, '2024-2025', 3.15, 85.0, 78.0, '我的学习成绩虽然一般，但我有自己的特长。我在体育方面表现突出，代表学院参加了校运动会，为学院争得了荣誉。', 'PENDING', NOW(), NOW()),

-- 院系优秀奖学金申请（ID=7）
(7, 13, '2024-2025', 3.25, 88.0, 80.0, '作为一名普通学生，我努力做好自己的本职工作。在宿舍中我是舍长，帮助维护宿舍和谐，营造良好的学习氛围。', 'PENDING', NOW(), NOW()),

-- 创新创业奖学金申请（ID=8）
(8, 14, '2024-2025', 3.45, 100.0, 85.0, '我对创新创业充满热情，参加了学校的创新创业大赛，我们团队的项目获得了省级三等奖。我希望能够获得创新创业奖学金的支持，将我们的项目进一步完善。', 'PENDING', NOW(), NOW()),

(8, 15, '2024-2025', 3.50, 102.0, 86.0, '我和我的团队开发了一个校园服务小程序，已经在学校内推广使用，获得了广大师生的好评。我们希望通过获得奖学金的资助，进一步扩大项目规模。', 'COUNSELOR_APPROVED', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),

-- 再添加一些已经完成审批的申请（用于测试审批历史）
(1, 16, '2023-2024', 3.92, 128.0, 95.0, '上一学年的优秀表现', 'DEPT_APPROVED', NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY),

(2, 17, '2023-2024', 3.72, 120.0, 90.0, '家庭困难但成绩优秀', 'DEPT_APPROVED', NOW() - INTERVAL 25 DAY, NOW() - INTERVAL 25 DAY),

(3, 18, '2023-2024', 3.68, 115.0, 88.0, '科研成果突出', 'REJECTED', NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 20 DAY)
ON DUPLICATE KEY UPDATE student_id=student_id;

-- 为已通过辅导员审核的申请添加审批记录
INSERT INTO scholarship_approval (
    application_id,
    approval_level,
    approver_id,
    action,
    comment,
    approved_at,
    created_at
) VALUES
-- 假设审批记录使用实际申请的ID（这里使用子查询获取）
-- 由于我们不知道插入后的实际ID，这里使用相对较新的方式

-- 为student_id=6的申请添加辅导员审批（假设申请ID为对应的值）
-- 注意：实际运行时需要根据数据库中的实际ID调整
(
    (SELECT id FROM scholarship_application WHERE student_id = 6 AND academic_year = '2024-2025' LIMIT 1),
    1, -- 辅导员级别
    2, -- 假设教师用户ID为2（T001）
    'APPROVE',
    '该生成绩优秀，家庭确实困难，符合申请条件，同意推荐。',
    NOW() - INTERVAL 2 DAY,
    NOW() - INTERVAL 2 DAY
),

(
    (SELECT id FROM scholarship_application WHERE student_id = 11 AND academic_year = '2024-2025' LIMIT 1),
    1,
    2,
    'APPROVE',
    '该生学习态度端正，成绩进步明显，同意推荐。',
    NOW() - INTERVAL 1 DAY,
    NOW() - INTERVAL 1 DAY
),

(
    (SELECT id FROM scholarship_application WHERE student_id = 15 AND academic_year = '2024-2025' LIMIT 1),
    1,
    2,
    'APPROVE',
    '该生创新能力强，项目有实际价值，强烈推荐。',
    NOW() - INTERVAL 3 DAY,
    NOW() - INTERVAL 3 DAY
)
ON DUPLICATE KEY UPDATE approver_id=approver_id;

