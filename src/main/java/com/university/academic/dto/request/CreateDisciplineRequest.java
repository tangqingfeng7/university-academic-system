package com.university.academic.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建处分请求
 *
 * @author Academic System Team
 */
@Data
public class CreateDisciplineRequest {

    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    @NotBlank(message = "处分类型不能为空")
    private String disciplineType;

    @NotBlank(message = "处分原因不能为空")
    private String reason;

    private String description;

    @NotNull(message = "违纪发生日期不能为空")
    private LocalDate occurrenceDate;

    @NotNull(message = "处分日期不能为空")
    private LocalDate punishmentDate;

    private Boolean canRemove = true;

    private String attachmentUrl;

    // 上报人ID改为可选，由后端自动设置
    private Long reporterId;
}

