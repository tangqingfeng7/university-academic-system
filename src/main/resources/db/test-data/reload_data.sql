-- ============================================================================
-- 重新加载测试数据脚本（修复字符编码问题后使用）
-- ============================================================================

USE academic_system;

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 清空相关表数据（按依赖顺序）
SET FOREIGN_KEY_CHECKS = 0;

-- 清空学生相关数据
DELETE FROM attendance_request;
DELETE FROM attendance_detail;
DELETE FROM attendance_warning;
DELETE FROM attendance_statistics;
DELETE FROM attendance_record;
DELETE FROM grade;
DELETE FROM course_selection;
DELETE FROM student WHERE id > 0;

-- 清空教师相关数据
DELETE FROM teacher WHERE id > 0;

-- 清空课程相关数据
DELETE FROM course_prerequisite;
DELETE FROM course_offering;
DELETE FROM course WHERE id > 0;

-- 清空班级数据
DELETE FROM class WHERE id > 0;

-- 清空专业和院系数据
DELETE FROM major WHERE id > 0;
DELETE FROM department WHERE id > 0;

-- 清空学期数据
DELETE FROM semester WHERE id > 0;

-- 清空通知数据
DELETE FROM notification WHERE id > 1;

-- 清空用户数据（保留admin用户 id=1）
DELETE FROM sys_user WHERE id > 1;

SET FOREIGN_KEY_CHECKS = 1;

-- 重置自增ID
ALTER TABLE department AUTO_INCREMENT = 1;
ALTER TABLE major AUTO_INCREMENT = 1;
ALTER TABLE sys_user AUTO_INCREMENT = 2;
ALTER TABLE teacher AUTO_INCREMENT = 1;
ALTER TABLE student AUTO_INCREMENT = 1;
ALTER TABLE class AUTO_INCREMENT = 1;
ALTER TABLE course AUTO_INCREMENT = 1;
ALTER TABLE semester AUTO_INCREMENT = 1;
ALTER TABLE course_offering AUTO_INCREMENT = 1;
ALTER TABLE course_selection AUTO_INCREMENT = 1;
ALTER TABLE notification AUTO_INCREMENT = 2;

-- 提示：数据已清空，现在可以重新导入测试数据
-- 依次执行：test_data.sql -> additional_test_data.sql -> complete_class_data.sql

