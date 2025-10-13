package com.university.ems.service;

import com.university.ems.dto.*;
import com.university.ems.enums.SolutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 排课方案管理服务接口
 * 
 * @author Academic System Team
 */
public interface SchedulingSolutionService {

    /**
     * 创建排课方案
     *
     * @param request 创建请求
     * @return 方案DTO
     */
    SchedulingSolutionDTO createSolution(CreateSolutionRequest request);

    /**
     * 执行智能排课
     * 为指定方案执行排课算法
     *
     * @param solutionId 方案ID
     * @param schedulingRequest 排课请求参数
     * @return 排课结果
     */
    SchedulingResultDTO executeScheduling(Long solutionId, SchedulingRequest schedulingRequest);

    /**
     * 手动调整排课
     * 调整单个课程的时间或教室
     *
     * @param solutionId 方案ID
     * @param request 调整请求
     * @return 调整后的排课项
     */
    ScheduleItemDTO adjustSchedule(Long solutionId, ScheduleAdjustmentRequest request);

    /**
     * 检测方案冲突
     *
     * @param solutionId 方案ID
     * @return 冲突列表
     */
    List<ConflictDTO> detectConflicts(Long solutionId);

    /**
     * 应用排课方案
     * 将排课方案批量保存到课程开课表
     *
     * @param solutionId 方案ID
     * @param request 应用请求
     */
    void applySolution(Long solutionId, ApplySolutionRequest request);

    /**
     * 查询方案详情
     *
     * @param solutionId 方案ID
     * @return 方案DTO
     */
    SchedulingSolutionDTO getSolutionById(Long solutionId);

    /**
     * 查询方案的排课结果
     *
     * @param solutionId 方案ID
     * @return 排课结果
     */
    SchedulingResultDTO getSolutionResult(Long solutionId);

    /**
     * 查询学期的所有方案
     *
     * @param semesterId 学期ID
     * @param pageable 分页参数
     * @return 方案分页
     */
    Page<SchedulingSolutionDTO> getSolutionsBySemester(Long semesterId, Pageable pageable);

    /**
     * 查询学期指定状态的方案
     *
     * @param semesterId 学期ID
     * @param status 方案状态
     * @return 方案列表
     */
    List<SchedulingSolutionDTO> getSolutionsBySemesterAndStatus(Long semesterId, SolutionStatus status);

    /**
     * 查询学期已应用的方案
     *
     * @param semesterId 学期ID
     * @return 已应用的方案
     */
    SchedulingSolutionDTO getAppliedSolution(Long semesterId);

    /**
     * 删除方案
     *
     * @param solutionId 方案ID
     */
    void deleteSolution(Long solutionId);

    /**
     * 评估方案质量
     *
     * @param solutionId 方案ID
     * @return 质量分数
     */
    Double evaluateQuality(Long solutionId);

    /**
     * 比较两个方案
     *
     * @param solutionId1 方案1 ID
     * @param solutionId2 方案2 ID
     * @return 比较结果
     */
    String compareSolutions(Long solutionId1, Long solutionId2);
}

