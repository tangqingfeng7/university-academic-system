package com.university.academic.service.impl;

import com.university.academic.entity.*;
import com.university.academic.repository.ApprovalConfigurationRepository;
import com.university.academic.repository.ApprovalLevelApproverRepository;
import com.university.academic.repository.UserRepository;
import com.university.academic.service.ApprovalWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 审批工作流Service实现类
 * 支持负载均衡和可配置审批时限
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalWorkflowServiceImpl implements ApprovalWorkflowService {

    private final UserRepository userRepository;
    private final ApprovalLevelApproverRepository approverRepository;
    private final ApprovalConfigurationRepository configurationRepository;

    /**
     * 默认审批时限（天数）
     */
    private static final int DEFAULT_DEADLINE_DAYS = 7;

    /**
     * 审批级别和角色映射
     * Level 1: 教师（辅导员）
     * Level 2: 管理员（院系）
     * Level 3: 管理员（教务处）
     */

    @Override
    @Transactional(readOnly = true)
    public Long assignApprover(StudentStatusChange statusChange) {
        Integer level = statusChange.getApprovalLevel();
        return assignApproverByLevel(level);
    }

    @Override
    @Transactional
    public Long assignApproverByLevel(Integer approvalLevel) {
        log.debug("为审批级别{}分配审批人（负载均衡）", approvalLevel);

        // 优先使用配置的审批人（支持负载均衡）
        ApprovalLevelApprover approver = approverRepository.findLeastLoadedByLevel(approvalLevel);
        if (approver != null) {
            log.info("使用负载均衡分配审批人: level={}, userId={}, currentLoad={}",
                    approvalLevel, approver.getUser().getId(), approver.getPendingCount());
            return approver.getUser().getId();
        }

        // 如果没有配置审批人，使用默认逻辑（向后兼容）
        log.warn("审批级别{}未配置审批人，使用默认分配逻辑", approvalLevel);
        if (approvalLevel == 1) {
            // 第一级：辅导员（教师角色）
            return userRepository.findFirstTeacher()
                    .map(User::getId)
                    .orElse(null);
        } else if (approvalLevel == 2 || approvalLevel == 3) {
            // 第二级和第三级：院系和教务处（管理员角色）
            return userRepository.findFirstAdmin()
                    .map(User::getId)
                    .orElse(null);
        }

        log.warn("未知的审批级别: {}", approvalLevel);
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasApprovalPermission(StudentStatusChange statusChange, Long userId) {
        // 查询用户信息
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !user.getEnabled()) {
            log.debug("用户不存在或已禁用: userId={}", userId);
            return false;
        }

        // 获取当前审批级别
        Integer currentLevel = statusChange.getApprovalLevel();
        
        // 优先检查是否在配置的审批人列表中
        ApprovalLevelApprover approver = approverRepository.findByUserIdAndLevel(userId, currentLevel);
        if (approver != null) {
            log.debug("用户在配置的审批人列表中: userId={}, level={}", userId, currentLevel);
            return true;
        }
        
        // 向后兼容：使用基于角色的权限检查
        Integer userLevel = getUserApprovalLevel(userId);
        
        if (userLevel == -1) {
            log.debug("用户无审批权限: userId={}, role={}", userId, user.getRole());
            return false;
        }

        // 检查用户级别是否匹配当前审批级别
        boolean hasPermission = userLevel.equals(currentLevel) || 
                               (user.getRole() == User.UserRole.ADMIN && (currentLevel == 2 || currentLevel == 3));
        
        log.debug("权限检查结果: userId={}, userLevel={}, currentLevel={}, hasPermission={}",
                userId, userLevel, currentLevel, hasPermission);
        
        return hasPermission;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getUserApprovalLevel(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !user.getEnabled()) {
            return -1;
        }

        // 根据用户角色确定审批级别
        return switch (user.getRole()) {
            case TEACHER -> 1;  // 教师对应第1级（辅导员）
            case ADMIN -> 2;    // 管理员可以审批第2级和第3级，这里返回2，实际审批时需要额外判断
            case STUDENT -> -1; // 学生没有审批权限
        };
    }

    @Override
    @Transactional(readOnly = true)
    public int getDeadlineDays(ChangeType changeType) {
        return configurationRepository.findByChangeType(changeType)
                .map(ApprovalConfiguration::getDeadlineDays)
                .orElse(DEFAULT_DEADLINE_DAYS);
    }

    @Override
    @Transactional
    public void incrementApproverLoad(Long userId, Integer approvalLevel) {
        ApprovalLevelApprover approver = approverRepository.findByUserIdAndLevel(userId, approvalLevel);
        if (approver != null) {
            approverRepository.incrementPendingCount(approver.getId());
            log.debug("增加审批人负载: userId={}, level={}, newCount={}",
                    userId, approvalLevel, approver.getPendingCount() + 1);
        }
    }

    @Override
    @Transactional
    public void decrementApproverLoad(Long userId, Integer approvalLevel) {
        ApprovalLevelApprover approver = approverRepository.findByUserIdAndLevel(userId, approvalLevel);
        if (approver != null) {
            approverRepository.decrementPendingCount(approver.getId());
            log.debug("减少审批人负载: userId={}, level={}, newCount={}",
                    userId, approvalLevel, Math.max(0, approver.getPendingCount() - 1));
        }
    }

    /**
     * 检查管理员是否可以审批指定级别
     *
     * @param userId        用户ID
     * @param approvalLevel 审批级别
     * @return 是否可以审批
     */
    public boolean canAdminApproveLevel(Long userId, Integer approvalLevel) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            return false;
        }
        // 管理员可以审批第2级和第3级
        return approvalLevel == 2 || approvalLevel == 3;
    }
}

