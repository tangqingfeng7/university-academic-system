package com.university.ems.repository;

import com.university.academic.entity.Semester;
import com.university.ems.entity.SchedulingSolution;
import com.university.ems.enums.SolutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 排课方案Repository接口
 * 
 * @author Academic System Team
 */
@Repository
public interface SchedulingSolutionRepository extends JpaRepository<SchedulingSolution, Long> {

    /**
     * 查询学期的所有方案
     *
     * @param semester 学期
     * @return 方案列表
     */
    List<SchedulingSolution> findBySemester(Semester semester);

    /**
     * 查询学期的指定状态方案
     *
     * @param semester 学期
     * @param status 方案状态
     * @return 方案列表
     */
    List<SchedulingSolution> findBySemesterAndStatus(Semester semester, SolutionStatus status);

    /**
     * 查询学期已应用的方案
     *
     * @param semesterId 学期ID
     * @return 已应用的方案
     */
    @Query("SELECT s FROM SchedulingSolution s WHERE s.semester.id = :semesterId AND s.status = 'APPLIED'")
    Optional<SchedulingSolution> findAppliedSolutionBySemester(@Param("semesterId") Long semesterId);

    /**
     * 查询学期的最佳方案（质量分数最高）
     *
     * @param semester 学期
     * @return 最佳方案
     */
    @Query("SELECT s FROM SchedulingSolution s WHERE s.semester = :semester " +
           "AND s.status = 'COMPLETED' ORDER BY s.qualityScore DESC")
    List<SchedulingSolution> findBestSolutionsBySemester(@Param("semester") Semester semester);

    /**
     * 按学期ID和状态查询方案
     *
     * @param semesterId 学期ID
     * @param status 方案状态
     * @return 方案列表
     */
    @Query("SELECT s FROM SchedulingSolution s WHERE s.semester.id = :semesterId AND s.status = :status")
    List<SchedulingSolution> findBySemesterIdAndStatus(@Param("semesterId") Long semesterId, 
                                                        @Param("status") SolutionStatus status);

    /**
     * 检查是否存在活跃的排课方案（正在优化或已应用的方案）
     * 这些方案可能正在使用排课约束，不应删除约束
     *
     * @return true-存在活跃方案，false-不存在
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM SchedulingSolution s " +
           "WHERE s.status IN ('OPTIMIZING', 'APPLIED')")
    boolean existsActiveSolution();
}

