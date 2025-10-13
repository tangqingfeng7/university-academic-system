package com.university.ems.enums;

/**
 * 排课约束类型枚举
 * HARD - 硬约束（必须满足）
 * SOFT - 软约束（尽量满足）
 */
public enum ConstraintType {
    /**
     * 硬约束：必须满足的约束条件
     * 例如：教师时间冲突、教室容量限制、学生课表冲突
     */
    HARD,
    
    /**
     * 软约束：尽量满足的约束条件
     * 例如：教师时间偏好、学生连续课程数量、教室利用率
     */
    SOFT
}

