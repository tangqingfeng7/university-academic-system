package com.university.academic.controller.attendance;

import com.university.academic.dto.attendance.*;
import com.university.academic.entity.attendance.*;
import com.university.academic.security.SecurityUtils;
import com.university.academic.service.attendance.AttendanceAppealService;
import com.university.academic.service.attendance.AttendanceExportService;
import com.university.academic.service.attendance.AttendanceService;
import com.university.academic.service.attendance.AttendanceStatisticsService;
import com.university.academic.service.attendance.LocationAttendanceService;
import com.university.academic.service.attendance.QRCodeAttendanceService;
import com.university.academic.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 教师端考勤Controller
 * 提供教师考勤管理相关接口
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherAttendanceController {

    private final AttendanceService attendanceService;
    private final QRCodeAttendanceService qrCodeService;
    private final LocationAttendanceService locationService;
    private final AttendanceStatisticsService statisticsService;
    private final AttendanceAppealService appealService;
    private final AttendanceExportService exportService;

    /**
     * 开始考勤
     */
    @PostMapping("/start")
    public Result<AttendanceRecord> startAttendance(@Valid @RequestBody StartAttendanceDTO dto) {
        log.info("开始考勤: offeringId={}, method={}", dto.getOfferingId(), dto.getMethod());
        
        AttendanceRecord record = attendanceService.startAttendance(
                dto.getOfferingId(), 
                dto.getMethod()
        );
        
        return Result.success(record);
    }

    /**
     * 记录学生考勤
     */
    @PostMapping("/{id}/record")
    public Result<AttendanceDetailDTO> recordAttendance(
            @PathVariable Long id,
            @Valid @RequestBody RecordAttendanceDTO dto) {
        log.info("记录考勤: recordId={}, studentId={}, status={}", 
                id, dto.getStudentId(), dto.getStatus());
        
        AttendanceDetail detail = attendanceService.recordAttendance(
                id, 
                dto.getStudentId(), 
                dto.getStatus(), 
                dto.getRemarks()
        );
        
        return Result.success(AttendanceDetailDTO.fromEntity(detail));
    }

    /**
     * 提交考勤
     */
    @PutMapping("/{id}/submit")
    public Result<AttendanceRecord> submitAttendance(@PathVariable Long id) {
        log.info("提交考勤: recordId={}", id);
        
        AttendanceRecord record = attendanceService.submitAttendance(id);
        
        return Result.success(record);
    }

    /**
     * 取消考勤
     */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelAttendance(@PathVariable Long id) {
        log.info("取消考勤: recordId={}", id);
        
        attendanceService.cancelAttendance(id);
        
        return Result.success(null);
    }

    /**
     * 更新考勤明细
     */
    @PutMapping("/detail/{id}")
    public Result<AttendanceDetailDTO> updateAttendanceDetail(
            @PathVariable Long id,
            @Valid @RequestBody RecordAttendanceDTO dto) {
        log.info("更新考勤明细: detailId={}, status={}", id, dto.getStatus());
        
        AttendanceDetail detail = attendanceService.updateAttendanceStatus(
                id, 
                dto.getStatus(), 
                dto.getRemarks()
        );
        
        return Result.success(AttendanceDetailDTO.fromEntity(detail));
    }

    /**
     * 获取考勤详情
     */
    @GetMapping("/{id}")
    public Result<AttendanceRecordDTO> getAttendanceRecord(@PathVariable Long id) {
        log.info("获取考勤详情: recordId={}", id);
        
        AttendanceRecord record = attendanceService.getAttendanceRecord(id);
        AttendanceRecordDTO dto = AttendanceRecordDTO.fromEntity(record);
        
        return Result.success(dto);
    }

    /**
     * 获取考勤明细列表
     */
    @GetMapping("/{id}/details")
    public Result<List<AttendanceDetailDTO>> getAttendanceDetails(@PathVariable Long id) {
        log.info("获取考勤明细: recordId={}", id);
        
        List<AttendanceDetail> details = attendanceService.getAttendanceDetails(id);
        List<AttendanceDetailDTO> dtos = details.stream()
                .map(AttendanceDetailDTO::fromEntity)
                .toList();
        
        return Result.success(dtos);
    }

    /**
     * 获取教师的考勤记录列表
     */
    @GetMapping("/list")
    public Result<List<AttendanceRecordDTO>> getAttendanceList() {
        Long teacherId = SecurityUtils.getCurrentTeacherId();
        log.info("获取教师考勤列表: teacherId={}", teacherId);
        
        List<AttendanceRecord> records = attendanceService.getTeacherAttendanceList(teacherId);
        List<AttendanceRecordDTO> dtos = records.stream()
                .map(AttendanceRecordDTO::fromEntity)
                .toList();
        
        return Result.success(dtos);
    }

    /**
     * 获取开课计划的考勤记录
     */
    @GetMapping("/offering/{offeringId}")
    public Result<List<AttendanceRecordDTO>> getOfferingAttendance(@PathVariable Long offeringId) {
        log.info("获取课程考勤列表: offeringId={}", offeringId);
        
        List<AttendanceRecord> records = attendanceService.getOfferingAttendanceList(offeringId);
        List<AttendanceRecordDTO> dtos = records.stream()
                .map(AttendanceRecordDTO::fromEntity)
                .toList();
        
        return Result.success(dtos);
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
     * 获取课程出勤趋势
     */
    @GetMapping("/statistics/trend/{offeringId}")
    public Result<List<Map<String, Object>>> getAttendanceTrend(@PathVariable Long offeringId) {
        log.info("获取出勤趋势: offeringId={}", offeringId);
        
        List<Map<String, Object>> trend = statisticsService.getAttendanceTrend(offeringId);
        
        return Result.success(trend);
    }

    /**
     * 生成签到二维码
     */
    @PostMapping("/qrcode/generate")
    public Result<Map<String, String>> generateQRCode(@RequestParam Long recordId) {
        log.info("生成二维码: recordId={}", recordId);
        
        String qrToken = qrCodeService.generateQRCode(recordId);
        
        return Result.success(Map.of("qrToken", qrToken));
    }

    /**
     * 刷新二维码
     */
    @PostMapping("/qrcode/refresh")
    public Result<Map<String, String>> refreshQRCode(@RequestParam Long recordId) {
        log.info("刷新二维码: recordId={}", recordId);
        
        String qrToken = qrCodeService.refreshQRCode(recordId);
        
        return Result.success(Map.of("qrToken", qrToken));
    }

    /**
     * 设置定位签到
     */
    @PostMapping("/location/set")
    public Result<Void> setGeofence(@Valid @RequestBody SetGeofenceDTO dto) {
        log.info("设置地理围栏: recordId={}, lat={}, lon={}, radius={}", 
                dto.getRecordId(), dto.getLatitude(), dto.getLongitude(), dto.getRadius());
        
        locationService.setGeofence(
                dto.getRecordId(), 
                dto.getLatitude(), 
                dto.getLongitude(), 
                dto.getRadius()
        );
        
        return Result.success(null);
    }

    /**
     * 获取待审批申请列表
     */
    @GetMapping("/requests")
    public Result<List<AttendanceRequest>> getPendingRequests() {
        Long teacherId = SecurityUtils.getCurrentTeacherId();
        log.info("获取待审批申请: teacherId={}", teacherId);
        
        List<AttendanceRequest> requests = appealService.getPendingRequests(teacherId);
        
        return Result.success(requests);
    }

    /**
     * 获取所有申请列表（包括已处理）
     */
    @GetMapping("/requests/all")
    public Result<List<AttendanceRequest>> getAllRequests(
            @RequestParam(required = false) RequestStatus status) {
        Long teacherId = SecurityUtils.getCurrentTeacherId();
        log.info("获取申请列表: teacherId={}, status={}", teacherId, status);
        
        List<AttendanceRequest> requests = appealService.getTeacherRequests(teacherId, status);
        
        return Result.success(requests);
    }

    /**
     * 批准申请
     */
    @PostMapping("/requests/{id}/approve")
    public Result<AttendanceRequest> approveRequest(
            @PathVariable Long id,
            @RequestBody ApprovalDTO dto) {
        log.info("批准申请: requestId={}", id);
        
        AttendanceRequest request = appealService.approveRequest(id, dto.getComment());
        
        return Result.success(request);
    }

    /**
     * 拒绝申请
     */
    @PostMapping("/requests/{id}/reject")
    public Result<AttendanceRequest> rejectRequest(
            @PathVariable Long id,
            @RequestBody ApprovalDTO dto) {
        log.info("拒绝申请: requestId={}", id);
        
        AttendanceRequest request = appealService.rejectRequest(id, dto.getComment());
        
        return Result.success(request);
    }

    /**
     * 导出课程考勤数据
     */
    @PostMapping("/export")
    public Result<byte[]> exportAttendance(@RequestParam Long offeringId) {
        log.info("导出课程考勤: offeringId={}", offeringId);
        
        byte[] data = exportService.exportCourseAttendance(offeringId);
        
        return Result.success(data);
    }
}

