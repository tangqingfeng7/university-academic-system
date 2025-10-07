package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 专业数据传输对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MajorDTO {

    /**
     * 专业ID
     */
    private Long id;

    /**
     * 专业代码
     */
    private String code;

    /**
     * 专业名称
     */
    private String name;

    /**
     * 院系ID
     */
    private Long departmentId;

    /**
     * 院系名称
     */
    private String departmentName;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

