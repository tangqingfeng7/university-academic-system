package com.university.academic.controller;

import com.university.academic.controller.dto.*;
import com.university.academic.entity.ClassroomStatus;
import com.university.academic.security.CustomUserDetailsService;
import com.university.academic.service.*;
import com.university.academic.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员端教室管理控制器
 * 提供教室管理、借用审批、统计报告等功能
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/classrooms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminClassroomController {
    
    private final ClassroomService classroomService;
    private final ClassroomBookingService bookingService;
    private final ClassroomUsageService usageService;
    private final ClassroomStatisticsService statisticsService;
    private final SchedulingApiService schedulingApiService;
    private final CustomUserDetailsService userDetailsService;
    
    // ==================== 教室管理 ====================
    
    /**
     * 创建教室
     */
    @PostMapping
    public Result<ClassroomDTO> createClassroom(
            @Valid @RequestBody CreateClassroomRequest request) {
        
        log.info("创建教室: roomNo={}, building={}", request.getRoomNo(), request.getBuilding());
        
        ClassroomDTO result = classroomService.createClassroom(request);
        return Result.success(result);
    }
    
    /**
     * 更新教室信息
     */
    @PutMapping("/{id}")
    public Result<ClassroomDTO> updateClassroom(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClassroomRequest request) {
        
        log.info("更新教室: id={}", id);
        
        ClassroomDTO result = classroomService.updateClassroom(id, request);
        return Result.success(result);
    }
    
    /**
     * 删除教室
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteClassroom(@PathVariable Long id) {
        log.info("删除教室: id={}", id);
        
        classroomService.deleteClassroom(id);
        return Result.success();
    }
    
    /**
     * 查询教室列表
     */
    @GetMapping
    public Result<Page<ClassroomDTO>> getClassrooms(
            @RequestParam(required = false) String building,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) String roomNo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("查询教室列表: building={}, type={}, status={}", building, type, status);
        
        ClassroomQueryDTO query = new ClassroomQueryDTO();
        // 将空字符串转换为null，避免SQL查询条件错误
        query.setBuilding((building != null && !building.trim().isEmpty()) ? building : null);
        if (type != null && !type.trim().isEmpty()) {
            query.setType(com.university.academic.entity.ClassroomType.valueOf(type));
        }
        if (status != null && !status.trim().isEmpty()) {
            query.setStatus(ClassroomStatus.valueOf(status));
        }
        query.setMinCapacity(minCapacity);
        query.setRoomNo((roomNo != null && !roomNo.trim().isEmpty()) ? roomNo : null);
        
        PageRequest pageable = PageRequest.of(page, size, Sort.by("building", "roomNo"));
        Page<ClassroomDTO> result = classroomService.getClassrooms(query, pageable);
        
        return Result.success(result);
    }
    
    /**
     * 查询教室详情
     */
    @GetMapping("/{id}")
    public Result<ClassroomDTO> getClassroomDetail(@PathVariable Long id) {
        log.info("查询教室详情: id={}", id);
        
        ClassroomDTO result = classroomService.getClassroomById(id);
        return Result.success(result);
    }
    
    /**
     * 更新教室状态
     */
    @PutMapping("/{id}/status")
    public Result<ClassroomDTO> updateClassroomStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        
        log.info("更新教室状态: id={}, status={}", id, status);
        
        ClassroomStatus newStatus = ClassroomStatus.valueOf(status);
        ClassroomDTO result = classroomService.updateClassroomStatus(id, newStatus);
        
        return Result.success(result);
    }
    
    // ==================== 教室使用情况 ====================
    
    /**
     * 查询教室使用情况
     */
    @GetMapping("/{id}/usage")
    public Result<ClassroomUsageDTO> getClassroomUsage(
            @PathVariable Long id,
            @RequestParam String date) {
        
        log.info("查询教室使用情况: classroomId={}, date={}", id, date);
        
        LocalDate queryDate = LocalDate.parse(date);
        ClassroomUsageDTO result = usageService.getClassroomUsage(id, queryDate);
        
        return Result.success(result);
    }
    
    /**
     * 查询教室课表
     */
    @GetMapping("/{id}/schedule")
    public Result<ClassroomScheduleDTO> getClassroomSchedule(
            @PathVariable Long id,
            @RequestParam(required = false) String startDate) {
        
        log.info("查询教室课表: classroomId={}, startDate={}", id, startDate);
        
        ClassroomScheduleDTO result;
        if (startDate != null) {
            result = usageService.generateWeeklySchedule(id, LocalDate.parse(startDate));
        } else {
            result = usageService.getCurrentWeekSchedule(id);
        }
        
        return Result.success(result);
    }
    
    // ==================== 借用申请管理 ====================
    
    /**
     * 查询借用申请列表
     */
    @GetMapping("/bookings")
    public Result<Page<ClassroomBookingDTO>> getBookings(
            @RequestParam(required = false) Long applicantId,
            @RequestParam(required = false) Long classroomId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("查询借用申请列表: status={}", status);
        
        BookingQueryDTO query = new BookingQueryDTO();
        query.setApplicantId(applicantId);
        query.setClassroomId(classroomId);
        if (status != null && !status.trim().isEmpty()) {
            query.setStatus(com.university.academic.entity.BookingStatus.valueOf(status));
        }
        if (startDate != null && !startDate.trim().isEmpty()) {
            query.setStartDate(LocalDateTime.parse(startDate));
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            query.setEndDate(LocalDateTime.parse(endDate));
        }
        
        PageRequest pageable = PageRequest.of(page, size, 
            Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ClassroomBookingDTO> result = bookingService.getBookings(query, pageable);
        
        return Result.success(result);
    }
    
    /**
     * 查询待审批的借用申请
     */
    @GetMapping("/bookings/pending")
    public Result<Page<ClassroomBookingDTO>> getPendingBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("查询待审批的借用申请");
        
        PageRequest pageable = PageRequest.of(page, size, 
            Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<ClassroomBookingDTO> result = bookingService.getPendingBookings(pageable);
        
        return Result.success(result);
    }
    
    /**
     * 查询借用申请详情
     */
    @GetMapping("/bookings/{id}")
    public Result<ClassroomBookingDTO> getBookingDetail(@PathVariable Long id) {
        log.info("查询借用申请详情: bookingId={}", id);
        
        ClassroomBookingDTO result = bookingService.getBookingById(id);
        return Result.success(result);
    }
    
    /**
     * 审批借用申请
     */
    @PutMapping("/bookings/{id}/approve")
    public Result<ClassroomBookingDTO> approveBooking(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequest request,
            Authentication authentication) {
        
        Long approverId = userDetailsService.getUserIdFromAuth(authentication);
        log.info("审批借用申请: bookingId={}, approverId={}, approved={}", 
            id, approverId, request.getApproved());
        
        ClassroomBookingDTO result = bookingService.approveBooking(id, approverId, request);
        return Result.success(result);
    }
    
    // ==================== 统计报告 ====================
    
    /**
     * 统计教室使用率
     */
    @GetMapping("/{id}/utilization")
    public Result<ClassroomUtilizationDTO> getClassroomUtilization(
            @PathVariable Long id,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        log.info("统计教室使用率: classroomId={}, {} - {}", id, startDate, endDate);
        
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        ClassroomUtilizationDTO result = statisticsService.getClassroomUtilization(id, start, end);
        
        return Result.success(result);
    }
    
    /**
     * 生成使用率报告
     */
    @GetMapping("/statistics/report")
    public Result<ClassroomUtilizationReportDTO> generateUtilizationReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String building) {
        
        log.info("生成使用率报告: {} - {}, building={}", startDate, endDate, building);
        
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        
        ClassroomUtilizationReportDTO result;
        if (building != null && !building.isEmpty()) {
            result = statisticsService.generateBuildingReport(building, start, end);
        } else {
            result = statisticsService.generateUtilizationReport(start, end);
        }
        
        return Result.success(result);
    }
    
    /**
     * 查询使用率异常的教室
     */
    @GetMapping("/statistics/abnormal")
    public Result<List<ClassroomUtilizationDTO>> getAbnormalClassrooms(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Double highThreshold,
            @RequestParam(required = false) Double lowThreshold) {
        
        log.info("查询使用率异常教室: {} - {}", startDate, endDate);
        
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<ClassroomUtilizationDTO> result = statisticsService.findAbnormalUtilizationClassrooms(
            start, end, highThreshold, lowThreshold);
        
        return Result.success(result);
    }
    
    /**
     * 查询使用率最高的教室
     */
    @GetMapping("/statistics/top")
    public Result<List<ClassroomUtilizationDTO>> getTopUtilizedClassrooms(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "10") Integer limit) {
        
        log.info("查询使用率最高教室: {} - {}, limit={}", startDate, endDate, limit);
        
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<ClassroomUtilizationDTO> result = statisticsService.getTopUtilizedClassrooms(
            start, end, limit);
        
        return Result.success(result);
    }
    
    /**
     * 查询使用率最低的教室
     */
    @GetMapping("/statistics/bottom")
    public Result<List<ClassroomUtilizationDTO>> getBottomUtilizedClassrooms(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "10") Integer limit) {
        
        log.info("查询使用率最低教室: {} - {}, limit={}", startDate, endDate, limit);
        
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<ClassroomUtilizationDTO> result = statisticsService.getBottomUtilizedClassrooms(
            start, end, limit);
        
        return Result.success(result);
    }
    
    // ==================== 排课系统API ====================
    
    /**
     * 查询可用教室（用于排课）
     */
    @PostMapping("/scheduling/available")
    public Result<List<ClassroomDTO>> findAvailableClassrooms(
            @Valid @RequestBody AvailableClassroomRequest request) {
        
        log.info("排课系统查询可用教室: {} - {}", 
            request.getStartTime(), request.getEndTime());
        
        List<ClassroomDTO> result = schedulingApiService.findAvailableClassrooms(request);
        return Result.success(result);
    }
    
    /**
     * 检查教室可用性
     */
    @GetMapping("/{id}/availability")
    public Result<ClassroomAvailabilityDTO> checkClassroomAvailability(
            @PathVariable Long id,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        log.info("检查教室可用性: classroomId={}, {} - {}", id, startTime, endTime);
        
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        ClassroomAvailabilityDTO result = schedulingApiService
            .checkSingleClassroomAvailability(id, start, end);
        
        return Result.success(result);
    }
    
    /**
     * 推荐教室
     */
    @GetMapping("/scheduling/recommend")
    public Result<List<ClassroomDTO>> recommendClassrooms(
            @RequestParam Integer capacity,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(required = false) String preferredBuilding) {
        
        log.info("推荐教室: capacity={}, {} - {}", capacity, startTime, endTime);
        
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        List<ClassroomDTO> result = schedulingApiService.recommendClassrooms(
            capacity, start, end, preferredBuilding);
        
        return Result.success(result);
    }
    
    /**
     * 查询空闲时间段
     */
    @GetMapping("/{id}/free-slots")
    public Result<List<SchedulingApiService.TimeSlot>> getFreeTimeSlots(
            @PathVariable Long id,
            @RequestParam String date) {
        
        log.info("查询教室空闲时间段: classroomId={}, date={}", id, date);
        
        LocalDate queryDate = LocalDate.parse(date);
        List<SchedulingApiService.TimeSlot> result = schedulingApiService.getFreeTimeSlots(id, queryDate);
        
        return Result.success(result);
    }
    
    // ==================== 统计信息 ====================
    
    /**
     * 统计教室总数
     */
    @GetMapping("/count")
    public Result<Long> countClassrooms() {
        log.info("统计教室总数");
        
        long count = classroomService.countClassrooms();
        return Result.success(count);
    }
    
    /**
     * 统计可用教室数
     */
    @GetMapping("/count/available")
    public Result<Long> countAvailableClassrooms() {
        log.info("统计可用教室数");
        
        long count = classroomService.countAvailableClassrooms();
        return Result.success(count);
    }
}

