package com.university.academic.dto.converter;

import com.university.academic.dto.GraduationAuditDTO;
import com.university.academic.entity.GraduationAudit;
import com.university.academic.entity.User;
import com.university.academic.repository.StudentRepository;
import com.university.academic.repository.TeacherRepository;
import com.university.academic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 毕业审核DTO转换器
 *
 * @author Academic System Team
 */
@Component
@RequiredArgsConstructor
public class GraduationAuditConverter {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    /**
     * 实体转DTO
     *
     * @param entity 实体对象
     * @return DTO对象
     */
    public GraduationAuditDTO toDTO(GraduationAudit entity) {
        if (entity == null) {
            return null;
        }

        GraduationAuditDTO.GraduationAuditDTOBuilder builder = GraduationAuditDTO.builder()
                .id(entity.getId())
                .studentId(entity.getStudent().getId())
                .studentNo(entity.getStudent().getStudentNo())
                .studentName(entity.getStudent().getName())
                .majorId(entity.getStudent().getMajor().getId())
                .majorName(entity.getStudent().getMajor().getName())
                .auditYear(entity.getAuditYear())
                .totalCredits(entity.getTotalCredits())
                .requiredCredits(entity.getRequiredCredits())
                .electiveCredits(entity.getElectiveCredits())
                .practicalCredits(entity.getPracticalCredits())
                .status(entity.getStatus())
                .statusDescription(entity.getStatus().getDescription())
                .failReason(entity.getFailReason())
                .auditedBy(entity.getAuditedBy())
                .auditedAt(entity.getAuditedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        // 查询审核人姓名
        if (entity.getAuditedBy() != null) {
            String auditedByName = getAuditorName(entity.getAuditedBy());
            builder.auditedByName(auditedByName);
        }

        return builder.build();
    }

    /**
     * 根据用户ID获取审核人姓名
     *
     * @param userId 用户ID
     * @return 审核人姓名
     */
    private String getAuditorName(Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    // 根据用户角色获取真实姓名
                    if (user.getRole() == User.UserRole.STUDENT) {
                        return studentRepository.findByUserId(user.getId())
                                .map(student -> student.getName())
                                .orElse(user.getUsername());
                    } else if (user.getRole() == User.UserRole.TEACHER) {
                        return teacherRepository.findByUserId(user.getId())
                                .map(teacher -> teacher.getName())
                                .orElse(user.getUsername());
                    } else {
                        // 管理员返回用户名
                        return user.getUsername();
                    }
                })
                .orElse("未知");
    }

    /**
     * 实体列表转DTO列表
     *
     * @param entities 实体列表
     * @return DTO列表
     */
    public List<GraduationAuditDTO> toDTOList(List<GraduationAudit> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}

