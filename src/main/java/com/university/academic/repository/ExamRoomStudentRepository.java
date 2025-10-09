package com.university.academic.repository;

import com.university.academic.entity.ExamRoomStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 考场学生分配数据访问接口
 * 提供考场学生分配数据的CRUD操作
 *
 * @author Academic System Team
 */
@Repository
public interface ExamRoomStudentRepository extends JpaRepository<ExamRoomStudent, Long> {

    /**
     * 根据考场ID查询学生分配列表
     *
     * @param examRoomId 考场ID
     * @return 学生分配列表
     */
    List<ExamRoomStudent> findByExamRoomId(Long examRoomId);

    /**
     * 根据考场ID查询学生分配列表（带学生信息）
     *
     * @param examRoomId 考场ID
     * @return 学生分配列表
     */
    @Query("SELECT DISTINCT ers FROM ExamRoomStudent ers " +
           "LEFT JOIN FETCH ers.student s " +
           "LEFT JOIN FETCH s.major m " +
           "LEFT JOIN FETCH m.department " +
           "WHERE ers.examRoom.id = :examRoomId " +
           "ORDER BY ers.seatNumber")
    List<ExamRoomStudent> findByExamRoomIdWithStudent(@Param("examRoomId") Long examRoomId);

    /**
     * 根据学生ID和考试ID查询分配记录
     *
     * @param studentId 学生ID
     * @param examId    考试ID
     * @return 学生分配记录
     */
    @Query("SELECT ers FROM ExamRoomStudent ers " +
           "LEFT JOIN FETCH ers.examRoom er " +
           "LEFT JOIN FETCH er.exam e " +
           "LEFT JOIN FETCH e.courseOffering co " +
           "LEFT JOIN FETCH co.course " +
           "WHERE ers.student.id = :studentId " +
           "AND e.id = :examId")
    Optional<ExamRoomStudent> findByStudentIdAndExamId(
            @Param("studentId") Long studentId,
            @Param("examId") Long examId);

    /**
     * 根据学生ID查询考试分配列表（带考场和考试信息）
     *
     * @param studentId 学生ID
     * @return 学生分配列表
     */
    @Query("SELECT DISTINCT ers FROM ExamRoomStudent ers " +
           "LEFT JOIN FETCH ers.examRoom er " +
           "LEFT JOIN FETCH er.exam e " +
           "LEFT JOIN FETCH e.courseOffering co " +
           "LEFT JOIN FETCH co.course " +
           "LEFT JOIN FETCH co.semester " +
           "WHERE ers.student.id = :studentId")
    List<ExamRoomStudent> findByStudentIdWithDetails(@Param("studentId") Long studentId);

    /**
     * 检查学生在指定时间段是否有考试
     *
     * @param studentId 学生ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return true-有考试，false-无考试
     */
    @Query("SELECT CASE WHEN COUNT(ers) > 0 THEN true ELSE false END " +
           "FROM ExamRoomStudent ers " +
           "WHERE ers.student.id = :studentId " +
           "AND ers.examRoom.exam.examTime BETWEEN :startTime AND :endTime " +
           "AND ers.examRoom.exam.status != 'CANCELLED'")
    boolean hasExamInTimeRange(
            @Param("studentId") Long studentId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 检查学生在指定时间段是否有考试（排除指定考试）
     *
     * @param studentId     学生ID
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param excludeExamId 排除的考试ID
     * @return true-有考试，false-无考试
     */
    @Query("SELECT CASE WHEN COUNT(ers) > 0 THEN true ELSE false END " +
           "FROM ExamRoomStudent ers " +
           "WHERE ers.student.id = :studentId " +
           "AND ers.examRoom.exam.examTime BETWEEN :startTime AND :endTime " +
           "AND ers.examRoom.exam.status != 'CANCELLED' " +
           "AND (:excludeExamId IS NULL OR ers.examRoom.exam.id != :excludeExamId)")
    boolean hasExamInTimeRangeExcluding(
            @Param("studentId") Long studentId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeExamId") Long excludeExamId);

    /**
     * 检查学生是否已分配到该考试
     *
     * @param studentId 学生ID
     * @param examId    考试ID
     * @return true-已分配，false-未分配
     */
    @Query("SELECT CASE WHEN COUNT(ers) > 0 THEN true ELSE false END " +
           "FROM ExamRoomStudent ers " +
           "WHERE ers.student.id = :studentId " +
           "AND ers.examRoom.exam.id = :examId")
    boolean existsByStudentIdAndExamId(
            @Param("studentId") Long studentId,
            @Param("examId") Long examId);

    /**
     * 根据考试ID查询所有学生分配
     *
     * @param examId 考试ID
     * @return 学生分配列表
     */
    @Query("SELECT ers FROM ExamRoomStudent ers " +
           "WHERE ers.examRoom.exam.id = :examId")
    List<ExamRoomStudent> findByExamId(@Param("examId") Long examId);

    /**
     * 统计考场的学生数量
     *
     * @param examRoomId 考场ID
     * @return 学生数量
     */
    long countByExamRoomId(Long examRoomId);

    /**
     * 统计考试的学生总数
     *
     * @param examId 考试ID
     * @return 学生总数
     */
    @Query("SELECT COUNT(ers) FROM ExamRoomStudent ers " +
           "WHERE ers.examRoom.exam.id = :examId")
    long countByExamId(@Param("examId") Long examId);

    /**
     * 根据考场ID删除所有学生分配记录
     *
     * @param examRoomId 考场ID
     */
    @Modifying
    @Query("DELETE FROM ExamRoomStudent ers WHERE ers.examRoom.id = :examRoomId")
    void deleteByExamRoomId(@Param("examRoomId") Long examRoomId);

    /**
     * 根据考场ID和学生ID删除分配记录
     *
     * @param examRoomId 考场ID
     * @param studentId  学生ID
     */
    @Modifying
    @Query("DELETE FROM ExamRoomStudent ers WHERE ers.examRoom.id = :examRoomId AND ers.student.id = :studentId")
    void deleteByExamRoomIdAndStudentId(@Param("examRoomId") Long examRoomId, @Param("studentId") Long studentId);
}

