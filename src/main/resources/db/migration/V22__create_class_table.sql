-- ============================================================================
-- 班级管理模块数据库迁移脚本
-- Version: V20
-- Description: 创建班级表
-- Author: Academic System Team
-- Date: 2025-01-13
-- ============================================================================

USE academic_system;

-- ============================================================================
-- 班级表 (class)
-- ============================================================================
CREATE TABLE IF NOT EXISTS class (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    class_code VARCHAR(20) UNIQUE NOT NULL COMMENT '班级代码（如：2024CS01）',
    class_name VARCHAR(100) NOT NULL COMMENT '班级名称（如：计算机科学与技术2024级1班）',
    major_id BIGINT NOT NULL COMMENT '所属专业ID',
    enrollment_year INT NOT NULL COMMENT '入学年份',
    capacity INT COMMENT '班级人数上限',
    counselor_id BIGINT COMMENT '辅导员ID（关联user表）',
    remarks TEXT COMMENT '备注',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除（软删除：0-否，1-是）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_class_code (class_code),
    INDEX idx_class_major (major_id),
    INDEX idx_class_year (enrollment_year),
    INDEX idx_class_deleted (deleted),
    CONSTRAINT fk_class_major FOREIGN KEY (major_id) REFERENCES major(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级表';

-- 插入测试数据
INSERT INTO class (class_code, class_name, major_id, enrollment_year, capacity, counselor_id, remarks, deleted) VALUES
('2024CS01', '计算机科学与技术2024级1班', 1, 2024, 40, NULL, '计算机科学与技术专业第1班', 0),
('2024CS02', '计算机科学与技术2024级2班', 1, 2024, 40, NULL, '计算机科学与技术专业第2班', 0),
('2024SE01', '软件工程2024级1班', 2, 2024, 35, NULL, '软件工程专业第1班', 0),
('2023CS01', '计算机科学与技术2023级1班', 1, 2023, 40, NULL, '计算机科学与技术专业第1班', 0),
('2023SE01', '软件工程2023级1班', 2, 2023, 35, NULL, '软件工程专业第1班', 0);

