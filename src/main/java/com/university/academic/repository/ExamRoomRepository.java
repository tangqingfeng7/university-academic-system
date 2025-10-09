package com.university.academic.repository;

import com.university.academic.entity.ExamRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 考场数据访问接口
 * 提供考场数据的CRUD操作
 *
 * @author Academic System Team
 */
@Repository
public interface ExamRoomRepository extends JpaRepository<ExamRoom, Long> {

    /**
     * 根据ID查询考场（带关联实体）
     *
     * @param id 考场ID
     * @return 考场对象
     */
    @Query("SELECT DISTINCT er FROM ExamRoom er " +
           "LEFT JOIN FETCH er.exam e " +
           "LEFT JOIN FETCH e.courseOffering co " +
           "LEFT JOIN FETCH co.semester " +
           "LEFT JOIN FETCH co.course " +
           "WHERE er.id = :id")
    Optional<ExamRoom> findByIdWithDetails(@Param("id") Long id);

    /**
     * 根据考试ID查询考场列表
     *
     * @param examId 考试ID
     * @return 考场列表
     */
    List<ExamRoom> findByExamId(Long examId);

    /**
     * 根据考试ID查询考场列表（带学生和监考信息）
     *
     * @param examId 考试ID
     * @return 考场列表
     */
    @Query("SELECT DISTINCT er FROM ExamRoom er " +
           "LEFT JOIN FETCH er.students " +
           "LEFT JOIN FETCH er.invigilators " +
           "WHERE er.exam.id = :examId")
    List<ExamRoom> findByExamIdWithDetails(@Param("examId") Long examId);

    /**
     * 查询指定时间段和地点的考场（用于冲突检测）
     *
     * @param location  考场地点
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 考场列表
     */
    @Query("SELECT er FROM ExamRoom er " +
           "WHERE er.location = :location " +
           "AND er.exam.examTime BETWEEN :startTime AND :endTime " +
           "AND er.exam.status != 'CANCELLED'")
    List<ExamRoom> findByLocationAndTimeRange(
            @Param("location") String location,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定时间段和地点的考场（用于冲突检测，排除指定考试）
     *
     * @param location  考场地点
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param excludeExamId 排除的考试ID
     * @return 考场列表
     */
    @Query("SELECT er FROM ExamRoom er " +
           "WHERE er.location = :location " +
           "AND er.exam.examTime BETWEEN :startTime AND :endTime " +
           "AND er.exam.status != 'CANCELLED' " +
           "AND (:excludeExamId IS NULL OR er.exam.id != :excludeExamId)")
    List<ExamRoom> findByLocationAndTimeRangeExcluding(
            @Param("location") String location,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeExamId") Long excludeExamId);

    /**
     * 统计考试的考场数量
     *
     * @param examId 考试ID
     * @return 考场数量
     */
    long countByExamId(Long examId);

    /**
     * 统计考试的总容量
     *
     * @param examId 考试ID
     * @return 总容量
     */
    @Query("SELECT SUM(er.capacity) FROM ExamRoom er " +
           "WHERE er.exam.id = :examId")
    Integer sumCapacityByExamId(@Param("examId") Long examId);

    /**
     * 统计考试的已分配人数
     *
     * @param examId 考试ID
     * @return 已分配总人数
     */
    @Query("SELECT SUM(er.assignedCount) FROM ExamRoom er " +
           "WHERE er.exam.id = :examId")
    Integer sumAssignedCountByExamId(@Param("examId") Long examId);

    /**
     * 查询有剩余容量的考场
     *
     * @param examId 考试ID
     * @return 考场列表
     */
    @Query("SELECT er FROM ExamRoom er " +
           "WHERE er.exam.id = :examId " +
           "AND er.assignedCount < er.capacity")
    List<ExamRoom> findAvailableRoomsByExamId(@Param("examId") Long examId);
}

