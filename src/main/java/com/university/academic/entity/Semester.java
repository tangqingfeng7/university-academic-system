package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 学期实体类
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "semester")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Semester extends BaseEntity {

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Column(name = "semester_type", nullable = false)
    private Integer semesterType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "course_selection_start", nullable = false)
    private LocalDateTime courseSelectionStart;

    @Column(name = "course_selection_end", nullable = false)
    private LocalDateTime courseSelectionEnd;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = false;

    @OneToMany(mappedBy = "semester", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CourseOffering> offerings = new ArrayList<>();

    /**
     * 获取学期名称
     */
    public String getSemesterName() {
        String semesterName = semesterType == 1 ? "春季学期" : "秋季学期";
        return academicYear + " " + semesterName;
    }
}

