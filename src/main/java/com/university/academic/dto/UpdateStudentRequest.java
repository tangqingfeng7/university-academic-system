package com.university.academic.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 更新学生请求DTO
 *
 * @author Academic System Team
 */
@Data
public class UpdateStudentRequest {

    @Size(max = 20, message = "学号长度不能超过20")
    private String studentNo;

    @Size(max = 50, message = "姓名长度不能超过50")
    private String name;

    /**
     * 性别
     */
    private String gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    @Min(value = 1900, message = "入学年份不合法")
    @Max(value = 2100, message = "入学年份不合法")
    private Integer enrollmentYear;

    /**
     * 专业ID
     */
    private Long majorId;

    @Size(max = 50, message = "班级名称长度不能超过50")
    private String className;

    @Size(max = 20, message = "联系电话长度不能超过20")
    private String phone;
}

