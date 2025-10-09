package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考场数据传输对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamRoomDTO {

    /**
     * 考场ID
     */
    private Long id;

    /**
     * 考试ID
     */
    private Long examId;

    /**
     * 考场名称
     */
    private String roomName;

    /**
     * 考场地点
     */
    private String location;

    /**
     * 考场容量
     */
    private Integer capacity;

    /**
     * 已分配人数
     */
    private Integer assignedCount;

    /**
     * 剩余容量
     */
    private Integer remainingCapacity;

    /**
     * 学生列表
     */
    private List<ExamRoomStudentDTO> students;

    /**
     * 监考列表
     */
    private List<ExamInvigilatorDTO> invigilators;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

