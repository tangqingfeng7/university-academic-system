package com.university.academic.controller;

import com.university.academic.dto.LeaveRequestCreateRequest;
import com.university.academic.dto.LeaveRequestDTO;
import com.university.academic.service.LeaveRequestService;
import com.university.academic.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 学生端请假申请控制器
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/student/leave-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentLeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    /**
     * 提交请假申请
     */
    @PostMapping
    public Result<LeaveRequestDTO> createLeaveRequest(@Valid @RequestBody LeaveRequestCreateRequest request) {
        log.info("学生提交请假申请");
        LeaveRequestDTO result = leaveRequestService.createStudentLeaveRequest(request);
        return Result.success(result);
    }

    /**
     * 查询我的请假申请列表
     */
    @GetMapping
    public Result<Page<LeaveRequestDTO>> getMyLeaveRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LeaveRequestDTO> result = leaveRequestService.getMyLeaveRequests(pageable);
        return Result.success(result);
    }

    /**
     * 查询请假申请详情
     */
    @GetMapping("/{id}")
    public Result<LeaveRequestDTO> getLeaveRequestById(@PathVariable Long id) {
        LeaveRequestDTO result = leaveRequestService.getLeaveRequestById(id);
        return Result.success(result);
    }

    /**
     * 取消请假申请
     */
    @DeleteMapping("/{id}")
    public Result<Void> cancelLeaveRequest(@PathVariable Long id) {
        log.info("学生取消请假申请: {}", id);
        leaveRequestService.cancelLeaveRequest(id);
        return Result.success("取消成功");
    }
}

