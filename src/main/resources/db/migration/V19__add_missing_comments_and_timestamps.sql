-- =================================================================
-- 为所有表添加缺失的注释和时间戳字段
-- Version: V19
-- Description: 统一为所有表添加 updated_at 字段和表注释
-- Author: Academic System Team
-- Date: 2025-10-13
-- =================================================================

-- 创建临时存储过程
DELIMITER //

CREATE PROCEDURE add_updated_at_column()
BEGIN
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;
    
    -- 审批配置表
    ALTER TABLE `approval_configuration` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `approval_configuration` COMMENT='审批配置表';
    
    -- 审批级别批准人表
    ALTER TABLE `approval_level_approver` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `approval_level_approver` COMMENT='审批级别批准人表';
    
    -- 教室表
    ALTER TABLE `classroom` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `classroom` COMMENT='教室资源表';
    
    -- 教室借用表
    ALTER TABLE `classroom_booking` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `classroom_booking` COMMENT='教室借用申请表';
    
    -- 课程表
    ALTER TABLE `course` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `course` COMMENT='课程基本信息表';
    
    -- 调课申请表
    ALTER TABLE `course_change_request` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `course_change_request` COMMENT='调课申请表';
    
    -- 课程评价表
    ALTER TABLE `course_evaluation` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `course_evaluation` COMMENT='课程评价表';
    
    -- 开课计划表
    ALTER TABLE `course_offering` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `course_offering` COMMENT='开课计划表';
    
    -- 先修课程表
    ALTER TABLE `course_prerequisite` ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
    ALTER TABLE `course_prerequisite` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `course_prerequisite` COMMENT='先修课程关系表';
    
    -- 选课记录表
    ALTER TABLE `course_selection` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `course_selection` COMMENT='选课记录表';
    
    -- 院系表
    ALTER TABLE `department` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `department` COMMENT='院系信息表';
    
    -- 评价周期表
    ALTER TABLE `evaluation_period` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `evaluation_period` COMMENT='教学评价周期表';
    
    -- 考试表
    ALTER TABLE `exam` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `exam` COMMENT='考试信息表';
    
    -- 监考安排表
    ALTER TABLE `exam_invigilator` ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
    ALTER TABLE `exam_invigilator` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `exam_invigilator` COMMENT='考试监考安排表';
    
    -- 考场表
    ALTER TABLE `exam_room` ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
    ALTER TABLE `exam_room` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `exam_room` COMMENT='考场安排表';
    
    -- 考场学生表
    ALTER TABLE `exam_room_student` ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
    ALTER TABLE `exam_room_student` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `exam_room_student` COMMENT='考场学生座位表';
    
    -- 成绩表
    ALTER TABLE `grade` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `grade` COMMENT='学生成绩表';
    
    -- 毕业审核表
    ALTER TABLE `graduation_audit` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `graduation_audit` COMMENT='毕业审核记录表';
    
    -- 毕业要求表
    ALTER TABLE `graduation_requirement` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `graduation_requirement` COMMENT='专业毕业要求表';
    
    -- 请假申请表
    ALTER TABLE `leave_request` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `leave_request` COMMENT='学生请假申请表';
    
    -- 专业表
    ALTER TABLE `major` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `major` COMMENT='专业信息表';
    
    -- 通知表
    ALTER TABLE `notification` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `notification` COMMENT='系统通知公告表';
    
    -- 通知阅读记录表
    ALTER TABLE `notification_read` ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
    ALTER TABLE `notification_read` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `notification_read` COMMENT='通知阅读记录表';
    
    -- 操作日志表
    ALTER TABLE `operation_log` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `operation_log` COMMENT='系统操作日志表';
    
    -- 缴费提醒表
    ALTER TABLE `payment_reminder` ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
    ALTER TABLE `payment_reminder` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `payment_reminder` COMMENT='学费缴费提醒表';
    
    -- 退款申请表
    ALTER TABLE `refund_application` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `refund_application` COMMENT='退款申请表';
    
    -- 退款审批表
    ALTER TABLE `refund_approval` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `refund_approval` COMMENT='退款审批记录表';
    
    -- 奖学金表
    ALTER TABLE `scholarship` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `scholarship` COMMENT='奖学金类型表';
    
    -- 奖学金申请表
    ALTER TABLE `scholarship_application` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `scholarship_application` COMMENT='奖学金申请表';
    
    -- 奖学金审批表
    ALTER TABLE `scholarship_approval` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `scholarship_approval` COMMENT='奖学金审批记录表';
    
    -- 奖学金获奖记录表
    ALTER TABLE `scholarship_award` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `scholarship_award` COMMENT='奖学金获奖记录表';
    
    -- 学期表
    ALTER TABLE `semester` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `semester` COMMENT='学期信息表';
    
    -- 学籍异动审批表
    ALTER TABLE `status_change_approval` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `status_change_approval` COMMENT='学籍异动审批记录表';
    
    -- 学生表
    ALTER TABLE `student` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `student` COMMENT='学生基本信息表';
    
    -- 学生学分汇总表
    ALTER TABLE `student_credit_summary` ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
    ALTER TABLE `student_credit_summary` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `student_credit_summary` COMMENT='学生学分汇总表';
    
    -- 学籍异动表
    ALTER TABLE `student_status_change` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `student_status_change` COMMENT='学生学籍异动表';
    
    -- 用户表
    ALTER TABLE `sys_user` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `sys_user` COMMENT='系统用户表';
    
    -- 系统配置表
    ALTER TABLE `system_config` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `system_config` COMMENT='系统配置表';
    
    -- 教师表
    ALTER TABLE `teacher` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `teacher` COMMENT='教师基本信息表';
    
    -- 教师评价表
    ALTER TABLE `teacher_evaluation` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `teacher_evaluation` COMMENT='教师评价表';
    
    -- 学费账单表
    ALTER TABLE `tuition_bill` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `tuition_bill` COMMENT='学费账单表';
    
    -- 学费缴费记录表
    ALTER TABLE `tuition_payment` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `tuition_payment` COMMENT='学费缴费记录表';
    
    -- 学费标准表
    ALTER TABLE `tuition_standard` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `tuition_standard` COMMENT='学费标准配置表';
    
    -- 用户通知表
    ALTER TABLE `user_notification` ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
    ALTER TABLE `user_notification` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
    ALTER TABLE `user_notification` COMMENT='用户通知关联表';
END//

DELIMITER ;

-- 调用存储过程
CALL add_updated_at_column();

-- 删除存储过程
DROP PROCEDURE IF EXISTS add_updated_at_column;
