package com.university.academic.service.impl;

import com.university.academic.entity.*;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.StatusChangeApprovalRepository;
import com.university.academic.repository.StudentStatusChangeRepository;
import com.university.academic.repository.UserRepository;
import com.university.academic.service.ApprovalWorkflowService;
import com.university.academic.service.StatusChangeApprovalService;
import com.university.academic.service.StudentStatusUpdateService;
import com.university.academic.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学籍异动审批Service实现类
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatusChangeApprovalServiceImpl implements StatusChangeApprovalService {

    private final StudentStatusChangeRepository statusChangeRepository;
    private final StatusChangeApprovalRepository approvalRepository;
    private final StudentStatusUpdateService studentStatusUpdateService;
    private final UserNotificationService userNotificationService;
    private final ApprovalWorkflowService workflowService;
    private final UserRepository userRepository;

    /**
     * 审批级别配置
     * 1 - 辅导员
     * 2 - 院系
     * 3 - 教务处（最终审批）
     */
    private static final int MAX_APPROVAL_LEVEL = 3;

    @Override
    @Transactional
    public StudentStatusChange approveApplication(
            Long applicationId,
            Long approverId,
            ApprovalAction action,
            String comment
    ) {
        log.info("审批学籍异动申请: 申请ID={}, 审批人ID={}, 操作={}",
                applicationId, approverId, action);

        // 1. 查询异动申请
        StudentStatusChange statusChange = statusChangeRepository
                .findByIdWithDetails(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STATUS_CHANGE_NOT_FOUND));

        // 2. 验证申请状态
        if (statusChange.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.STATUS_CHANGE_ALREADY_PROCESSED);
        }

        // 3. 验证审批权限
        if (!workflowService.hasApprovalPermission(statusChange, approverId)) {
            log.warn("用户无审批权限: userId={}, 申请ID={}, 当前级别={}",
                    approverId, applicationId, statusChange.getApprovalLevel());
            throw new BusinessException(ErrorCode.FORBIDDEN, "您没有权限审批该申请");
        }

        // 4. 获取当前审批级别和审批人
        Integer currentLevel = statusChange.getApprovalLevel();
        Long currentApproverId = statusChange.getCurrentApproverId();

        // 5. 创建审批记录
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        StatusChangeApproval approval = StatusChangeApproval.builder()
                .statusChange(statusChange)
                .approvalLevel(currentLevel)
                .approver(approver)
                .action(action)
                .comment(comment)
                .approvedAt(LocalDateTime.now())
                .deleted(false)
                .build();

        approvalRepository.save(approval);

        // 减少当前审批人的待处理任务计数
        if (currentApproverId != null) {
            workflowService.decrementApproverLoad(currentApproverId, currentLevel);
        }

        // 6. 根据审批操作更新申请状态
        switch (action) {
            case APPROVE -> handleApprove(statusChange, currentLevel);
            case REJECT -> handleReject(statusChange);
            case RETURN -> handleReturn(statusChange);
        }

        // 7. 保存更新
        StudentStatusChange updated = statusChangeRepository.save(statusChange);

        // 8. 发送通知
        sendApprovalNotification(updated, action, comment, currentLevel);

        log.info("审批完成: 申请ID={}, 当前状态={}, 当前级别={}",
                applicationId, updated.getStatus(), updated.getApprovalLevel());

        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentStatusChange> getPendingApplications(Long approverId, Pageable pageable) {
        log.info("查询审批人{}的待审批申请", approverId);
        
        // 获取审批人的审批级别
        Integer approvalLevel = workflowService.getUserApprovalLevel(approverId);
        
        if (approvalLevel == null || approvalLevel < 1) {
            log.warn("用户{}没有审批权限", approverId);
            // 返回空结果
            return Page.empty(pageable);
        }
        
        // 查询该审批级别的所有待审批申请
        Page<StudentStatusChange> pendingApplications = 
                statusChangeRepository.findPendingByApprovalLevel(approvalLevel, pageable);
        
        log.info("审批人{}（级别{}）查询到{}个待审批申请", 
                approverId, approvalLevel, pendingApplications.getTotalElements());
        
        return pendingApplications;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatusChangeApproval> getApprovalHistory(Long applicationId) {
        return approvalRepository.findByStatusChangeId(applicationId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canApprove(Long applicationId, Long userId) {
        StudentStatusChange statusChange = statusChangeRepository
                .findByIdWithDetails(applicationId)
                .orElse(null);

        if (statusChange == null || statusChange.getStatus() != ApprovalStatus.PENDING) {
            return false;
        }

        // 使用工作流服务检查权限
        return workflowService.hasApprovalPermission(statusChange, userId);
    }

    @Override
    public String getApprovalLevelName(Integer level) {
        return switch (level) {
            case 1 -> "辅导员";
            case 2 -> "院系";
            case 3 -> "教务处";
            default -> "未知";
        };
    }

    /**
     * 处理批准操作
     *
     * @param statusChange 异动申请
     * @param currentLevel 当前审批级别
     */
    private void handleApprove(StudentStatusChange statusChange, Integer currentLevel) {
        if (currentLevel < MAX_APPROVAL_LEVEL) {
            // 还有下一级审批，流转到下一级
            Integer nextLevel = currentLevel + 1;
            statusChange.setApprovalLevel(nextLevel);
            
            // 自动分配下一级审批人（负载均衡）
            Long nextApproverId = workflowService.assignApproverByLevel(nextLevel);
            statusChange.setCurrentApproverId(nextApproverId);
            
            // 增加下一级审批人的待处理任务计数
            if (nextApproverId != null) {
                workflowService.incrementApproverLoad(nextApproverId, nextLevel);
            }
            
            log.info("审批通过，流转到下一级: 当前级别={}, 下一级别={}, 下一级审批人ID={}",
                    currentLevel, nextLevel, nextApproverId);
        } else {
            // 最终审批完成
            statusChange.setStatus(ApprovalStatus.APPROVED);
            statusChange.setCurrentApproverId(null);
            
            // 执行学籍变更
            executeStatusChange(statusChange);
            
            log.info("最终审批完成，申请已批准: 申请ID={}", statusChange.getId());
        }
    }

    /**
     * 处理拒绝操作
     *
     * @param statusChange 异动申请
     */
    private void handleReject(StudentStatusChange statusChange) {
        statusChange.setStatus(ApprovalStatus.REJECTED);
        statusChange.setCurrentApproverId(null);
        log.info("审批拒绝: 申请ID={}", statusChange.getId());
    }

    /**
     * 处理退回操作
     *
     * @param statusChange 异动申请
     */
    private void handleReturn(StudentStatusChange statusChange) {
        // 退回到第一级重新审批
        statusChange.setApprovalLevel(1);
        
        // 自动分配第一级审批人（负载均衡）
        Long firstApproverId = workflowService.assignApproverByLevel(1);
        statusChange.setCurrentApproverId(firstApproverId);
        
        // 增加第一级审批人的待处理任务计数
        if (firstApproverId != null) {
            workflowService.incrementApproverLoad(firstApproverId, 1);
        }
        
        log.info("审批退回，返回第一级: 申请ID={}, 第一级审批人ID={}",
                statusChange.getId(), firstApproverId);
    }

    /**
     * 执行学籍变更
     *
     * @param statusChange 异动申请
     */
    private void executeStatusChange(StudentStatusChange statusChange) {
        log.info("执行学籍变更: 申请ID={}, 类型={}",
                statusChange.getId(), statusChange.getType());

        // 使用StudentStatusUpdateService统一处理状态更新
        studentStatusUpdateService.updateStudentStatus(statusChange);

        log.info("学籍变更执行完成: 申请ID={}", statusChange.getId());
    }

    /**
     * 发送审批通知
     *
     * @param statusChange 异动申请
     * @param action       审批操作
     * @param comment      审批意见
     * @param approvalLevel 审批级别
     */
    private void sendApprovalNotification(
            StudentStatusChange statusChange,
            ApprovalAction action,
            String comment,
            Integer approvalLevel
    ) {
        try {
            Student student = statusChange.getStudent();
            Long studentUserId = student.getUser().getId();
            String levelName = getApprovalLevelName(approvalLevel);
            
            // 构建通知标题和内容
            String title;
            String content;
            
            switch (action) {
                case APPROVE -> {
                    if (statusChange.getStatus() == ApprovalStatus.APPROVED) {
                        // 最终批准
                        title = "学籍异动申请已批准";
                        content = String.format(
                                "您的%s申请已通过最终审批，申请已批准。\n\n" +
                                "申请编号：%d\n" +
                                "审批意见：%s",
                                statusChange.getType().getDescription(),
                                statusChange.getId(),
                                comment != null ? comment : "无"
                        );
                    } else {
                        // 流转到下一级
                        title = "学籍异动申请审批进度";
                        content = String.format(
                                "您的%s申请已通过%s审批，正在流转到%s审批。\n\n" +
                                "申请编号：%d\n" +
                                "审批意见：%s",
                                statusChange.getType().getDescription(),
                                levelName,
                                getApprovalLevelName(statusChange.getApprovalLevel()),
                                statusChange.getId(),
                                comment != null ? comment : "无"
                        );
                    }
                }
                case REJECT -> {
                    title = "学籍异动申请被拒绝";
                    content = String.format(
                            "您的%s申请在%s审批环节被拒绝。\n\n" +
                            "申请编号：%d\n" +
                            "拒绝原因：%s",
                            statusChange.getType().getDescription(),
                            levelName,
                            statusChange.getId(),
                            comment != null ? comment : "无"
                    );
                }
                case RETURN -> {
                    title = "学籍异动申请被退回";
                    content = String.format(
                            "您的%s申请在%s审批环节被退回，请修改后重新提交。\n\n" +
                            "申请编号：%d\n" +
                            "退回原因：%s",
                            statusChange.getType().getDescription(),
                            levelName,
                            statusChange.getId(),
                            comment != null ? comment : "无"
                    );
                }
                default -> {
                    return;
                }
            }

            // 发送通知给特定学生
            userNotificationService.sendToUser(
                    studentUserId,
                    title,
                    content,
                    "STATUS_CHANGE",
                    "STATUS_CHANGE",
                    statusChange.getId()
            );

            log.info("审批通知已发送: 申请ID={}, 学生用户ID={}, 标题={}",
                    statusChange.getId(), studentUserId, title);

        } catch (Exception e) {
            // 通知发送失败不影响审批流程
            log.error("发送审批通知失败: 申请ID={}", statusChange.getId(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StudentStatusChange getApplicationForApproval(Long applicationId, Long approverId) {
        log.info("审批人{}查询申请详情: {}", approverId, applicationId);

        // 1. 查询申请
        StudentStatusChange statusChange = statusChangeRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STATUS_CHANGE_NOT_FOUND));

        // 2. 检查权限
        if (!workflowService.hasApprovalPermission(statusChange, approverId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看此申请");
        }

        return statusChange;
    }
}

