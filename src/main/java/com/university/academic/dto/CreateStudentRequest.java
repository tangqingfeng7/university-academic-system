package com.university.academic.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建学生请求DTO
 *
 * @author Academic System Team
 */
@Data
public class CreateStudentRequest {

    @NotBlank(message = "学号不能为空")
    @Size(max = 20, message = "学号长度不能超过20")
    private String studentNo;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50")
    private String name;

    @NotBlank(message = "性别不能为空")
    private String gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    @NotNull(message = "入学年份不能为空")
    @Min(value = 1900, message = "入学年份不合法")
    @Max(value = 2100, message = "入学年份不合法")
    private Integer enrollmentYear;

    @NotNull(message = "专业ID不能为空")
    private Long majorId;

    @Size(max = 50, message = "班级名称长度不能超过50")
    private String className;

    @Size(max = 20, message = "联系电话长度不能超过20")
    private String phone;

    /**
     * 用户名（可选，如果不提供则使用学号作为用户名）
     */
    private String username;

    /**
     * 初始密码（可选，如果不提供则使用默认密码123456）
     */
    private String password;
}

