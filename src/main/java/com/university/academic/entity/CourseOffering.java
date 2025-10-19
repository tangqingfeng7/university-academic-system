package com.university.academic.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 开课计划实体类
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "course_offering")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"selections", "schedules", "exams", "evaluations", "hibernateLazyInitializer", "handler"})
public class CourseOffering extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    @JsonIgnoreProperties({"offerings", "hibernateLazyInitializer", "handler"})
    private Semester semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnoreProperties({"prerequisites", "hibernateLazyInitializer", "handler"})
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    @JsonIgnoreProperties({"offerings", "user", "hibernateLazyInitializer", "handler"})
    private Teacher teacher;

    @Column(columnDefinition = "JSON")
    private String schedule;

    @Column(length = 100)
    private String location;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    @Builder.Default
    private Integer enrolled = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OfferingStatus status = OfferingStatus.DRAFT;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 0;

    @OneToMany(mappedBy = "offering", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CourseSelection> selections = new ArrayList<>();

    /**
     * 开课状态枚举
     */
    public enum OfferingStatus {
        DRAFT("草稿"),
        PUBLISHED("已发布"),
        CANCELLED("已取消");

        private final String description;

        OfferingStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 检查是否还有名额
     */
    public boolean hasCapacity() {
        return enrolled < capacity;
    }

    /**
     * 获取剩余名额
     */
    public int getRemainingCapacity() {
        return capacity - enrolled;
    }
}

