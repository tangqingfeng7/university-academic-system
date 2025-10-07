package com.university.academic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建教师请求
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeacherRequest {

    /**
     * 工号
     */
    @NotBlank(message = "工号不能为空")
    private String teacherNo;

    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    private String name;

    /**
     * 性别 (MALE/FEMALE)
     */
    @NotBlank(message = "性别不能为空")
    @Pattern(regexp = "MALE|FEMALE", message = "性别必须是MALE或FEMALE")
    private String gender;

    /**
     * 职称
     */
    private String title;

    /**
     * 院系ID
     */
    @NotNull(message = "院系ID不能为空")
    private Long departmentId;

    /**
     * 联系电话
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 用户名（可选，默认使用工号）
     */
    private String username;

    /**
     * 密码（可选，默认123456）
     */
    private String password;
}

