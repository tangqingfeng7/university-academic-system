package com.university.academic.repository;

import com.university.academic.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 操作日志数据访问接口
 * 提供操作日志数据的CRUD操作
 *
 * @author Academic System Team
 */
@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    /**
     * 根据用户ID查询操作日志
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 日志分页数据
     */
    Page<OperationLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 根据用户名查询操作日志
     *
     * @param username 用户名
     * @param pageable 分页参数
     * @return 日志分页数据
     */
    Page<OperationLog> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);

    /**
     * 根据状态查询操作日志
     *
     * @param status   状态
     * @param pageable 分页参数
     * @return 日志分页数据
     */
    Page<OperationLog> findByStatusOrderByCreatedAtDesc(
            OperationLog.LogStatus status, Pageable pageable);

    /**
     * 根据时间范围查询操作日志
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param pageable  分页参数
     * @return 日志分页数据
     */
    Page<OperationLog> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 组合条件查询操作日志
     *
     * @param userId    用户ID（可选）
     * @param username  用户名（可选）
     * @param status    状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @param pageable  分页参数
     * @return 日志分页数据
     */
    @Query("SELECT ol FROM OperationLog ol WHERE " +
            "(:userId IS NULL OR ol.userId = :userId) AND " +
            "(:username IS NULL OR ol.username LIKE %:username%) AND " +
            "(:status IS NULL OR ol.status = :status) AND " +
            "(:startTime IS NULL OR ol.createdAt >= :startTime) AND " +
            "(:endTime IS NULL OR ol.createdAt <= :endTime) " +
            "ORDER BY ol.createdAt DESC")
    Page<OperationLog> findByConditions(
            @Param("userId") Long userId,
            @Param("username") String username,
            @Param("status") OperationLog.LogStatus status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    /**
     * 统计指定时间范围内的日志数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 日志数量
     */
    long countByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计指定用户的操作数量
     *
     * @param userId 用户ID
     * @return 操作数量
     */
    long countByUserId(Long userId);
}

