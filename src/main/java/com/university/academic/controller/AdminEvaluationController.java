package com.university.academic.controller;

import com.university.academic.annotation.OperationLog;
import com.university.academic.controller.dto.EvaluationPeriodDTO;
import com.university.academic.entity.CourseEvaluation;
import com.university.academic.entity.EvaluationPeriod;
import com.university.academic.entity.TeacherEvaluation;
import com.university.academic.service.*;
import com.university.academic.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员评价控制器
 * 提供管理员端评价管理接口
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/evaluations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminEvaluationController {

    private final EvaluationPeriodService evaluationPeriodService;
    private final CourseEvaluationService courseEvaluationService;
    private final TeacherEvaluationService teacherEvaluationService;
    private final EvaluationStatisticsService evaluationStatisticsService;

    // ==================== 评价周期管理 ====================

    /**
     * 创建评价周期
     */
    @PostMapping("/periods")
    @OperationLog(value = "创建评价周期", type = "CREATE")
    public Result<EvaluationPeriodDTO> createPeriod(@RequestBody PeriodRequest request) {
        log.info("创建评价周期: semesterId={}", request.getSemesterId());

        EvaluationPeriod period = evaluationPeriodService.createPeriod(
                request.getSemesterId(),
                request.getStartTime(),
                request.getEndTime(),
                request.getDescription()
        );

        return Result.success(EvaluationPeriodDTO.fromEntity(period));
    }

    /**
     * 更新评价周期
     */
    @PutMapping("/periods/{id}")
    @OperationLog(value = "更新评价周期", type = "UPDATE")
    public Result<EvaluationPeriodDTO> updatePeriod(
            @PathVariable Long id,
            @RequestBody PeriodRequest request) {
        log.info("更新评价周期: id={}", id);

        EvaluationPeriod period = evaluationPeriodService.updatePeriod(
                id,
                request.getStartTime(),
                request.getEndTime(),
                request.getDescription()
        );

        return Result.success(EvaluationPeriodDTO.fromEntity(period));
    }

    /**
     * 开启/关闭评价周期
     */
    @PutMapping("/periods/{id}/toggle")
    @OperationLog(value = "切换评价周期状态", type = "UPDATE")
    public Result<EvaluationPeriodDTO> togglePeriod(
            @PathVariable Long id,
            @RequestParam Boolean active) {
        log.info("切换评价周期状态: id={}, active={}", id, active);

        EvaluationPeriod period = evaluationPeriodService.togglePeriod(id, active);

        return Result.success(EvaluationPeriodDTO.fromEntity(period));
    }

    /**
     * 查询所有评价周期
     */
    @GetMapping("/periods")
    public Result<List<EvaluationPeriodDTO>> getAllPeriods() {
        log.info("查询所有评价周期");

        List<EvaluationPeriod> periods = evaluationPeriodService.findAll();
        List<EvaluationPeriodDTO> periodDTOs = periods.stream()
                .map(EvaluationPeriodDTO::fromEntity)
                .collect(Collectors.toList());

        return Result.success(periodDTOs);
    }

    /**
     * 查询当前活跃评价周期（公开接口，所有角色可访问）
     */
    @GetMapping("/periods/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'TEACHER')")
    public Result<EvaluationPeriodDTO> getActivePeriod() {
        log.info("查询当前活跃评价周期");

        EvaluationPeriod period = evaluationPeriodService.findActivePeriod();

        return Result.success(period != null ? EvaluationPeriodDTO.fromEntity(period) : null);
    }

    /**
     * 删除评价周期
     */
    @DeleteMapping("/periods/{id}")
    @OperationLog(value = "删除评价周期", type = "DELETE")
    public Result<Void> deletePeriod(@PathVariable Long id) {
        log.info("删除评价周期: id={}", id);

        evaluationPeriodService.deletePeriod(id);

        return Result.success();
    }

    // ==================== 课程评价管理 ====================

    /**
     * 查询某学期的所有课程评价
     */
    @GetMapping("/course")
    public Result<Page<CourseEvaluation>> getCourseEvaluations(
            @RequestParam Long semesterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("查询学期课程评价: semesterId={}", semesterId);

        PageRequest pageRequest = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CourseEvaluation> evaluations = courseEvaluationService
                .getEvaluationsBySemester(semesterId, pageRequest);

        return Result.success(evaluations);
    }

    /**
     * 查询某门课程的评价（带学生信息）
     */
    @GetMapping("/course/{offeringId}")
    public Result<Page<CourseEvaluation>> getCourseEvaluationDetails(
            @PathVariable Long offeringId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("查询课程评价详情: offeringId={}", offeringId);

        PageRequest pageRequest = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CourseEvaluation> evaluations = courseEvaluationService
                .getCourseEvaluationsWithDetails(offeringId, pageRequest);

        return Result.success(evaluations);
    }

    /**
     * 删除课程评价
     */
    @DeleteMapping("/course/{id}")
    @OperationLog(value = "删除课程评价", type = "DELETE")
    public Result<Void> deleteCourseEvaluation(@PathVariable Long id) {
        log.info("删除课程评价: id={}", id);

        courseEvaluationService.deleteEvaluation(id);

        return Result.success();
    }

    // ==================== 教师评价管理 ====================

    /**
     * 查询某位教师的评价（带学生信息）
     */
    @GetMapping("/teacher/{teacherId}")
    public Result<Page<TeacherEvaluation>> getTeacherEvaluationDetails(
            @PathVariable Long teacherId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("查询教师评价详情: teacherId={}", teacherId);

        PageRequest pageRequest = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TeacherEvaluation> evaluations = teacherEvaluationService
                .getTeacherEvaluationsWithDetails(teacherId, pageRequest);

        return Result.success(evaluations);
    }

    /**
     * 删除教师评价
     */
    @DeleteMapping("/teacher/{id}")
    @OperationLog(value = "删除教师评价", type = "DELETE")
    public Result<Void> deleteTeacherEvaluation(@PathVariable Long id) {
        log.info("删除教师评价: id={}", id);

        teacherEvaluationService.deleteEvaluation(id);

        return Result.success();
    }

    // ==================== 统计分析 ====================

    /**
     * 获取课程评价统计
     */
    @GetMapping("/statistics/course/{offeringId}")
    public Result<EvaluationStatisticsService.CourseEvaluationStatisticsDTO> getCourseStatistics(
            @PathVariable Long offeringId) {
        log.info("查询课程评价统计: offeringId={}", offeringId);

        EvaluationStatisticsService.CourseEvaluationStatisticsDTO statistics = 
                evaluationStatisticsService.getCourseStatistics(offeringId);

        return Result.success(statistics);
    }

    /**
     * 获取教师评价统计
     */
    @GetMapping("/statistics/teacher/{teacherId}")
    public Result<EvaluationStatisticsService.TeacherEvaluationStatisticsDTO> getTeacherStatistics(
            @PathVariable Long teacherId,
            @RequestParam(required = false) Long semesterId) {
        log.info("查询教师评价统计: teacherId={}, semesterId={}", teacherId, semesterId);

        EvaluationStatisticsService.TeacherEvaluationStatisticsDTO statistics = 
                evaluationStatisticsService.getTeacherStatistics(teacherId, semesterId);

        return Result.success(statistics);
    }

    /**
     * 生成教学质量报告
     */
    @GetMapping("/reports/quality/{semesterId}")
    @OperationLog(value = "生成教学质量报告", type = "QUERY")
    public Result<EvaluationStatisticsService.TeachingQualityReportDTO> generateQualityReport(
            @PathVariable Long semesterId) {
        log.info("生成教学质量报告: semesterId={}", semesterId);

        EvaluationStatisticsService.TeachingQualityReportDTO report = 
                evaluationStatisticsService.generateQualityReport(semesterId);

        return Result.success(report);
    }

    /**
     * 获取参与率统计
     */
    @GetMapping("/statistics/participation/{semesterId}")
    public Result<EvaluationStatisticsService.ParticipationStatisticsDTO> getParticipationStatistics(
            @PathVariable Long semesterId) {
        log.info("查询参与率统计: semesterId={}", semesterId);

        EvaluationStatisticsService.ParticipationStatisticsDTO statistics = 
                evaluationStatisticsService.getParticipationStatistics(semesterId);

        return Result.success(statistics);
    }

    /**
     * 评价周期请求DTO
     */
    @lombok.Data
    public static class PeriodRequest {
        private Long semesterId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String description;
    }
}

