package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 考场学生分配实体类
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "exam_room_student",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_room_student",
           columnNames = {"exam_room_id", "student_id"}
       ),
       indexes = {
           @Index(name = "idx_ers_room", columnList = "exam_room_id"),
           @Index(name = "idx_ers_student", columnList = "student_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamRoomStudent extends BaseEntity {

    /**
     * 关联考场
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_room_id", nullable = false)
    private ExamRoom examRoom;

    /**
     * 关联学生
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * 座位号
     */
    @Column(name = "seat_number", nullable = false, length = 20)
    private String seatNumber;
}

