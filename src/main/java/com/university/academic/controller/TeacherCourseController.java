package com.university.academic.controller;

import com.university.academic.entity.CourseOffering;
import com.university.academic.entity.CourseSelection;
import com.university.academic.entity.Student;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.CourseOfferingRepository;
import com.university.academic.repository.CourseSelectionRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final CourseSelectionRepository courseSelectionRepository;

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
     * 获取开课班级详情
     *
     * @param authentication 认证信息
     * @param offeringId     开课计划ID
     * @return 开课计划详情
     */
    @GetMapping("/courses/{offeringId}")
    @Transactional(readOnly = true)
    public Result<Map<String, Object>> getCourseDetail(
            Authentication authentication,
            @PathVariable Long offeringId) {
        
        Long teacherId = userDetailsService.getTeacherIdFromAuth(authentication);
        log.info("教师查询开课班级详情: teacherId={}, offeringId={}", teacherId, offeringId);
        
        // 验证开课计划是否存在
        CourseOffering offering = courseOfferingRepository.findById(offeringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "开课计划不存在"));
        
        // 验证该开课计划是否是当前教师授课的
        if (!offering.getTeacher().getId().equals(teacherId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看该课程信息");
        }
        
        // 强制加载关联对象
        offering.getCourse().getName();
        offering.getSemester().getAcademicYear();
        offering.getTeacher().getName();
        
        // 构建返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("id", offering.getId());
        result.put("courseNo", offering.getCourse().getCourseNo());
        result.put("courseName", offering.getCourse().getName());
        result.put("credits", offering.getCourse().getCredits());
        result.put("semesterName", offering.getSemester().getAcademicYear() + 
                   " 第" + offering.getSemester().getSemesterType() + "学期");
        result.put("schedule", offering.getSchedule());
        result.put("location", offering.getLocation());
        result.put("capacity", offering.getCapacity());
        result.put("enrolled", offering.getEnrolled());
        result.put("status", offering.getStatus().name());
        
        return Result.success(result);
    }

    /**
     * 查询开课班级的学生名单
     *
     * @param authentication 认证信息
     * @param offeringId     开课计划ID
     * @return 学生名单列表
     */
    @GetMapping("/courses/{offeringId}/students")
    @Transactional(readOnly = true)
    public Result<List<Map<String, Object>>> getCourseStudents(
            Authentication authentication,
            @PathVariable Long offeringId) {
        
        Long teacherId = userDetailsService.getTeacherIdFromAuth(authentication);
        log.info("教师查询开课班级学生名单: teacherId={}, offeringId={}", teacherId, offeringId);
        
        // 验证开课计划是否存在
        CourseOffering offering = courseOfferingRepository.findById(offeringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "开课计划不存在"));
        
        // 验证该开课计划是否是当前教师授课的
        if (!offering.getTeacher().getId().equals(teacherId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看该班级学生信息");
        }
        
        // 查询该开课班级的所有选课记录（只包含SELECTED状态）
        List<CourseSelection> selections = courseSelectionRepository.findByOfferingId(offeringId)
                .stream()
                .filter(selection -> selection.getStatus() == CourseSelection.SelectionStatus.SELECTED)
                .collect(Collectors.toList());
        
        // 转换为简化的Map格式
        List<Map<String, Object>> studentList = selections.stream()
                .map(selection -> {
                    Student student = selection.getStudent();
                    // 强制加载懒加载属性
                    if (student.getMajor() != null) {
                        student.getMajor().getName();
                    }
                    
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", student.getId());
                    map.put("studentNo", student.getStudentNo());
                    map.put("name", student.getName());
                    map.put("gender", student.getGender() != null ? student.getGender().name() : null);
                    map.put("phone", student.getPhone());
                    map.put("email", student.getEmail());
                    map.put("majorName", student.getMajor() != null ? student.getMajor().getName() : null);
                    map.put("className", student.getClassName());
                    map.put("enrollmentYear", student.getEnrollmentYear());
                    map.put("status", student.getStatus() != null ? student.getStatus().name() : null);
                    return map;
                })
                .collect(Collectors.toList());
        
        log.info("查询到 {} 个学生", studentList.size());
        return Result.success(studentList);
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
