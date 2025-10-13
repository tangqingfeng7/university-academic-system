-- 创建排课结果表
-- 该表存储排课方案的详细排课结果，包括每门课程的具体时间和教室安排

CREATE TABLE schedule_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    solution_id BIGINT NOT NULL COMMENT '排课方案ID',
    course_offering_id BIGINT NOT NULL COMMENT '开课ID',
    classroom_id BIGINT NOT NULL COMMENT '教室ID',
    day_of_week INT NOT NULL COMMENT '星期（1-7，1为周一）',
    start_slot INT NOT NULL COMMENT '开始时段（1-5）',
    end_slot INT NOT NULL COMMENT '结束时段（1-5）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    CONSTRAINT fk_schedule_item_solution FOREIGN KEY (solution_id) 
        REFERENCES scheduling_solution(id) ON DELETE CASCADE,
    CONSTRAINT fk_schedule_item_offering FOREIGN KEY (course_offering_id) 
        REFERENCES course_offering(id) ON DELETE CASCADE,
    CONSTRAINT fk_schedule_item_classroom FOREIGN KEY (classroom_id) 
        REFERENCES classroom(id) ON DELETE RESTRICT,
    
    -- 确保同一个方案中，同一门课程不会被重复排课
    CONSTRAINT uk_schedule_item_solution_offering 
        UNIQUE (solution_id, course_offering_id, day_of_week, start_slot),
    
    INDEX idx_schedule_item_solution (solution_id),
    INDEX idx_schedule_item_offering (course_offering_id),
    INDEX idx_schedule_item_classroom (classroom_id),
    INDEX idx_schedule_item_time (day_of_week, start_slot, end_slot)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排课结果表';

