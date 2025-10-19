package com.university.academic.repository.attendance;

import com.university.academic.entity.attendance.AttendanceWarning;
import com.university.academic.entity.attendance.WarningStatus;
import com.university.academic.entity.attendance.WarningType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考勤预警数据访问接口
 *
 * @author Academic System Team
 */
@Repository
public interface AttendanceWarningRepository extends JpaRepository<AttendanceWarning, Long> {

    /**
     * 查询指定目标的所有预警
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 预警列表
     */
    List<AttendanceWarning> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
            String targetType, Long targetId);

    /**
     * 查询指定状态的预警
     *
     * @param status 预警状态
     * @return 预警列表
     */
    List<AttendanceWarning> findByStatusOrderByCreatedAtDesc(WarningStatus status);

    /**
     * 查询指定类型和状态的预警
     *
     * @param warningType 预警类型
     * @param status      预警状态
     * @return 预警列表
     */
    List<AttendanceWarning> findByWarningTypeAndStatusOrderByCreatedAtDesc(
            WarningType warningType, WarningStatus status);

    /**
     * 查询指定时间范围内的预警
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 预警列表
     */
    @Query("SELECT aw FROM AttendanceWarning aw " +
            "WHERE aw.createdAt BETWEEN :startTime AND :endTime " +
            "ORDER BY aw.createdAt DESC")
    List<AttendanceWarning> findByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定级别及以上的待处理预警
     *
     * @param minLevel 最低预警级别
     * @return 预警列表
     */
    @Query("SELECT aw FROM AttendanceWarning aw " +
            "WHERE aw.warningLevel >= :minLevel " +
            "AND aw.status = 'PENDING' " +
            "ORDER BY aw.warningLevel DESC, aw.createdAt DESC")
    List<AttendanceWarning> findPendingByMinLevel(@Param("minLevel") Integer minLevel);

    /**
     * 统计指定目标的待处理预警数量
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 预警数量
     */
    @Query("SELECT COUNT(aw) FROM AttendanceWarning aw " +
            "WHERE aw.targetType = :targetType " +
            "AND aw.targetId = :targetId " +
            "AND aw.status = 'PENDING'")
    long countPendingByTarget(
            @Param("targetType") String targetType,
            @Param("targetId") Long targetId);

    /**
     * 检查是否存在相同的待处理预警（避免重复）
     *
     * @param warningType 预警类型
     * @param targetType  目标类型
     * @param targetId    目标ID
     * @param offeringId  开课计划ID（可选）
     * @return true-存在，false-不存在
     */
    @Query("SELECT CASE WHEN COUNT(aw) > 0 THEN true ELSE false END " +
            "FROM AttendanceWarning aw " +
            "WHERE aw.warningType = :warningType " +
            "AND aw.targetType = :targetType " +
            "AND aw.targetId = :targetId " +
            "AND (:offeringId IS NULL OR aw.offering.id = :offeringId) " +
            "AND aw.status = 'PENDING'")
    boolean existsPendingWarning(
            @Param("warningType") WarningType warningType,
            @Param("targetType") String targetType,
            @Param("targetId") Long targetId,
            @Param("offeringId") Long offeringId);
}

