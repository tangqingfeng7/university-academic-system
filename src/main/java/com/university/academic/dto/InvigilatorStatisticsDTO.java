package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 监考统计DTO（单个教师）
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvigilatorStatisticsDTO {
    
    /**
     * 教师ID
     */
    private Long teacherId;
    
    /**
     * 教师工号
     */
    private String teacherNo;
    
    /**
     * 教师姓名
     */
    private String teacherName;
    
    /**
     * 部门名称
     */
    private String departmentName;
    
    /**
     * 监考总次数
     */
    private Long totalCount;
    
    /**
     * 主监考次数
     */
    private Long chiefCount;
    
    /**
     * 副监考次数
     */
    private Long assistantCount;
}

