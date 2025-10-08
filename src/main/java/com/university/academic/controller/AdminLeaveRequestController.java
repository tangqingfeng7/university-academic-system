package com.university.academic.controller;

import com.university.academic.dto.LeaveRequestApprovalRequest;
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

import java.util.HashMap;
import java.util.Map;

/**
 * 管理端请假审批控制器
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/leave-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminLeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    /**
     * 查询所有请假申请列表
     */
    @GetMapping
    public Result<Page<LeaveRequestDTO>> getAllLeaveRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String applicantType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LeaveRequestDTO> result = leaveRequestService.getAllLeaveRequests(
                status, applicantType, keyword, pageable);
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
     * 审批请假申请
     */
    @PostMapping("/{id}/approve")
    public Result<LeaveRequestDTO> approveLeaveRequest(
            @PathVariable Long id,
            @Valid @RequestBody LeaveRequestApprovalRequest request
    ) {
        log.info("管理员审批请假申请: {}, 结果: {}", id, request.getStatus());
        LeaveRequestDTO result = leaveRequestService.approveLeaveRequest(id, request);
        return Result.success(result);
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("pendingCount", leaveRequestService.countPendingLeaveRequests());
        return Result.success(statistics);
    }
}

