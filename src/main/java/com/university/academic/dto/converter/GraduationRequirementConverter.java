package com.university.academic.dto.converter;

import com.university.academic.dto.CreateGraduationRequirementRequest;
import com.university.academic.dto.GraduationRequirementDTO;
import com.university.academic.entity.GraduationRequirement;
import com.university.academic.entity.Major;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 毕业要求DTO转换器
 *
 * @author Academic System Team
 */
@Component
public class GraduationRequirementConverter {

    /**
     * 将毕业要求实体转换为DTO
     *
     * @param entity 毕业要求实体
     * @return 毕业要求DTO
     */
    public GraduationRequirementDTO toDTO(GraduationRequirement entity) {
        if (entity == null) {
            return null;
        }

        // Null安全检查
        if (entity.getMajor() == null) {
            throw new IllegalStateException("毕业要求关联的专业信息为空: requirementId=" + entity.getId());
        }

        return GraduationRequirementDTO.builder()
                .id(entity.getId())
                .majorId(entity.getMajor().getId())
                .majorName(entity.getMajor().getName())
                .departmentName(entity.getMajor().getDepartment() != null 
                    ? entity.getMajor().getDepartment().getName() : "未知院系")
                .enrollmentYear(entity.getEnrollmentYear())
                .totalCreditsRequired(entity.getTotalCreditsRequired())
                .requiredCreditsRequired(entity.getRequiredCreditsRequired())
                .electiveCreditsRequired(entity.getElectiveCreditsRequired())
                .practicalCreditsRequired(entity.getPracticalCreditsRequired())
                .additionalRequirements(entity.getAdditionalRequirements())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * 将创建请求转换为实体
     *
     * @param request 创建请求
     * @param major   专业实体
     * @return 毕业要求实体
     */
    public GraduationRequirement toEntity(CreateGraduationRequirementRequest request, Major major) {
        if (request == null || major == null) {
            return null;
        }

        return GraduationRequirement.builder()
                .major(major)
                .enrollmentYear(request.getEnrollmentYear())
                .totalCreditsRequired(request.getTotalCreditsRequired())
                .requiredCreditsRequired(request.getRequiredCreditsRequired())
                .electiveCreditsRequired(request.getElectiveCreditsRequired())
                .practicalCreditsRequired(request.getPracticalCreditsRequired())
                .additionalRequirements(request.getAdditionalRequirements())
                .build();
    }

    /**
     * 批量转换毕业要求实体为DTO
     *
     * @param entities 毕业要求实体列表
     * @return 毕业要求DTO列表
     */
    public List<GraduationRequirementDTO> toDTOList(List<GraduationRequirement> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}

