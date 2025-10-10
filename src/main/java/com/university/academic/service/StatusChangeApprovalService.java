package com.university.academic.service;

import com.university.academic.entity.ApprovalAction;
import com.university.academic.entity.StatusChangeApproval;
import com.university.academic.entity.StudentStatusChange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 学籍异动审批Service接口
 *
 * @author Academic System Team
 */
public interface StatusChangeApprovalService {

    /**
     * 审批异动申请
     *
     * @param applicationId 申请ID
     * @param approverId    审批人ID
     * @param action        审批操作（批准、拒绝、退回）
     * @param comment       审批意见
     * @return 更新后的异动申请
     */
    StudentStatusChange approveApplication(
            Long applicationId,
            Long approverId,
            ApprovalAction action,
            String comment
    );

    /**
     * 查询审批人的待审批申请列表
     *
     * @param approverId 审批人ID
     * @param pageable   分页参数
     * @return 待审批申请分页列表
     */
    Page<StudentStatusChange> getPendingApplications(Long approverId, Pageable pageable);

    /**
     * 查询异动申请的审批历史
     *
     * @param applicationId 申请ID
     * @return 审批记录列表
     */
    List<StatusChangeApproval> getApprovalHistory(Long applicationId);

    /**
     * 检查用户是否有权限审批该申请
     *
     * @param applicationId 申请ID
     * @param userId        用户ID
     * @return 是否有权限
     */
    boolean canApprove(Long applicationId, Long userId);

    /**
     * 获取当前审批级别名称
     *
     * @param level 审批级别
     * @return 级别名称
     */
    String getApprovalLevelName(Integer level);

    /**
     * 获取待审批的申请详情（包含权限检查）
     *
     * @param applicationId 申请ID
     * @param approverId    审批人ID
     * @return 异动申请
     */
    StudentStatusChange getApplicationForApproval(Long applicationId, Long approverId);
}

