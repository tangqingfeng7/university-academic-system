package com.university.academic.service;

import com.university.academic.dto.*;
import com.university.academic.entity.ApprovalAction;
import com.university.academic.entity.tuition.RefundApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 退费管理服务接口
 *
 * @author Academic System Team
 */
public interface RefundService {

    /**
     * 提交退费申请
     *
     * @param studentId 学生ID
     * @param request   退费申请信息
     * @return 退费申请DTO
     */
    RefundApplicationDTO submitRefundApplication(Long studentId, CreateRefundApplicationRequest request);

    /**
     * 审批退费申请
     *
     * @param applicationId 申请ID
     * @param approverId    审批人ID
     * @param action        审批操作（批准/拒绝）
     * @param comment       审批意见
     * @return 更新后的退费申请
     */
    RefundApplication approveRefundApplication(Long applicationId, Long approverId, 
                                                ApprovalAction action, String comment);

    /**
     * 执行退费（审批通过后）
     *
     * @param applicationId 申请ID
     * @param operatorId    操作员ID
     */
    void executeRefund(Long applicationId, Long operatorId);

    /**
     * 查询学生的退费申请
     *
     * @param studentId 学生ID
     * @return 退费申请列表
     */
    List<RefundApplicationDTO> getStudentRefundApplications(Long studentId);

    /**
     * 查询退费申请详情
     *
     * @param applicationId 申请ID
     * @return 退费申请DTO（含审批历史）
     */
    RefundApplicationDTO getRefundApplicationDetail(Long applicationId);

    /**
     * 查询待审批的退费申请
     *
     * @param approverId 审批人ID
     * @param pageable   分页参数
     * @return 待审批退费申请列表
     */
    Page<RefundApplicationDTO> getPendingRefundApplications(Long approverId, Pageable pageable);

    /**
     * 查询所有退费申请（管理员）
     *
     * @param query    查询条件
     * @param pageable 分页参数
     * @return 退费申请列表
     */
    Page<RefundApplicationDTO> searchRefundApplications(RefundQueryDTO query, Pageable pageable);

    /**
     * 取消退费申请
     *
     * @param applicationId 申请ID
     * @param studentId     学生ID
     */
    void cancelRefundApplication(Long applicationId, Long studentId);

    /**
     * 查询退费申请实体
     *
     * @param applicationId 申请ID
     * @return 退费申请实体
     */
    RefundApplication findRefundApplicationById(Long applicationId);
}

