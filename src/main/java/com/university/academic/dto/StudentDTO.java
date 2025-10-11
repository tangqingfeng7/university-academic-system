package com.university.academic.dto;

import com.university.academic.entity.StudentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 学生DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {
    
    private Long id;
    
    /**
     * 学号
     */
    private String studentNo;
    
    /**
     * 姓名
     */
    private String name;
    
    /**
     * 性别
     */
    private String gender;
    
    /**
     * 出生日期
     */
    private LocalDate birthDate;
    
    /**
     * 入学年份
     */
    private Integer enrollmentYear;
    
    /**
     * 院系ID
     */
    private Long departmentId;
    
    /**
     * 院系名称
     */
    private String departmentName;
    
    /**
     * 专业ID
     */
    private Long majorId;
    
    /**
     * 专业名称
     */
    private String majorName;
    
    /**
     * 年级
     */
    private Integer grade;
    
    /**
     * 班级
     */
    private String className;
    
    /**
     * 电话
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 学生状态
     */
    private StudentStatus status;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 创建时间
     */
    private java.time.LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private java.time.LocalDateTime updatedAt;
}
