package com.university.ems.service;

import com.university.ems.dto.SchedulingRequest;
import com.university.ems.dto.SchedulingResultDTO;

import java.util.concurrent.CompletableFuture;

/**
 * 智能排课算法服务接口
 * 
 * @author Academic System Team
 */
public interface SchedulingAlgorithmService {

    /**
     * 执行智能排课（同步）
     * 使用约束满足算法为指定学期的课程生成排课方案
     *
     * @param request 排课请求
     * @return 排课结果
     */
    SchedulingResultDTO schedule(SchedulingRequest request);

    /**
     * 执行智能排课（异步）
     * 适用于大规模排课任务
     *
     * @param request 排课请求
     * @return 异步排课结果
     */
    CompletableFuture<SchedulingResultDTO> scheduleAsync(SchedulingRequest request);

    /**
     * 评估排课方案质量
     * 
     * @param solutionId 方案ID
     * @return 质量分数（0-100）
     */
    Double evaluateSolutionQuality(Long solutionId);

    /**
     * 检测排课冲突
     * 
     * @param solutionId 方案ID
     * @return 冲突数量
     */
    Integer detectConflicts(Long solutionId);
}

