package com.university.academic.dto.converter;

import com.university.academic.dto.GraduationAuditDTO;
import com.university.academic.entity.GraduationAudit;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 毕业审核DTO转换器
 *
 * @author Academic System Team
 */
@Component
public class GraduationAuditConverter {

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

        return GraduationAuditDTO.builder()
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
                // TODO: 查询审核人姓名
                // .auditedByName(...)
                .auditedAt(entity.getAuditedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
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

