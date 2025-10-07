package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 课程实体类
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "course")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course extends BaseEntity {

    @Column(name = "course_no", nullable = false, unique = true, length = 20)
    private String courseNo;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer credits;

    @Column(nullable = false)
    private Integer hours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CourseType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CourseOffering> offerings = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CoursePrerequisite> prerequisites = new ArrayList<>();

    /**
     * 课程类型枚举
     */
    public enum CourseType {
        REQUIRED("必修"),
        ELECTIVE("选修"),
        PUBLIC("公共");

        private final String description;

        CourseType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}

