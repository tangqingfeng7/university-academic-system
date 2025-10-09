package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 考场实体类
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "exam_room", indexes = {
    @Index(name = "idx_room_exam", columnList = "exam_id"),
    @Index(name = "idx_room_location", columnList = "location")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamRoom extends BaseEntity {

    /**
     * 关联考试
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    /**
     * 考场名称（如：教学楼A101）
     */
    @Column(name = "room_name", nullable = false, length = 100)
    private String roomName;

    /**
     * 考场地点
     */
    @Column(nullable = false, length = 200)
    private String location;

    /**
     * 考场容量
     */
    @Column(nullable = false)
    private Integer capacity;

    /**
     * 实际安排人数
     */
    @Column(name = "assigned_count", nullable = false)
    @Builder.Default
    private Integer assignedCount = 0;

    /**
     * 版本号（用于乐观锁，防止并发更新冲突）
     */
    @Version
    private Long version;

    /**
     * 学生分配列表
     */
    @OneToMany(mappedBy = "examRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<ExamRoomStudent> students = new ArrayList<>();

    /**
     * 监考安排列表
     */
    @OneToMany(mappedBy = "examRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<ExamInvigilator> invigilators = new ArrayList<>();

    /**
     * 检查是否还有名额
     */
    public boolean hasCapacity() {
        return assignedCount < capacity;
    }

    /**
     * 获取剩余名额
     */
    public int getRemainingCapacity() {
        return capacity - assignedCount;
    }

    /**
     * 检查是否已满
     */
    public boolean isFull() {
        return assignedCount >= capacity;
    }

    /**
     * 增加已分配人数
     */
    public void incrementAssignedCount() {
        this.assignedCount++;
    }

    /**
     * 减少已分配人数
     */
    public void decrementAssignedCount() {
        if (this.assignedCount > 0) {
            this.assignedCount--;
        }
    }
}

