package com.university.academic.controller;

import com.university.academic.service.*;
import com.university.academic.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 仪表盘控制器
 * 提供统计数据和概览信息
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final StudentService studentService;
    private final TeacherService teacherService;
    private final CourseService courseService;
    private final CourseOfferingService offeringService;
    private final SemesterService semesterService;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getDashboardStatistics() {
        log.info("获取仪表盘统计数据");

        Map<String, Object> statistics = new HashMap<>();

        try {
            // 基础统计
            long studentCount = studentService.countAll();
            long teacherCount = teacherService.countAll();
            long courseCount = courseService.countAll();

            statistics.put("studentCount", studentCount);
            statistics.put("teacherCount", teacherCount);
            statistics.put("courseCount", courseCount);

            // 当前学期统计
            try {
                var activeSemester = semesterService.findActiveSemester();
                statistics.put("activeSemester", Map.of(
                    "id", activeSemester.getId(),
                    "name", activeSemester.getSemesterNameWithWeek(),
                    "academicYear", activeSemester.getAcademicYear(),
                    "semesterType", activeSemester.getSemesterType(),
                    "currentWeek", activeSemester.getCurrentWeek(),
                    "totalWeeks", activeSemester.getTotalWeeks()
                ));

                // 当前学期开课数
                long offeringCount = offeringService.countBySemester(activeSemester.getId());
                statistics.put("offeringCount", offeringCount);

            } catch (Exception e) {
                log.warn("未找到活动学期", e);
                statistics.put("activeSemester", null);
                statistics.put("offeringCount", 0);
            }

            // 最近30天新增学生数（模拟数据）
            statistics.put("recentStudents", 15);
            statistics.put("recentTeachers", 3);

            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取仪表盘统计数据失败", e);
            return Result.error("获取统计数据失败");
        }
    }

    /**
     * 获取数据趋势（最近7天）
     */
    @GetMapping("/trends")
    public Result<Map<String, Object>> getDataTrends() {
        log.info("获取数据趋势");

        Map<String, Object> trends = new HashMap<>();

        // 模拟数据 - 实际应该从数据库按日期统计
        trends.put("dates", new String[]{"周一", "周二", "周三", "周四", "周五", "周六", "周日"});
        trends.put("studentLogins", new int[]{120, 132, 101, 134, 90, 230, 210});
        trends.put("courseSelections", new int[]{45, 52, 38, 56, 42, 78, 65});

        return Result.success(trends);
    }
}

