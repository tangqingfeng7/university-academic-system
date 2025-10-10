package com.university.academic.repository;

import com.university.academic.entity.Classroom;
import com.university.academic.entity.ClassroomUsageLog;
import com.university.academic.entity.UsageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教室使用记录Repository接口
 */
@Repository
public interface ClassroomUsageLogRepository extends JpaRepository<ClassroomUsageLog, Long> {
    
    /**
     * 根据教室查询使用记录
     *
     * @param classroom 教室
     * @param pageable 分页参数
     * @return 使用记录列表
     */
    Page<ClassroomUsageLog> findByClassroom(Classroom classroom, Pageable pageable);
    
    /**
     * 根据使用类型查询记录
     *
     * @param type 使用类型
     * @param pageable 分页参数
     * @return 使用记录列表
     */
    Page<ClassroomUsageLog> findByType(UsageType type, Pageable pageable);
    
    /**
     * 查询教室在指定时间范围内的使用记录
     *
     * @param classroomId 教室ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 使用记录列表
     */
    @Query("SELECT cul FROM ClassroomUsageLog cul " +
           "WHERE cul.classroom.id = :classroomId " +
           "AND ((cul.startTime < :endTime AND cul.endTime > :startTime)) " +
           "ORDER BY cul.startTime")
    List<ClassroomUsageLog> findByClassroomAndTimeRange(@Param("classroomId") Long classroomId,
                                                         @Param("startTime") LocalDateTime startTime,
                                                         @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据关联ID和类型查询使用记录
     *
     * @param referenceId 关联ID
     * @param type 使用类型
     * @return 使用记录列表
     */
    List<ClassroomUsageLog> findByReferenceIdAndType(Long referenceId, UsageType type);
    
    /**
     * 检查教室在指定时间段是否有使用记录（冲突检查）
     *
     * @param classroomId 教室ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 是否有冲突
     */
    @Query("SELECT CASE WHEN COUNT(cul) > 0 THEN true ELSE false END FROM ClassroomUsageLog cul " +
           "WHERE cul.classroom.id = :classroomId " +
           "AND ((cul.startTime < :endTime AND cul.endTime > :startTime))")
    boolean existsConflict(@Param("classroomId") Long classroomId,
                           @Param("startTime") LocalDateTime startTime,
                           @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计教室在指定时间范围内的使用时长（小时）
     *
     * @param classroomId 教室ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 使用时长（分钟）
     */
    @Query("SELECT SUM(TIMESTAMPDIFF(MINUTE, cul.startTime, cul.endTime)) FROM ClassroomUsageLog cul " +
           "WHERE cul.classroom.id = :classroomId " +
           "AND cul.startTime >= :startTime " +
           "AND cul.endTime <= :endTime")
    Long calculateUsageMinutes(@Param("classroomId") Long classroomId,
                               @Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据条件组合查询使用记录
     *
     * @param classroomId 教室ID（可选）
     * @param type 使用类型（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param pageable 分页参数
     * @return 使用记录列表
     */
    @Query("SELECT cul FROM ClassroomUsageLog cul " +
           "WHERE (:classroomId IS NULL OR cul.classroom.id = :classroomId) " +
           "AND (:type IS NULL OR cul.type = :type) " +
           "AND (:startDate IS NULL OR cul.startTime >= :startDate) " +
           "AND (:endDate IS NULL OR cul.endTime <= :endDate)")
    Page<ClassroomUsageLog> findByConditions(@Param("classroomId") Long classroomId,
                                             @Param("type") UsageType type,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate,
                                             Pageable pageable);
    
    /**
     * 删除指定关联ID和类型的使用记录
     *
     * @param referenceId 关联ID
     * @param type 使用类型
     */
    void deleteByReferenceIdAndType(Long referenceId, UsageType type);
    
    /**
     * 统计指定时间范围内各教室的使用次数
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果列表 [教室ID, 使用次数]
     */
    @Query("SELECT cul.classroom.id, COUNT(cul) FROM ClassroomUsageLog cul " +
           "WHERE cul.startTime >= :startTime AND cul.endTime <= :endTime " +
           "GROUP BY cul.classroom.id " +
           "ORDER BY COUNT(cul) DESC")
    List<Object[]> countUsageByClassroom(@Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);
}

