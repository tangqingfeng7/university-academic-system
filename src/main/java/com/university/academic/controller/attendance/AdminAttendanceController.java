package com.university.academic.controller.attendance;

import com.university.academic.entity.attendance.AttendanceConfig;
import com.university.academic.entity.attendance.AttendanceWarning;
import com.university.academic.entity.attendance.WarningStatus;
import com.university.academic.service.attendance.AttendanceConfigService;
import com.university.academic.service.attendance.AttendanceExportService;
import com.university.academic.service.attendance.AttendanceStatisticsService;
import com.university.academic.service.attendance.AttendanceWarningService;
import com.university.academic.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 管理员端考勤Controller
 * 提供考勤统计、预警管理、配置管理等接口
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAttendanceController {

    private final AttendanceStatisticsService statisticsService;
    private final AttendanceWarningService warningService;
    private final AttendanceConfigService configService;
    private final AttendanceExportService exportService;

    /**
     * 获取院系考勤统计
     */
    @GetMapping("/statistics/department")
    public Result<Map<String, Object>> getDepartmentStatistics(
            @RequestParam Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("获取院系统计: departmentId={}, startDate={}, endDate={}", 
                departmentId, startDate, endDate);
        
        Map<String, Object> statistics = statisticsService.getDepartmentStatistics(
                departmentId, startDate, endDate);
        
        return Result.success(statistics);
    }

    /**
     * 获取教师考勤统计
     */
    @GetMapping("/statistics/teacher")
    public Result<Map<String, Object>> getTeacherStatistics(
            @RequestParam Long teacherId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("获取教师统计: teacherId={}, startDate={}, endDate={}", 
                teacherId, startDate, endDate);
        
        Map<String, Object> statistics;
        if (startDate != null && endDate != null) {
            statistics = statisticsService.getTeacherStatistics(teacherId, startDate, endDate);
        } else {
            statistics = statisticsService.getTeacherStatistics(teacherId);
        }
        
        return Result.success(statistics);
    }

    /**
     * 获取课程考勤统计
     */
    @GetMapping("/statistics/course/{offeringId}")
    public Result<Map<String, Object>> getCourseStatistics(@PathVariable Long offeringId) {
        log.info("获取课程统计: offeringId={}", offeringId);
        
        Map<String, Object> statistics = statisticsService.getCourseStatistics(offeringId);
        
        return Result.success(statistics);
    }

    /**
     * 获取预警列表
     */
    @GetMapping("/warnings")
    public Result<List<AttendanceWarning>> getWarnings(
            @RequestParam(required = false) WarningStatus status) {
        log.info("获取预警列表: status={}", status);
        
        List<AttendanceWarning> warnings = warningService.getWarningList(status);
        
        return Result.success(warnings);
    }

    /**
     * 处理预警
     */
    @PostMapping("/warnings/{id}/handle")
    public Result<Void> handleWarning(
            @PathVariable Long id,
            @RequestParam(required = false) String comment) {
        log.info("处理预警: warningId={}", id);
        
        warningService.handleWarning(id, comment);
        
        return Result.success(null);
    }

    /**
     * 忽略预警
     */
    @PostMapping("/warnings/{id}/ignore")
    public Result<Void> ignoreWarning(@PathVariable Long id) {
        log.info("忽略预警: warningId={}", id);
        
        warningService.ignoreWarning(id);
        
        return Result.success(null);
    }

    /**
     * 获取所有配置
     */
    @GetMapping("/config")
    public Result<List<AttendanceConfig>> getAllConfigs() {
        log.info("获取所有配置");
        
        List<AttendanceConfig> configs = configService.getAllConfigs();
        
        return Result.success(configs);
    }

    /**
     * 获取配置Map
     */
    @GetMapping("/config/map")
    public Result<Map<String, String>> getConfigsMap() {
        log.info("获取配置Map");
        
        Map<String, String> configs = configService.getAllConfigsMap();
        
        return Result.success(configs);
    }

    /**
     * 更新单个配置
     */
    @PutMapping("/config/{key}")
    public Result<AttendanceConfig> updateConfig(
            @PathVariable String key,
            @RequestParam String value) {
        log.info("更新配置: key={}, value={}", key, value);
        
        AttendanceConfig config = configService.updateConfig(key, value);
        
        return Result.success(config);
    }

    /**
     * 批量更新配置
     */
    @PutMapping("/config")
    public Result<Void> updateConfigs(@RequestBody Map<String, String> configs) {
        log.info("批量更新配置: count={}", configs.size());
        
        configService.updateConfigs(configs);
        
        return Result.success(null);
    }

    /**
     * 刷新配置缓存
     */
    @PostMapping("/config/refresh")
    public Result<Void> refreshConfigCache() {
        log.info("刷新配置缓存");
        
        configService.refreshCache();
        
        return Result.success(null);
    }

    /**
     * 刷新统计缓存
     */
    @PostMapping("/statistics/refresh/{offeringId}")
    public Result<Void> refreshStatisticsCache(@PathVariable Long offeringId) {
        log.info("刷新统计缓存: offeringId={}", offeringId);
        
        statisticsService.refreshStatisticsCache(offeringId);
        
        return Result.success(null);
    }

    /**
     * 手动执行预警检测
     */
    @PostMapping("/warnings/check")
    public Result<Void> executeWarningCheck() {
        log.info("手动执行预警检测");
        
        warningService.executeAllWarningChecks();
        
        return Result.success(null);
    }

    /**
     * 导出院系考勤统计
     */
    @PostMapping("/export/department")
    public Result<byte[]> exportDepartmentStatistics(
            @RequestParam Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("导出院系统计: departmentId={}, startDate={}, endDate={}", 
                departmentId, startDate, endDate);
        
        byte[] data = exportService.exportDepartmentStatistics(departmentId, startDate, endDate);
        
        return Result.success(data);
    }

    /**
     * 导出教师考勤统计
     */
    @PostMapping("/export/teacher")
    public Result<byte[]> exportTeacherStatistics(
            @RequestParam Long teacherId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("导出教师统计: teacherId={}, startDate={}, endDate={}", 
                teacherId, startDate, endDate);
        
        byte[] data = exportService.exportTeacherStatistics(teacherId, startDate, endDate);
        
        return Result.success(data);
    }

    /**
     * 生成月度考勤报告
     */
    @PostMapping("/report/monthly")
    public Result<byte[]> generateMonthlyReport(
            @RequestParam Long departmentId,
            @RequestParam Integer year,
            @RequestParam Integer month) {
        log.info("生成月度报告: departmentId={}, year={}, month={}", 
                departmentId, year, month);
        
        byte[] data = exportService.generateMonthlyReport(departmentId, year, month);
        
        return Result.success(data);
    }
}


