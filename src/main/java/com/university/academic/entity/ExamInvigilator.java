package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 监考安排实体类
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "exam_invigilator", indexes = {
    @Index(name = "idx_invig_room", columnList = "exam_room_id"),
    @Index(name = "idx_invig_teacher", columnList = "teacher_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamInvigilator extends BaseEntity {

    /**
     * 关联考场
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_room_id", nullable = false)
    private ExamRoom examRoom;

    /**
     * 关联教师
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    /**
     * 监考类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvigilatorType type;
}

