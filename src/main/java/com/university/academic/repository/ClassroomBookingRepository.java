package com.university.academic.repository;

import com.university.academic.entity.Classroom;
import com.university.academic.entity.ClassroomBooking;
import com.university.academic.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 教室借用Repository接口
 */
@Repository
public interface ClassroomBookingRepository extends JpaRepository<ClassroomBooking, Long> {
    
    /**
     * 根据ID查询借用记录（预加载教室信息）
     *
     * @param id 借用记录ID
     * @return 借用记录
     */
    @Query("SELECT cb FROM ClassroomBooking cb JOIN FETCH cb.classroom WHERE cb.id = :id")
    Optional<ClassroomBooking> findByIdWithClassroom(@Param("id") Long id);
    
    /**
     * 根据申请人查询借用列表
     *
     * @param applicantId 申请人ID
     * @param pageable 分页参数
     * @return 借用列表
     */
    @Query("SELECT cb FROM ClassroomBooking cb JOIN FETCH cb.classroom WHERE cb.applicantId = :applicantId")
    Page<ClassroomBooking> findByApplicantId(@Param("applicantId") Long applicantId, Pageable pageable);
    
    /**
     * 根据教室查询借用列表
     *
     * @param classroom 教室
     * @param pageable 分页参数
     * @return 借用列表
     */
    Page<ClassroomBooking> findByClassroom(Classroom classroom, Pageable pageable);
    
    /**
     * 根据状态查询借用列表
     *
     * @param status 借用状态
     * @param pageable 分页参数
     * @return 借用列表
     */
    @Query("SELECT cb FROM ClassroomBooking cb JOIN FETCH cb.classroom WHERE cb.status = :status")
    Page<ClassroomBooking> findByStatus(@Param("status") BookingStatus status, Pageable pageable);
    
    /**
     * 检查教室在指定时间段是否有冲突（已批准的借用）
     *
     * @param classroomId 教室ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 是否有冲突
     */
    @Query("SELECT CASE WHEN COUNT(cb) > 0 THEN true ELSE false END FROM ClassroomBooking cb " +
           "WHERE cb.classroom.id = :classroomId " +
           "AND cb.status = 'APPROVED' " +
           "AND ((cb.startTime < :endTime AND cb.endTime > :startTime))")
    boolean existsConflict(@Param("classroomId") Long classroomId,
                           @Param("startTime") LocalDateTime startTime,
                           @Param("endTime") LocalDateTime endTime);
    
    /**
     * 检查教室在指定时间段是否有冲突（排除指定的借用记录）
     *
     * @param classroomId 教室ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param excludeBookingId 要排除的借用ID
     * @return 是否有冲突
     */
    @Query("SELECT CASE WHEN COUNT(cb) > 0 THEN true ELSE false END FROM ClassroomBooking cb " +
           "WHERE cb.classroom.id = :classroomId " +
           "AND cb.id != :excludeBookingId " +
           "AND cb.status = 'APPROVED' " +
           "AND ((cb.startTime < :endTime AND cb.endTime > :startTime))")
    boolean existsConflictExcluding(@Param("classroomId") Long classroomId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime,
                                    @Param("excludeBookingId") Long excludeBookingId);
    
    /**
     * 查询教室在指定时间范围内的借用记录
     *
     * @param classroomId 教室ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 借用列表
     */
    @Query("SELECT cb FROM ClassroomBooking cb " +
           "JOIN FETCH cb.classroom " +
           "WHERE cb.classroom.id = :classroomId " +
           "AND cb.status = 'APPROVED' " +
           "AND ((cb.startTime < :endTime AND cb.endTime > :startTime)) " +
           "ORDER BY cb.startTime")
    List<ClassroomBooking> findByClassroomAndTimeRange(@Param("classroomId") Long classroomId,
                                                        @Param("startTime") LocalDateTime startTime,
                                                        @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据条件组合查询借用申请
     *
     * @param applicantId 申请人ID（可选）
     * @param classroomId 教室ID（可选）
     * @param status 借用状态（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param pageable 分页参数
     * @return 借用列表
     */
    @Query(value = "SELECT cb FROM ClassroomBooking cb " +
           "JOIN FETCH cb.classroom " +
           "WHERE (:applicantId IS NULL OR cb.applicantId = :applicantId) " +
           "AND (:classroomId IS NULL OR cb.classroom.id = :classroomId) " +
           "AND (:status IS NULL OR cb.status = :status) " +
           "AND (:startDate IS NULL OR cb.startTime >= :startDate) " +
           "AND (:endDate IS NULL OR cb.endTime <= :endDate)",
           countQuery = "SELECT COUNT(cb) FROM ClassroomBooking cb " +
                        "WHERE (:applicantId IS NULL OR cb.applicantId = :applicantId) " +
                        "AND (:classroomId IS NULL OR cb.classroom.id = :classroomId) " +
                        "AND (:status IS NULL OR cb.status = :status) " +
                        "AND (:startDate IS NULL OR cb.startTime >= :startDate) " +
                        "AND (:endDate IS NULL OR cb.endTime <= :endDate)")
    Page<ClassroomBooking> findByConditions(@Param("applicantId") Long applicantId,
                                            @Param("classroomId") Long classroomId,
                                            @Param("status") BookingStatus status,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate,
                                            Pageable pageable);
    
    /**
     * 统计指定时间范围内的借用数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 借用数量
     */
    @Query("SELECT COUNT(cb) FROM ClassroomBooking cb " +
           "WHERE cb.status = 'APPROVED' " +
           "AND ((cb.startTime < :endTime AND cb.endTime > :startTime))")
    long countByTimeRange(@Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计申请人的待审批申请数量
     *
     * @param applicantId 申请人ID
     * @return 待审批数量
     */
    long countByApplicantIdAndStatus(Long applicantId, BookingStatus status);
}

