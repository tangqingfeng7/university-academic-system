package com.university.academic.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    /**
     * 选课功能是否启用（管理员可控制）
     */
    @Column(name = "course_selection_enabled", nullable = false)
    @Builder.Default
    private Boolean courseSelectionEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = false;

    @OneToMany(mappedBy = "semester", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnoreProperties({"semester", "schedules", "selections", "exams", "evaluations"})
    private List<CourseOffering> offerings = new ArrayList<>();

    /**
     * 获取学期名称
     */
    public String getSemesterName() {
        String semesterName = semesterType == 1 ? "春季学期" : "秋季学期";
        return academicYear + " " + semesterName;
    }

    /**
     * 获取当前周次
     */
    public Integer getCurrentWeek() {
        LocalDate now = LocalDate.now();
        
        // 如果当前日期在学期开始前，返回0
        if (now.isBefore(startDate)) {
            return 0;
        }
        
        // 如果当前日期在学期结束后，返回总周数
        if (now.isAfter(endDate)) {
            return getTotalWeeks();
        }
        
        // 计算从开学到现在的天数，然后除以7得到周次（向上取整）
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, now);
        return (int) Math.ceil((daysBetween + 1) / 7.0);
    }

    /**
     * 获取学期总周数
     */
    public Integer getTotalWeeks() {
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        return (int) Math.ceil((daysBetween + 1) / 7.0);
    }

    /**
     * 获取带周次的学期名称
     */
    public String getSemesterNameWithWeek() {
        String semesterName = semesterType == 1 ? "春季学期" : "秋季学期";
        Integer currentWeek = getCurrentWeek();
        
        if (currentWeek == 0) {
            return academicYear + " " + semesterName + "（未开学）";
        } else if (currentWeek > getTotalWeeks()) {
            return academicYear + " " + semesterName + "（已结束）";
        } else {
            return academicYear + " " + semesterName + " 第" + currentWeek + "周";
        }
    }

    /**
     * 判断当前是否可以选课
     */
    public boolean isCourseSelectionAvailable() {
        if (!courseSelectionEnabled) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(courseSelectionStart) && !now.isAfter(courseSelectionEnd);
    }

    /**
     * 获取选课状态描述
     */
    public String getCourseSelectionStatus() {
        if (!courseSelectionEnabled) {
            return "选课已关闭";
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(courseSelectionStart)) {
            return "选课未开始";
        } else if (now.isAfter(courseSelectionEnd)) {
            return "选课已结束";
        } else {
            return "选课进行中";
        }
    }
}

