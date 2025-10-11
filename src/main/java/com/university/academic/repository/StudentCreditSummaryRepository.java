package com.university.academic.repository;

import com.university.academic.entity.StudentCreditSummary;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 学生学分汇总数据访问接口
 * 提供学生学分汇总数据的CRUD操作
 *
 * @author Academic System Team
 */
@Repository
public interface StudentCreditSummaryRepository extends JpaRepository<StudentCreditSummary, Long> {

    /**
     * 根据学生ID查询学分汇总
     *
     * @param studentId 学生ID
     * @return 学分汇总
     */
    Optional<StudentCreditSummary> findByStudentId(Long studentId);

    /**
     * 根据学生ID查询学分汇总 - 预加载学生和专业信息
     *
     * @param studentId 学生ID
     * @return 学分汇总
     */
    @EntityGraph(attributePaths = {"student", "student.major", "student.major.department"})
    @Query("SELECT scs FROM StudentCreditSummary scs WHERE scs.student.id = :studentId")
    Optional<StudentCreditSummary> findByStudentIdWithDetails(@Param("studentId") Long studentId);

    /**
     * 检查学生是否存在学分汇总记录
     *
     * @param studentId 学生ID
     * @return true-存在，false-不存在
     */
    boolean existsByStudentId(Long studentId);

    /**
     * 查询总学分大于等于指定值的学生
     *
     * @param credits 学分值
     * @return 学分汇总列表
     */
    List<StudentCreditSummary> findByTotalCreditsGreaterThanEqual(Double credits);

    /**
     * 查询总学分大于等于指定值的学生 - 预加载学生信息
     *
     * @param credits 学分值
     * @return 学分汇总列表
     */
    @EntityGraph(attributePaths = {"student", "student.major", "student.major.department"})
    @Query("SELECT scs FROM StudentCreditSummary scs " +
           "WHERE scs.totalCredits >= :credits " +
           "ORDER BY scs.totalCredits DESC")
    List<StudentCreditSummary> findByTotalCreditsGreaterThanEqualWithDetails(@Param("credits") Double credits);

    /**
     * 根据专业ID查询该专业所有学生的学分汇总
     *
     * @param majorId 专业ID
     * @return 学分汇总列表
     */
    @EntityGraph(attributePaths = {"student", "student.major"})
    @Query("SELECT scs FROM StudentCreditSummary scs " +
           "WHERE scs.student.major.id = :majorId")
    List<StudentCreditSummary> findByMajorId(@Param("majorId") Long majorId);

    /**
     * 查询所有学分汇总 - 预加载学生信息
     *
     * @return 学分汇总列表
     */
    @EntityGraph(attributePaths = {"student", "student.major", "student.major.department"})
    @Query("SELECT scs FROM StudentCreditSummary scs")
    List<StudentCreditSummary> findAllWithDetails();

    /**
     * 根据入学年份查询学生学分汇总
     *
     * @param enrollmentYear 入学年份
     * @return 学分汇总列表
     */
    @EntityGraph(attributePaths = {"student", "student.major"})
    @Query("SELECT scs FROM StudentCreditSummary scs " +
           "WHERE scs.student.enrollmentYear = :enrollmentYear")
    List<StudentCreditSummary> findByEnrollmentYear(@Param("enrollmentYear") Integer enrollmentYear);
}

