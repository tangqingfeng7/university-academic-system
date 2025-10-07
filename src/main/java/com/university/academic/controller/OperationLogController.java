package com.university.academic.controller;

import com.university.academic.dto.OperationLogDTO;
import com.university.academic.service.OperationLogService;
import com.university.academic.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 操作日志控制器
 * 提供操作日志查询接口（管理员端）
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OperationLogController {

    private final OperationLogService logService;

    /**
     * 查询所有操作日志（分页）
     */
    @GetMapping
    public Result<Page<OperationLogDTO>> getAllLogs(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        log.info("查询所有操作日志");

        Page<OperationLogDTO> page = logService.findAll(pageable);
        return Result.success(page);
    }

    /**
     * 根据条件查询操作日志
     */
    @GetMapping("/search")
    public Result<Page<OperationLogDTO>> searchLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        log.info("条件查询操作日志: userId={}, username={}, status={}", userId, username, status);

        Page<OperationLogDTO> page = logService.findByConditions(
                userId, username, status, startTime, endTime, pageable);
        return Result.success(page);
    }

    /**
     * 根据用户ID查询操作日志
     */
    @GetMapping("/user/{userId}")
    public Result<Page<OperationLogDTO>> getLogsByUserId(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        log.info("查询用户操作日志: userId={}", userId);

        Page<OperationLogDTO> page = logService.findByUserId(userId, pageable);
        return Result.success(page);
    }

    /**
     * 获取日志统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        log.info("查询日志统计: startTime={}, endTime={}", startTime, endTime);

        // 如果没有指定时间范围，默认查询最近7天
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        Map<String, Object> statistics = logService.getStatistics(startTime, endTime);
        return Result.success(statistics);
    }
}

