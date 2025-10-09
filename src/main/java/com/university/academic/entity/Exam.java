package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 考试实体类
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "exam", indexes = {
    @Index(name = "idx_exam_offering", columnList = "course_offering_id"),
    @Index(name = "idx_exam_time", columnList = "exam_time"),
    @Index(name = "idx_exam_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam extends BaseEntity {

    /**
     * 考试名称（如：高等数学期末考试）
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * 考试类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExamType type;

    /**
     * 关联开课计划
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_offering_id", nullable = false)
    private CourseOffering courseOffering;

    /**
     * 考试时间
     */
    @Column(name = "exam_time", nullable = false)
    private LocalDateTime examTime;

    /**
     * 考试时长（分钟）
     */
    @Column(nullable = false)
    private Integer duration;

    /**
     * 总分
     */
    @Column(name = "total_score", nullable = false)
    @Builder.Default
    private Integer totalScore = 100;

    /**
     * 考试状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ExamStatus status = ExamStatus.DRAFT;

    /**
     * 考试说明
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 考场列表
     */
    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<ExamRoom> examRooms = new ArrayList<>();

    /**
     * 检查是否有考场
     */
    public boolean hasRooms() {
        return examRooms != null && !examRooms.isEmpty();
    }

    /**
     * 获取考场总数
     */
    public int getTotalRooms() {
        return examRooms != null ? examRooms.size() : 0;
    }

    /**
     * 获取学生总数
     */
    public int getTotalStudents() {
        if (examRooms == null) {
            return 0;
        }
        return examRooms.stream()
                .mapToInt(ExamRoom::getAssignedCount)
                .sum();
    }

    /**
     * 检查考试是否可以修改
     */
    public boolean isEditable() {
        return status == ExamStatus.DRAFT;
    }

    /**
     * 检查考试是否可以取消
     */
    public boolean isCancellable() {
        return status == ExamStatus.DRAFT || status == ExamStatus.PUBLISHED;
    }
}

