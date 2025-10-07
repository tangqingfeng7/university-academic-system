package com.university.academic.controller;

import com.university.academic.annotation.OperationLog;
import com.university.academic.dto.*;
import com.university.academic.service.StatisticsService;
import com.university.academic.util.ExcelUtil;
import com.university.academic.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 统计报表控制器
 * 提供各种统计数据和报表导出功能（管理员端）
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final ExcelUtil excelUtil;

    /**
     * 获取学生统计数据
     */
    @GetMapping("/students")
    public Result<StudentStatisticsDTO> getStudentStatistics() {
        log.info("查询学生统计数据");

        StudentStatisticsDTO statistics = statisticsService.getStudentStatistics();
        return Result.success(statistics);
    }

    /**
     * 获取课程统计数据
     */
    @GetMapping("/courses")
    public Result<CourseStatisticsDTO> getCourseStatistics(
            @RequestParam(required = false) Long semesterId) {
        log.info("查询课程统计数据: semesterId={}", semesterId);

        CourseStatisticsDTO statistics = statisticsService.getCourseStatistics(semesterId);
        return Result.success(statistics);
    }

    /**
     * 获取成绩统计数据
     */
    @GetMapping("/grades")
    public Result<GradeStatisticsDTO> getGradeStatistics(
            @RequestParam(required = false) Long semesterId) {
        log.info("查询成绩统计数据: semesterId={}", semesterId);

        GradeStatisticsDTO statistics = statisticsService.getGradeStatistics(semesterId);
        return Result.success(statistics);
    }

    /**
     * 获取教师工作量统计
     */
    @GetMapping("/teachers")
    public Result<List<TeacherWorkloadDTO>> getTeacherWorkload(
            @RequestParam(required = false) Long semesterId) {
        log.info("查询教师工作量统计: semesterId={}", semesterId);

        List<TeacherWorkloadDTO> workload = statisticsService.getTeacherWorkload(semesterId);
        return Result.success(workload);
    }

    /**
     * 导出学生统计报表（Excel）
     */
    @GetMapping("/students/export")
    @OperationLog("导出学生统计报表")
    public ResponseEntity<byte[]> exportStudentStatistics() {
        log.info("导出学生统计报表");

        StudentStatisticsDTO statistics = statisticsService.getStudentStatistics();

        // 准备表头和数据
        String[] headers = {"类别", "分类", "数量"};
        List<Object[]> dataList = new ArrayList<>();

        // 按专业分布
        statistics.getByMajor().forEach((major, count) -> {
            dataList.add(new Object[]{"按专业分布", major, count});
        });

        // 按年级分布
        statistics.getByGrade().forEach((grade, count) -> {
            dataList.add(new Object[]{"按年级分布", grade + "年级", count});
        });

        // 按性别分布
        statistics.getByGender().forEach((gender, count) -> {
            dataList.add(new Object[]{"按性别分布", gender, count});
        });

        // 按院系分布
        statistics.getByDepartment().forEach((dept, count) -> {
            dataList.add(new Object[]{"按院系分布", dept, count});
        });

        byte[] excelBytes = excelUtil.exportStatisticsReport(
                "学生统计报表", headers, dataList);

        String filename = "学生统计报表_" + LocalDate.now() + ".xlsx";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelBytes);
    }

    /**
     * 导出课程统计报表（Excel）
     */
    @GetMapping("/courses/export")
    @OperationLog("导出课程统计报表")
    public ResponseEntity<byte[]> exportCourseStatistics(
            @RequestParam(required = false) Long semesterId) {
        log.info("导出课程统计报表: semesterId={}", semesterId);

        CourseStatisticsDTO statistics = statisticsService.getCourseStatistics(semesterId);

        String[] headers = {"课程名称", "教师", "容量", "已选", "利用率(%)"};
        List<Object[]> dataList = new ArrayList<>();

        statistics.getCourseDetails().forEach(detail -> {
            dataList.add(new Object[]{
                    detail.getCourseName(),
                    detail.getTeacherName(),
                    detail.getCapacity(),
                    detail.getEnrolled(),
                    detail.getUtilizationRate()
            });
        });

        byte[] excelBytes = excelUtil.exportStatisticsReport(
                "课程统计报表", headers, dataList);

        String filename = "课程统计报表_" + LocalDate.now() + ".xlsx";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelBytes);
    }

    /**
     * 导出成绩统计报表（Excel）
     */
    @GetMapping("/grades/export")
    @OperationLog("导出成绩统计报表")
    public ResponseEntity<byte[]> exportGradeStatistics(
            @RequestParam(required = false) Long semesterId) {
        log.info("导出成绩统计报表: semesterId={}", semesterId);

        GradeStatisticsDTO statistics = statisticsService.getGradeStatistics(semesterId);

        String[] headers = {"课程名称", "教师", "学生人数", "平均分", "及格率(%)", "优秀率(%)", "最高分", "最低分"};
        List<Object[]> dataList = new ArrayList<>();

        statistics.getCourseStats().forEach(stat -> {
            dataList.add(new Object[]{
                    stat.getCourseName(),
                    stat.getTeacherName(),
                    stat.getStudentCount(),
                    stat.getAverageScore(),
                    stat.getPassRate(),
                    stat.getExcellentRate(),
                    stat.getHighestScore(),
                    stat.getLowestScore()
            });
        });

        byte[] excelBytes = excelUtil.exportStatisticsReport(
                "成绩统计报表", headers, dataList);

        String filename = "成绩统计报表_" + LocalDate.now() + ".xlsx";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelBytes);
    }

    /**
     * 导出教师工作量统计报表（Excel）
     */
    @GetMapping("/teachers/export")
    @OperationLog("导出教师工作量统计报表")
    public ResponseEntity<byte[]> exportTeacherWorkload(
            @RequestParam(required = false) Long semesterId) {
        log.info("导出教师工作量统计报表: semesterId={}", semesterId);

        List<TeacherWorkloadDTO> workload = statisticsService.getTeacherWorkload(semesterId);

        String[] headers = {"工号", "姓名", "院系", "职称", "授课门数", "授课班级数", "授课学生数", "总学时", "平均每班人数"};
        List<Object[]> dataList = new ArrayList<>();

        workload.forEach(teacher -> {
            dataList.add(new Object[]{
                    teacher.getTeacherNo(),
                    teacher.getTeacherName(),
                    teacher.getDepartmentName(),
                    teacher.getTitle(),
                    teacher.getCourseCount(),
                    teacher.getOfferingCount(),
                    teacher.getTotalStudents(),
                    teacher.getTotalHours(),
                    teacher.getAverageStudentsPerClass()
            });
        });

        byte[] excelBytes = excelUtil.exportStatisticsReport(
                "教师工作量统计报表", headers, dataList);

        String filename = "教师工作量统计报表_" + LocalDate.now() + ".xlsx";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelBytes);
    }
}

