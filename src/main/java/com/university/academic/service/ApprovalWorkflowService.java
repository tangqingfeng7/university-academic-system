package com.university.academic.service;

import com.university.academic.entity.ChangeType;
import com.university.academic.entity.StudentStatusChange;

/**
 * 审批工作流Service接口
 *
 * @author Academic System Team
 */
public interface ApprovalWorkflowService {

    /**
     * 为异动申请分配审批人（负载均衡）
     *
     * @param statusChange 异动申请
     * @return 分配的审批人ID
     */
    Long assignApprover(StudentStatusChange statusChange);

    /**
     * 根据审批级别分配审批人（负载均衡）
     *
     * @param approvalLevel 审批级别
     * @return 审批人ID
     */
    Long assignApproverByLevel(Integer approvalLevel);

    /**
     * 验证用户是否有权限审批该申请
     *
     * @param statusChange 异动申请
     * @param userId       用户ID
     * @return 是否有权限
     */
    boolean hasApprovalPermission(StudentStatusChange statusChange, Long userId);

    /**
     * 获取用户的审批级别
     *
     * @param userId 用户ID
     * @return 审批级别，如果无审批权限返回-1
     */
    Integer getUserApprovalLevel(Long userId);

    /**
     * 获取异动类型的审批时限（天数）
     *
     * @param changeType 异动类型
     * @return 审批时限天数
     */
    int getDeadlineDays(ChangeType changeType);

    /**
     * 审批任务分配后，增加审批人的待处理任务数
     *
     * @param userId        审批人用户ID
     * @param approvalLevel 审批级别
     */
    void incrementApproverLoad(Long userId, Integer approvalLevel);

    /**
     * 审批任务完成后，减少审批人的待处理任务数
     *
     * @param userId        审批人用户ID
     * @param approvalLevel 审批级别
     */
    void decrementApproverLoad(Long userId, Integer approvalLevel);
}

