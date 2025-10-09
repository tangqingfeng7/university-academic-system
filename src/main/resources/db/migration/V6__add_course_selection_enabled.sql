-- 为semester表添加course_selection_enabled字段（选课功能开关）
ALTER TABLE semester 
ADD COLUMN course_selection_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '选课功能是否启用' AFTER course_selection_end;

-- 为现有数据设置默认值为启用
UPDATE semester SET course_selection_enabled = TRUE WHERE course_selection_enabled IS NULL;

