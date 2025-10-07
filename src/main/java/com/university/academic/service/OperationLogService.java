package com.university.academic.service;

import com.university.academic.dto.OperationLogDTO;
import com.university.academic.entity.OperationLog;
import com.university.academic.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志服务类
 * 提供操作日志查询功能
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogRepository logRepository;

    /**
     * 分页查询所有操作日志
     *
     * @param pageable 分页参数
     * @return 日志分页数据
     */
    @Transactional(readOnly = true)
    public Page<OperationLogDTO> findAll(Pageable pageable) {
        Page<OperationLog> page = logRepository.findAll(pageable);
        return page.map(this::toDTO);
    }

    /**
     * 根据条件查询操作日志
     *
     * @param userId    用户ID（可选）
     * @param username  用户名（可选）
     * @param status    状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @param pageable  分页参数
     * @return 日志分页数据
     */
    @Transactional(readOnly = true)
    public Page<OperationLogDTO> findByConditions(Long userId, String username, String status,
                                                   LocalDateTime startTime, LocalDateTime endTime,
                                                   Pageable pageable) {
        log.info("查询操作日志: userId={}, username={}, status={}, startTime={}, endTime={}",
                userId, username, status, startTime, endTime);

        OperationLog.LogStatus logStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                logStatus = OperationLog.LogStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                log.warn("无效的日志状态: {}", status);
            }
        }

        Page<OperationLog> page = logRepository.findByConditions(
                userId, username, logStatus, startTime, endTime, pageable);

        return page.map(this::toDTO);
    }

    /**
     * 根据用户ID查询操作日志
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 日志分页数据
     */
    @Transactional(readOnly = true)
    public Page<OperationLogDTO> findByUserId(Long userId, Pageable pageable) {
        Page<OperationLog> page = logRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return page.map(this::toDTO);
    }

    /**
     * 获取日志统计信息
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计信息
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("查询日志统计: startTime={}, endTime={}", startTime, endTime);

        Map<String, Object> statistics = new HashMap<>();

        // 总数量
        long totalCount = logRepository.countByCreatedAtBetween(startTime, endTime);
        statistics.put("totalCount", totalCount);

        // 成功数量
        Page<OperationLog> successPage = logRepository.findByStatusOrderByCreatedAtDesc(
                OperationLog.LogStatus.SUCCESS, Pageable.ofSize(1));
        long successCount = successPage.getTotalElements();
        statistics.put("successCount", successCount);

        // 失败数量
        Page<OperationLog> failurePage = logRepository.findByStatusOrderByCreatedAtDesc(
                OperationLog.LogStatus.FAILURE, Pageable.ofSize(1));
        long failureCount = failurePage.getTotalElements();
        statistics.put("failureCount", failureCount);

        return statistics;
    }

    /**
     * 转换为DTO
     */
    private OperationLogDTO toDTO(OperationLog log) {
        return OperationLogDTO.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .username(log.getUsername())
                .operation(log.getOperation())
                .method(log.getMethod())
                .params(log.getParams())
                .ip(log.getIp())
                .executionTime(log.getExecutionTime())
                .status(log.getStatus() != null ? log.getStatus().name() : null)
                .statusDescription(log.getStatus() != null ? 
                        log.getStatus().getDescription() : null)
                .errorMsg(log.getErrorMsg())
                .createdAt(log.getCreatedAt())
                .build();
    }
}

