package com.university.academic.controller;

import com.university.academic.entity.*;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.UserRepository;
import com.university.academic.service.*;
import com.university.academic.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private final com.university.academic.repository.ExamRepository examRepository;

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
                "name", activeSemester.getSemesterNameWithWeek(),
                "academicYear", activeSemester.getAcademicYear(),
                "semesterType", activeSemester.getSemesterType(),
                "currentWeek", activeSemester.getCurrentWeek(),
                "totalWeeks", activeSemester.getTotalWeeks()
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

    /**
     * 获取教师日历事件
     */
    @GetMapping("/calendar-events")
    public Result<List<Map<String, String>>> getCalendarEvents(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        log.info("获取教师日历事件: userId={}", user.getId());

        List<Map<String, String>> events = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            Teacher teacher = teacherService.findByUserId(user.getId());
            Semester activeSemester = semesterService.findActiveSemester();
            
            // 获取本学期授课班级
            List<CourseOffering> offerings = offeringService.findByTeacherAndSemester(
                teacher.getId(), activeSemester.getId()
            );

            // 添加授课事件（可以根据schedule字段解析出具体日期）
            for (CourseOffering offering : offerings) {
                // 简化处理，实际应该解析schedule字段
                Map<String, String> event = new HashMap<>();
                event.put("title", offering.getCourse().getName());
                event.put("type", "teaching");
                // 需要根据实际的课程安排生成日期
                events.add(event);
            }

            // 获取考试事件
            List<Exam> exams = examRepository.findByTeacherIdAndSemesterId(teacher.getId(), activeSemester.getId());
            for (Exam exam : exams) {
                if (exam.getExamTime() != null) {
                    Map<String, String> event = new HashMap<>();
                    event.put("date", exam.getExamTime().format(dateFormatter));
                    event.put("type", "exam");
                    event.put("title", exam.getName());
                    events.add(event);
                }
            }

        } catch (Exception e) {
            log.error("获取教师日历事件失败", e);
        }

        return Result.success(events);
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

