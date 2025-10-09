-- 为exam_room表添加version字段（用于乐观锁）
ALTER TABLE exam_room 
ADD COLUMN version BIGINT DEFAULT 0 AFTER assigned_count;

-- 为现有数据设置默认版本号
UPDATE exam_room SET version = 0 WHERE version IS NULL;

