package com.university.academic.controller;

import com.university.academic.dto.ApproveCourseChangeRequest;
import com.university.academic.dto.CourseChangeRequestDTO;
import com.university.academic.dto.CreateCourseChangeRequest;
import com.university.academic.service.CourseChangeRequestService;
import com.university.academic.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 调课申请控制器
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CourseChangeRequestController {

    private final CourseChangeRequestService changeRequestService;

    /**
     * 教师创建调课申请
     */
    @PostMapping("/teacher/course-change-requests")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<CourseChangeRequestDTO> createChangeRequest(
            @Valid @RequestBody CreateCourseChangeRequest request) {
        log.info("教师创建调课申请: {}", request);
        CourseChangeRequestDTO result = changeRequestService.createChangeRequest(request);
        return Result.success(result);
    }

    /**
     * 教师查询自己的调课申请列表
     */
    @GetMapping("/teacher/course-change-requests")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<Page<CourseChangeRequestDTO>> getTeacherChangeRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CourseChangeRequestDTO> result = changeRequestService.getTeacherChangeRequests(page, size);
        return Result.success(result);
    }

    /**
     * 管理员查询所有调课申请列表
     */
    @GetMapping("/admin/course-change-requests")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<CourseChangeRequestDTO>> getAllChangeRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        log.info("管理员查询调课申请: page={}, size={}, status={}", page, size, status);
        Page<CourseChangeRequestDTO> result = changeRequestService.getAllChangeRequests(page, size, status);
        return Result.success(result);
    }

    /**
     * 获取调课申请详情
     */
    @GetMapping("/course-change-requests/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<CourseChangeRequestDTO> getChangeRequestDetail(@PathVariable Long id) {
        CourseChangeRequestDTO result = changeRequestService.getChangeRequestDetail(id);
        return Result.success(result);
    }

    /**
     * 管理员审批调课申请
     */
    @PutMapping("/admin/course-change-requests/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<CourseChangeRequestDTO> approveChangeRequest(
            @PathVariable Long id,
            @Valid @RequestBody ApproveCourseChangeRequest request) {
        log.info("管理员审批调课申请: id={}, approved={}", id, request.getApproved());
        CourseChangeRequestDTO result = changeRequestService.approveChangeRequest(id, request);
        return Result.success(result);
    }

    /**
     * 统计待审批数量
     */
    @GetMapping("/admin/course-change-requests/pending/count")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> countPendingRequests() {
        long count = changeRequestService.countPendingRequests();
        return Result.success(count);
    }
}

