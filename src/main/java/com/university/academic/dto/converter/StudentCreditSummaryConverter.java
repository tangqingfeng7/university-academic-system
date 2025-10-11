package com.university.academic.dto.converter;

import com.university.academic.dto.StudentCreditSummaryDTO;
import com.university.academic.entity.StudentCreditSummary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 学生学分汇总DTO转换器
 *
 * @author Academic System Team
 */
@Component
public class StudentCreditSummaryConverter {

    /**
     * 实体转DTO
     *
     * @param entity 实体对象
     * @return DTO对象
     */
    public StudentCreditSummaryDTO toDTO(StudentCreditSummary entity) {
        if (entity == null) {
            return null;
        }

        return StudentCreditSummaryDTO.builder()
                .id(entity.getId())
                .studentId(entity.getStudent().getId())
                .studentNo(entity.getStudent().getStudentNo())
                .studentName(entity.getStudent().getName())
                .majorId(entity.getStudent().getMajor().getId())
                .majorName(entity.getStudent().getMajor().getName())
                .enrollmentYear(entity.getStudent().getEnrollmentYear())
                .totalCredits(entity.getTotalCredits())
                .requiredCredits(entity.getRequiredCredits())
                .electiveCredits(entity.getElectiveCredits())
                .practicalCredits(entity.getPracticalCredits())
                .gpa(entity.getGpa())
                .lastUpdated(entity.getLastUpdated())
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
    public List<StudentCreditSummaryDTO> toDTOList(List<StudentCreditSummary> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}

