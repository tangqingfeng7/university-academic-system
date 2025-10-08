package com.university.academic.controller;

import com.university.academic.entity.CourseOffering;
import com.university.academic.entity.Semester;
import com.university.academic.entity.Teacher;
import com.university.academic.entity.User;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.UserRepository;
import com.university.academic.service.CourseOfferingService;
import com.university.academic.service.GradeService;
import com.university.academic.service.SemesterService;
import com.university.academic.service.TeacherService;
import com.university.academic.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 教师端仪表盘控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherDashboardController {

    private final TeacherService teacherService;
    private final CourseOfferingService offeringService;
    private final SemesterService semesterService;
    private final GradeService gradeService;
    private final UserRepository userRepository;

    /**
     * 获取教师仪表盘统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getDashboardStatistics(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        log.info("获取教师仪表盘统计数据: userId={}", user.getId());
        
        Teacher teacher = teacherService.findByUserId(user.getId());
        Map<String, Object> statistics = new HashMap<>();

        // 教师基本信息
        statistics.put("teacherName", teacher.getName());
        statistics.put("teacherNo", teacher.getTeacherNo());
        statistics.put("department", teacher.getDepartment() != null ? teacher.getDepartment().getName() : "");
        statistics.put("title", teacher.getTitle());

        // 当前学期统计
        try {
            Semester activeSemester = semesterService.findActiveSemester();
            statistics.put("activeSemester", Map.of(
                "id", activeSemester.getId(),
                "name", activeSemester.getSemesterName(),
                "academicYear", activeSemester.getAcademicYear(),
                "semesterType", activeSemester.getSemesterType()
            ));

            // 获取本学期授课班级
            List<CourseOffering> offerings = offeringService.findByTeacherAndSemester(
                teacher.getId(), activeSemester.getId()
            );
            
            statistics.put("courseCount", offerings.size());
            
            // 计算总学生数
            int totalStudents = offerings.stream()
                .mapToInt(CourseOffering::getEnrolled)
                .sum();
            statistics.put("studentCount", totalStudents);

            // 待批改成绩数（状态为DRAFT的成绩）
            long pendingGrades = gradeService.countPendingGradesByTeacher(teacher.getId());
            statistics.put("pendingGrades", pendingGrades);

        } catch (Exception e) {
            log.warn("未找到活动学期", e);
            statistics.put("activeSemester", null);
            statistics.put("courseCount", 0);
            statistics.put("studentCount", 0);
            statistics.put("pendingGrades", 0);
        }

        // 欢迎信息
        String greeting = getGreeting();
        statistics.put("greeting", greeting);
        statistics.put("currentDate", LocalDate.now().toString());

        return Result.success(statistics);
    }

    private String getGreeting() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 12) {
            return "上午好";
        } else if (hour < 18) {
            return "下午好";
        } else {
            return "晚上好";
        }
    }
}

