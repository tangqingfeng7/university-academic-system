package com.university.academic.controller.attendance;

import com.university.academic.dto.attendance.*;
import com.university.academic.entity.attendance.*;
import com.university.academic.security.SecurityUtils;
import com.university.academic.service.attendance.*;
import com.university.academic.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 学生端考勤Controller
 * 提供学生考勤查询和签到相关接口
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/student/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentAttendanceController {

    private final QRCodeAttendanceService qrCodeService;
    private final LocationAttendanceService locationService;
    private final AttendanceService attendanceService;
    private final AttendanceStatisticsService statisticsService;
    private final AttendanceAppealService appealService;

    /**
     * 扫码签到
     */
    @PostMapping("/checkin/qrcode")
    public Result<AttendanceDetail> qrCodeCheckin(@Valid @RequestBody QRCodeCheckinDTO dto) {
        Long studentId = SecurityUtils.getCurrentStudentId();
        log.info("学生扫码签到: studentId={}, qrToken={}", studentId, dto.getQrToken());
        
        AttendanceDetail detail = qrCodeService.checkIn(dto.getQrToken(), studentId);
        
        return Result.success(detail);
    }

    /**
     * 定位签到
     */
    @PostMapping("/checkin/location")
    public Result<AttendanceDetail> locationCheckin(@Valid @RequestBody LocationCheckinDTO dto) {
        Long studentId = SecurityUtils.getCurrentStudentId();
        log.info("学生定位签到: studentId={}, recordId={}, lat={}, lon={}", 
                studentId, dto.getRecordId(), dto.getLatitude(), dto.getLongitude());
        
        AttendanceDetail detail = locationService.checkIn(
                dto.getRecordId(), 
                studentId, 
                dto.getLatitude(), 
                dto.getLongitude()
        );
        
        return Result.success(detail);
    }

    /**
     * 获取学生在某课程的考勤记录
     */
    @GetMapping("/records")
    public Result<List<AttendanceDetailDTO>> getAttendanceRecords(
            @RequestParam(required = false) Long offeringId) {
        Long studentId = SecurityUtils.getCurrentStudentId();
        
        if (offeringId != null) {
            log.info("获取学生课程考勤记录: studentId={}, offeringId={}", studentId, offeringId);
            List<AttendanceDetail> records = attendanceService.getStudentAttendanceList(
                    studentId, offeringId);
            List<AttendanceDetailDTO> dtos = records.stream()
                    .map(AttendanceDetailDTO::fromEntity)
                    .toList();
            return Result.success(dtos);
        } else {
            log.info("获取学生所有考勤记录: studentId={}", studentId);
            // 这里可以扩展为获取所有课程的考勤记录
            return Result.success(List.of());
        }
    }

    /**
     * 获取学生在某课程的考勤统计
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getAttendanceStatistics(
            @RequestParam Long offeringId) {
        Long studentId = SecurityUtils.getCurrentStudentId();
        log.info("获取学生考勤统计: studentId={}, offeringId={}", studentId, offeringId);
        
        Map<String, Object> statistics = statisticsService.getStudentStatistics(
                studentId, offeringId);
        
        return Result.success(statistics);
    }

    /**
     * 获取学生在某学期的所有课程考勤统计
     */
    @GetMapping("/statistics/semester")
    public Result<List<Map<String, Object>>> getSemesterStatistics(
            @RequestParam Long semesterId) {
        Long studentId = SecurityUtils.getCurrentStudentId();
        log.info("获取学生学期统计: studentId={}, semesterId={}", studentId, semesterId);
        
        List<Map<String, Object>> statistics = statisticsService.getStudentSemesterStatistics(
                studentId, semesterId);
        
        return Result.success(statistics);
    }

    /**
     * 提交补签申请
     */
    @PostMapping("/makeup")
    public Result<AttendanceRequest> submitMakeupRequest(@Valid @RequestBody AppealRequestDTO dto) {
        Long studentId = SecurityUtils.getCurrentStudentId();
        log.info("提交补签申请: studentId={}, detailId={}", studentId, dto.getDetailId());
        
        AttendanceRequest request = appealService.submitMakeupRequest(
                studentId, 
                dto.getDetailId(), 
                dto.getReason(), 
                dto.getAttachmentUrl()
        );
        
        return Result.success(request);
    }

    /**
     * 提交考勤申诉
     */
    @PostMapping("/appeal")
    public Result<AttendanceRequest> submitAppeal(@Valid @RequestBody AppealRequestDTO dto) {
        Long studentId = SecurityUtils.getCurrentStudentId();
        log.info("提交考勤申诉: studentId={}, detailId={}", studentId, dto.getDetailId());
        
        AttendanceRequest request = appealService.submitAppeal(
                studentId, 
                dto.getDetailId(), 
                dto.getReason(), 
                dto.getAttachmentUrl()
        );
        
        return Result.success(request);
    }

    /**
     * 获取学生的申请列表
     */
    @GetMapping("/requests")
    public Result<List<AttendanceRequest>> getRequests(
            @RequestParam(required = false) RequestStatus status) {
        Long studentId = SecurityUtils.getCurrentStudentId();
        log.info("获取学生申请列表: studentId={}, status={}", studentId, status);
        
        List<AttendanceRequest> requests = appealService.getStudentRequests(studentId, status);
        
        return Result.success(requests);
    }

    /**
     * 获取申请详情
     */
    @GetMapping("/requests/{id}")
    public Result<AttendanceRequest> getRequestDetails(@PathVariable Long id) {
        log.info("获取申请详情: requestId={}", id);
        
        AttendanceRequest request = appealService.getRequestDetails(id);
        
        return Result.success(request);
    }

    /**
     * 取消申请
     */
    @DeleteMapping("/requests/{id}")
    public Result<Void> cancelRequest(@PathVariable Long id) {
        log.info("取消申请: requestId={}", id);
        
        appealService.cancelRequest(id);
        
        return Result.success(null);
    }
}

