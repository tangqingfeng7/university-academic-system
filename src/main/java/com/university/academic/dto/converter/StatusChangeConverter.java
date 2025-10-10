package com.university.academic.dto.converter;

import com.university.academic.dto.StatusChangeApprovalDTO;
import com.university.academic.dto.StudentStatusChangeDTO;
import com.university.academic.entity.*;
import com.university.academic.repository.MajorRepository;
import com.university.academic.repository.StatusChangeApprovalRepository;
import com.university.academic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 学籍异动DTO转换器
 *
 * @author university
 * @since 2024-01-01
 */
@Component
@RequiredArgsConstructor
public class StatusChangeConverter {

    private final MajorRepository majorRepository;
    private final UserRepository userRepository;
    private final StatusChangeApprovalRepository approvalRepository;

    /**
     * 将实体转换为DTO
     *
     * @param entity 学籍异动实体
     * @return DTO
     */
    public StudentStatusChangeDTO toDTO(StudentStatusChange entity) {
        return toDTO(entity, false);
    }

    /**
     * 将实体转换为DTO
     *
     * @param entity             学籍异动实体
     * @param includeApprovalHistory 是否包含审批历史
     * @return DTO
     */
    public StudentStatusChangeDTO toDTO(StudentStatusChange entity, boolean includeApprovalHistory) {
        if (entity == null) {
            return null;
        }

        Student student = entity.getStudent();
        Major major = student.getMajor();

        StudentStatusChangeDTO dto = StudentStatusChangeDTO.builder()
                .id(entity.getId())
                .studentId(student.getId())
                .studentName(student.getName())
                .studentNo(student.getStudentNo())
                .majorName(major != null ? major.getName() : null)
                .type(entity.getType())
                .typeDescription(getChangeTypeDescription(entity.getType()))
                .reason(entity.getReason())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .targetMajorId(entity.getTargetMajorId())
                .attachmentUrl(entity.getAttachmentUrl())
                .status(entity.getStatus())
                .statusDescription(getApprovalStatusDescription(entity.getStatus()))
                .currentApproverId(entity.getCurrentApproverId())
                .approvalLevel(entity.getApprovalLevel())
                .approvalLevelDescription(getApprovalLevelDescription(entity.getApprovalLevel()))
                .deadline(entity.getDeadline())
                .isOverdue(entity.getIsOverdue())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();

        // 设置目标专业名称
        if (entity.getTargetMajorId() != null) {
            majorRepository.findById(entity.getTargetMajorId())
                    .ifPresent(targetMajor -> dto.setTargetMajorName(targetMajor.getName()));
        }

        // 设置当前审批人姓名
        if (entity.getCurrentApproverId() != null) {
            userRepository.findById(entity.getCurrentApproverId())
                    .ifPresent(approver -> dto.setCurrentApproverName(approver.getUsername()));
        }

        // 包含审批历史
        if (includeApprovalHistory) {
            List<StatusChangeApproval> approvals = approvalRepository.findByStatusChangeId(entity.getId());
            dto.setApprovalHistory(approvals.stream()
                    .map(this::toApprovalDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    /**
     * 将审批记录实体转换为DTO
     *
     * @param entity 审批记录实体
     * @return DTO
     */
    public StatusChangeApprovalDTO toApprovalDTO(StatusChangeApproval entity) {
        if (entity == null) {
            return null;
        }

        User approver = entity.getApprover();

        return StatusChangeApprovalDTO.builder()
                .id(entity.getId())
                .statusChangeId(entity.getStatusChange().getId())
                .approvalLevel(entity.getApprovalLevel())
                .approvalLevelDescription(getApprovalLevelDescription(entity.getApprovalLevel()))
                .approverId(approver.getId())
                .approverName(approver.getUsername())
                .action(entity.getAction())
                .actionDescription(getApprovalActionDescription(entity.getAction()))
                .comment(entity.getComment())
                .approvedAt(entity.getApprovedAt())
                .build();
    }

    /**
     * 获取异动类型描述
     */
    private String getChangeTypeDescription(ChangeType type) {
        return switch (type) {
            case SUSPENSION -> "休学";
            case RESUMPTION -> "复学";
            case TRANSFER -> "转专业";
            case WITHDRAWAL -> "退学";
        };
    }

    /**
     * 获取审批状态描述
     */
    private String getApprovalStatusDescription(ApprovalStatus status) {
        return switch (status) {
            case PENDING -> "待审批";
            case APPROVED -> "已批准";
            case REJECTED -> "已拒绝";
            case CANCELLED -> "已取消";
        };
    }

    /**
     * 获取审批级别描述
     */
    private String getApprovalLevelDescription(Integer level) {
        return switch (level) {
            case 1 -> "辅导员审批";
            case 2 -> "院系审批";
            case 3 -> "教务处审批";
            default -> "未知级别";
        };
    }

    /**
     * 获取审批操作描述
     */
    private String getApprovalActionDescription(ApprovalAction action) {
        return switch (action) {
            case APPROVE -> "批准";
            case REJECT -> "拒绝";
            case RETURN -> "退回";
        };
    }
}

