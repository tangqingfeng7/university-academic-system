-- =====================================================
-- 修正审批相关表的外键引用
-- 将 sys_user 修正为 user（根据实际表名）
-- =====================================================

-- 删除旧的外键约束（如果存在）
SET @sql = IF(
    EXISTS(
        SELECT 1 FROM information_schema.TABLE_CONSTRAINTS 
        WHERE CONSTRAINT_SCHEMA = DATABASE()
        AND TABLE_NAME = 'user_notification'
        AND CONSTRAINT_NAME = 'fk_user_notification_user'
    ),
    'ALTER TABLE user_notification DROP FOREIGN KEY fk_user_notification_user',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1 FROM information_schema.TABLE_CONSTRAINTS 
        WHERE CONSTRAINT_SCHEMA = DATABASE()
        AND TABLE_NAME = 'approval_level_approver'
        AND CONSTRAINT_NAME = 'fk_approval_level_approver_user'
    ),
    'ALTER TABLE approval_level_approver DROP FOREIGN KEY fk_approval_level_approver_user',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 重新添加正确的外键约束（引用user表）
ALTER TABLE user_notification 
    ADD CONSTRAINT fk_user_notification_user 
    FOREIGN KEY (user_id) REFERENCES user(id);

ALTER TABLE approval_level_approver 
    ADD CONSTRAINT fk_approval_level_approver_user 
    FOREIGN KEY (user_id) REFERENCES user(id);

