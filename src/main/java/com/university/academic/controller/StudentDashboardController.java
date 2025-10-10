package com.university.academic.controller;

import com.university.academic.entity.Semester;
import com.university.academic.entity.Student;
import com.university.academic.entity.User;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.UserRepository;
import com.university.academic.service.CourseSelectionService;
import com.university.academic.service.GradeService;
import com.university.academic.service.SemesterService;
import com.university.academic.service.StudentService;
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
import java.util.Map;

/**
 * 学生端仪表盘控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/student/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentDashboardController {

    private final StudentService studentService;
    private final CourseSelectionService selectionService;
    private final GradeService gradeService;
    private final SemesterService semesterService;
    private final UserRepository userRepository;

    /**
     * 获取学生仪表盘统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getDashboardStatistics(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        log.info("获取学生仪表盘统计数据: userId={}", user.getId());
        
        Map<String, Object> statistics = new HashMap<>();

        try {
            Student student = studentService.findByUserIdWithDetails(user.getId());
            
            // 学生基本信息
            statistics.put("studentName", student.getName());
            statistics.put("studentNo", student.getStudentNo());
            statistics.put("major", student.getMajor() != null ? student.getMajor().getName() : "");
            statistics.put("className", student.getClassName() != null ? student.getClassName() : "");
            statistics.put("enrollmentYear", student.getEnrollmentYear());

            // 当前学期统计
            try {
                Semester activeSemester = semesterService.findActiveSemester();
                statistics.put("activeSemester", Map.of(
                    "id", activeSemester.getId(),
                    "name", activeSemester.getSemesterNameWithWeek(),
                    "academicYear", activeSemester.getAcademicYear(),
                    "semesterType", activeSemester.getSemesterType(),
                    "currentWeek", activeSemester.getCurrentWeek(),
                    "totalWeeks", activeSemester.getTotalWeeks()
                ));

                // 本学期已选课程数
                long currentCourses = selectionService.countByStudentAndSemester(
                    student.getId(), activeSemester.getId()
                );
                statistics.put("currentCourses", currentCourses);

            } catch (Exception e) {
                log.warn("未找到活动学期", e);
                statistics.put("activeSemester", null);
                statistics.put("currentCourses", 0);
            }

            // 总体统计 - 使用Service层方法，确保在事务内完成所有操作
            Map<String, Object> gradeStatistics = gradeService.getStudentDashboardStatistics(student.getId());
            statistics.putAll(gradeStatistics);

            // 欢迎信息
            String greeting = getGreeting();
            statistics.put("greeting", greeting);
            statistics.put("currentDate", LocalDate.now().toString());
            
        } catch (Exception e) {
            log.error("获取学生仪表盘统计数据失败", e);
            // 返回默认值
            statistics.put("studentName", "");
            statistics.put("studentNo", "");
            statistics.put("major", "");
            statistics.put("className", "");
            statistics.put("enrollmentYear", 0);
            statistics.put("activeSemester", null);
            statistics.put("currentCourses", 0);
            statistics.put("totalCredits", 0.0);
            statistics.put("gpa", 0.0);
            statistics.put("completedCourses", 0);
            statistics.put("greeting", getGreeting());
            statistics.put("currentDate", LocalDate.now().toString());
        }

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

