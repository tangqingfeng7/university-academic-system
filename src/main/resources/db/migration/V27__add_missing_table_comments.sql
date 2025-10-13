-- =================================================================
-- 为缺失注释的表添加注释
-- Version: V21
-- Description: 为 refund_approval 和 schedule_item 表添加注释
-- Author: Academic System Team
-- Date: 2025-10-13
-- =================================================================

-- 退款审批记录表注释
ALTER TABLE `refund_approval` COMMENT='退款审批记录表';

-- 排课结果表注释
ALTER TABLE `schedule_item` COMMENT='排课结果表';

