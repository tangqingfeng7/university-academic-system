package com.university.academic.repository;

import com.university.academic.entity.EvaluationPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 评价周期数据访问接口
 *
 * @author Academic System Team
 */
@Repository
public interface EvaluationPeriodRepository extends JpaRepository<EvaluationPeriod, Long> {

    /**
     * 查询当前活跃的评价周期
     *
     * @return 评价周期列表
     */
    @Query("SELECT ep FROM EvaluationPeriod ep " +
           "JOIN FETCH ep.semester " +
           "WHERE ep.active = true")
    List<EvaluationPeriod> findAllActive();

    /**
     * 查询当前时间有效的评价周期
     *
     * @param now 当前时间
     * @return 评价周期对象
     */
    @Query("SELECT ep FROM EvaluationPeriod ep " +
           "JOIN FETCH ep.semester " +
           "WHERE ep.active = true " +
           "AND ep.startTime <= :now " +
           "AND ep.endTime >= :now")
    Optional<EvaluationPeriod> findActiveByDate(@Param("now") LocalDateTime now);

    /**
     * 查询某学期的评价周期
     *
     * @param semesterId 学期ID
     * @return 评价周期对象
     */
    @Query("SELECT ep FROM EvaluationPeriod ep " +
           "JOIN FETCH ep.semester " +
           "WHERE ep.semester.id = :semesterId")
    Optional<EvaluationPeriod> findBySemesterId(@Param("semesterId") Long semesterId);

    /**
     * 查询某学期的所有评价周期
     *
     * @param semesterId 学期ID
     * @return 评价周期列表
     */
    @Query("SELECT ep FROM EvaluationPeriod ep " +
           "JOIN FETCH ep.semester " +
           "WHERE ep.semester.id = :semesterId " +
           "ORDER BY ep.startTime DESC")
    List<EvaluationPeriod> findAllBySemesterId(@Param("semesterId") Long semesterId);

    /**
     * 检查某学期是否已有活跃的评价周期
     *
     * @param semesterId 学期ID
     * @return true-已有活跃周期，false-无活跃周期
     */
    @Query("SELECT CASE WHEN COUNT(ep) > 0 THEN true ELSE false END " +
           "FROM EvaluationPeriod ep " +
           "WHERE ep.semester.id = :semesterId AND ep.active = true")
    boolean existsActivePeriodBySemesterId(@Param("semesterId") Long semesterId);

    /**
     * 查询所有评价周期（按开始时间倒序）
     *
     * @return 评价周期列表
     */
    @Query("SELECT ep FROM EvaluationPeriod ep " +
           "JOIN FETCH ep.semester " +
           "ORDER BY ep.startTime DESC")
    List<EvaluationPeriod> findAllOrderByStartTimeDesc();
}

