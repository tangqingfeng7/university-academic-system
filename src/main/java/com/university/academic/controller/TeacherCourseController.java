package com.university.academic.controller;

import com.university.academic.entity.CourseOffering;
import com.university.academic.repository.CourseOfferingRepository;
import com.university.academic.security.CustomUserDetailsService;
import com.university.academic.vo.Result;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 教师课程控制器
 * 提供教师查询自己授课列表的接口
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherCourseController {

    private final CustomUserDetailsService userDetailsService;
    private final CourseOfferingRepository courseOfferingRepository;

    /**
     * 查询教师的授课列表
     *
     * @param authentication 认证信息
     * @param semesterId     学期ID（可选）
     * @return 开课计划列表
     */
    @GetMapping("/courses")
    @Transactional(readOnly = true)
    public Result<List<CourseOfferingDTO>> getMyCourses(
            Authentication authentication,
            @RequestParam(required = false) Long semesterId) {

        Long teacherId = userDetailsService.getTeacherIdFromAuth(authentication);
        log.info("查询教师授课列表: teacherId={}, semesterId={}", teacherId, semesterId);

        List<CourseOffering> offerings;
        if (semesterId != null) {
            offerings = courseOfferingRepository.findByTeacherIdAndSemesterId(teacherId, semesterId);
        } else {
            offerings = courseOfferingRepository.findByTeacherId(teacherId);
        }

        // 强制加载关联对象，避免 LazyInitializationException
        offerings.forEach(offering -> {
            offering.getCourse().getName();
            offering.getCourse().getType(); // 加载课程类型
            offering.getSemester().getAcademicYear();
            offering.getTeacher().getName();
            offering.getStatus(); // 加载开课状态
        });

        List<CourseOfferingDTO> dtoList = offerings.stream()
                .map(CourseOfferingDTO::fromEntity)
                .collect(Collectors.toList());

        log.info("查询到 {} 门课程", dtoList.size());
        return Result.success(dtoList);
    }

    /**
     * 开课计划 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseOfferingDTO {
        private Long id;
        private CourseDTO course;
        private SemesterDTO semester;
        private TeacherDTO teacher;
        private String schedule;
        private String location;
        private Integer capacity;
        private Integer enrolled;
        private String status; // 开课状态：DRAFT, PUBLISHED, CANCELLED

        public static CourseOfferingDTO fromEntity(CourseOffering entity) {
            return CourseOfferingDTO.builder()
                    .id(entity.getId())
                    .course(CourseDTO.builder()
                            .id(entity.getCourse().getId())
                            .courseNo(entity.getCourse().getCourseNo())
                            .name(entity.getCourse().getName())
                            .credits(entity.getCourse().getCredits())
                            .type(entity.getCourse().getType().name())
                            .build())
                    .semester(SemesterDTO.builder()
                            .id(entity.getSemester().getId())
                            .academicYear(entity.getSemester().getAcademicYear())
                            .semesterType(entity.getSemester().getSemesterType())
                            .startDate(entity.getSemester().getStartDate())
                            .endDate(entity.getSemester().getEndDate())
                            .build())
                    .teacher(TeacherDTO.builder()
                            .id(entity.getTeacher().getId())
                            .name(entity.getTeacher().getName())
                            .teacherNo(entity.getTeacher().getTeacherNo())
                            .build())
                    .schedule(entity.getSchedule())
                    .location(entity.getLocation())
                    .capacity(entity.getCapacity())
                    .enrolled(entity.getEnrolled())
                    .status(entity.getStatus().name())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseDTO {
        private Long id;
        private String courseNo;
        private String name;
        private Integer credits;
        private String type; // 课程类型：REQUIRED, ELECTIVE, PUBLIC
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SemesterDTO {
        private Long id;
        private String academicYear;
        private Integer semesterType;
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherDTO {
        private Long id;
        private String teacherNo;
        private String name;
    }
}
