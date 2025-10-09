package com.university.academic.controller;

import com.university.academic.entity.Grade;
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
import java.util.List;
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
        
        Student student = studentService.findByUserIdWithDetails(user.getId());
        Map<String, Object> statistics = new HashMap<>();

        // 学生基本信息
        statistics.put("studentName", student.getName());
        statistics.put("studentNo", student.getStudentNo());
        statistics.put("major", student.getMajor() != null ? student.getMajor().getName() : "");
        statistics.put("className", student.getClassName());
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

        // 总体统计
        // 已获得总学分
        List<Grade> publishedGrades = gradeService.findPublishedByStudent(student.getId());
        double totalCredits = publishedGrades.stream()
            .filter(g -> g.getTotalScore() != null && g.getTotalScore().doubleValue() >= 60)
            .mapToDouble(g -> {
                if (g.getCourseSelection() != null && 
                    g.getCourseSelection().getOffering() != null &&
                    g.getCourseSelection().getOffering().getCourse() != null) {
                    return g.getCourseSelection().getOffering().getCourse().getCredits();
                }
                return 0;
            })
            .sum();
        statistics.put("totalCredits", totalCredits);

        // 平均绩点（简化计算：平均分/10-5）
        double avgScore = publishedGrades.stream()
            .filter(g -> g.getTotalScore() != null)
            .mapToDouble(g -> g.getTotalScore().doubleValue())
            .average()
            .orElse(0.0);
        double gpa = avgScore > 0 ? Math.max(0, avgScore / 10.0 - 5.0) : 0.0;
        statistics.put("gpa", Math.round(gpa * 100.0) / 100.0);

        // 已完成课程数
        statistics.put("completedCourses", publishedGrades.size());

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

