package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 选课记录实体类
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "course_selection",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_offering_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSelection extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_offering_id", nullable = false)
    private CourseOffering offering;

    @Column(name = "selection_time", nullable = false)
    @Builder.Default
    private LocalDateTime selectionTime = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SelectionStatus status = SelectionStatus.SELECTED;

    @OneToOne(mappedBy = "courseSelection", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Grade grade;

    /**
     * 选课状态枚举
     */
    public enum SelectionStatus {
        SELECTED("已选"),
        DROPPED("已退"),
        COMPLETED("已完成");

        private final String description;

        SelectionStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}

