package com.university.ems.service;

import com.university.ems.dto.SchedulingResultDTO;

/**
 * 排课通知服务接口
 * 负责在排课完成后发送通知
 * 
 * @author Academic System Team
 */
public interface SchedulingNotificationService {

    /**
     * 发送排课完成通知（给教师和学生）
     *
     * @param solutionId 排课方案ID
     * @param result 排课结果
     */
    void sendSchedulingCompletedNotification(Long solutionId, SchedulingResultDTO result);

    /**
     * 发送通知给教师
     * 通知教师其负责课程的排课信息
     *
     * @param solutionId 排课方案ID
     * @param semesterId 学期ID
     */
    void notifyTeachers(Long solutionId, Long semesterId);

    /**
     * 发送通知给学生
     * 通知学生课程表已更新
     *
     * @param solutionId 排课方案ID
     * @param semesterId 学期ID
     */
    void notifyStudents(Long solutionId, Long semesterId);

    /**
     * 发送排课失败通知给管理员
     *
     * @param solutionId 排课方案ID
     * @param errorMessage 错误信息
     */
    void notifySchedulingFailed(Long solutionId, String errorMessage);
}

