package com.university.academic.controller;

import com.university.academic.controller.dto.*;
import com.university.academic.security.CustomUserDetailsService;
import com.university.academic.service.ClassroomBookingService;
import com.university.academic.service.ClassroomService;
import com.university.academic.service.ClassroomUsageService;
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
 * 教师端教室管理控制器
 * 提供教室查询、借用申请等功能
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher/classrooms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherClassroomController {
    
    private final ClassroomService classroomService;
    private final ClassroomBookingService bookingService;
    private final ClassroomUsageService usageService;
    private final CustomUserDetailsService userDetailsService;
    
    /**
     * 查询教室列表
     */
    @GetMapping
    public Result<Page<ClassroomDTO>> getClassrooms(
            @RequestParam(required = false) String building,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("教师查询教室列表: building={}, type={}, status={}, minCapacity={}", 
            building, type, status, minCapacity);
        
        ClassroomQueryDTO query = new ClassroomQueryDTO();
        query.setBuilding(building);
        if (type != null && !type.trim().isEmpty()) {
            query.setType(com.university.academic.entity.ClassroomType.valueOf(type));
        }
        if (status != null && !status.trim().isEmpty()) {
            query.setStatus(com.university.academic.entity.ClassroomStatus.valueOf(status));
        }
        query.setMinCapacity(minCapacity);
        
        PageRequest pageable = PageRequest.of(page, size, Sort.by("building", "roomNo"));
        Page<ClassroomDTO> result = classroomService.getClassrooms(query, pageable);
        
        return Result.success(result);
    }
    
    /**
     * 查询可用教室
     */
    @GetMapping("/available")
    public Result<List<ClassroomDTO>> getAvailableClassrooms(
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) String type) {
        
        log.info("查询可用教室: {} - {}, capacity={}, type={}", 
            startTime, endTime, capacity, type);
        
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        
        List<ClassroomDTO> result = classroomService.getAvailableClassrooms(
            start, end, capacity, 
            (type != null && !type.trim().isEmpty()) ? com.university.academic.entity.ClassroomType.valueOf(type) : null);
        
        return Result.success(result);
    }
    
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
    
    /**
     * 提交借用申请
     */
    @PostMapping("/bookings")
    public Result<ClassroomBookingDTO> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            Authentication authentication) {
        
        Long userId = userDetailsService.getUserIdFromAuth(authentication);
        log.info("提交教室借用申请: userId={}, classroomId={}", 
            userId, request.getClassroomId());
        
        ClassroomBookingDTO result = bookingService.createBooking(userId, request);
        
        return Result.success(result);
    }
    
    /**
     * 查询我的借用申请
     */
    @GetMapping("/bookings")
    public Result<Page<ClassroomBookingDTO>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        Long userId = userDetailsService.getUserIdFromAuth(authentication);
        log.info("查询我的借用申请: userId={}", userId);
        
        PageRequest pageable = PageRequest.of(page, size, 
            Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ClassroomBookingDTO> result = bookingService.getMyBookings(userId, pageable);
        
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
     * 取消借用申请
     */
    @DeleteMapping("/bookings/{id}")
    public Result<Void> cancelBooking(
            @PathVariable Long id,
            Authentication authentication) {
        
        Long userId = userDetailsService.getUserIdFromAuth(authentication);
        log.info("取消借用申请: bookingId={}, userId={}", id, userId);
        
        bookingService.cancelBooking(id, userId);
        
        return Result.success();
    }
    
    /**
     * 检查教室时间冲突
     */
    @GetMapping("/bookings/check-conflict")
    public Result<Boolean> checkConflict(
            @RequestParam Long classroomId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        log.info("检查教室时间冲突: classroomId={}, {} - {}", classroomId, startTime, endTime);
        
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        
        boolean hasConflict = bookingService.checkConflict(classroomId, start, end);
        
        return Result.success(hasConflict);
    }
}

