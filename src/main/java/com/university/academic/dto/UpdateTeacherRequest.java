package com.university.academic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新教师请求
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTeacherRequest {

    /**
     * 工号
     */
    private String teacherNo;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别 (MALE/FEMALE)
     */
    @Pattern(regexp = "MALE|FEMALE", message = "性别必须是MALE或FEMALE")
    private String gender;

    /**
     * 职称
     */
    private String title;

    /**
     * 院系ID
     */
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
}

