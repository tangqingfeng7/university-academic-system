package com.university.ems.enums;

/**
 * 排课方案状态枚举
 */
public enum SolutionStatus {
    /**
     * 草稿：方案创建但未开始优化
     */
    DRAFT,
    
    /**
     * 优化中：正在执行智能排课算法
     */
    OPTIMIZING,
    
    /**
     * 已完成：优化完成，等待审核和应用
     */
    COMPLETED,
    
    /**
     * 已应用：方案已应用到实际排课系统
     */
    APPLIED
}

