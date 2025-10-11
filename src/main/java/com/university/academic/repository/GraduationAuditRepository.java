package com.university.academic.repository;

import com.university.academic.entity.GraduationAudit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 毕业审核数据访问接口
 * 提供毕业审核数据的CRUD操作
 *
 * @author Academic System Team
 */
@Repository
public interface GraduationAuditRepository extends JpaRepository<GraduationAudit, Long> {

    /**
     * 根据学生ID查询最新的审核记录
     *
     * @param studentId 学生ID
     * @return 审核记录
     */
    @Query("SELECT ga FROM GraduationAudit ga " +
           "WHERE ga.student.id = :studentId " +
           "ORDER BY ga.auditYear DESC, ga.createdAt DESC")
    Optional<GraduationAudit> findLatestByStudentId(@Param("studentId") Long studentId);

    /**
     * 根据学生ID和审核年份查询审核记录
     *
     * @param studentId 学生ID
     * @param auditYear 审核年份
     * @return 审核记录
     */
    Optional<GraduationAudit> findByStudentIdAndAuditYear(Long studentId, Integer auditYear);

    /**
     * 查询指定年份的所有审核记录 - 预加载学生和专业信息
     *
     * @param auditYear 审核年份
     * @return 审核记录列表
     */
    @EntityGraph(attributePaths = {"student", "student.major", "student.major.department"})
    @Query("SELECT ga FROM GraduationAudit ga WHERE ga.auditYear = :auditYear")
    List<GraduationAudit> findByAuditYearWithDetails(@Param("auditYear") Integer auditYear);

    /**
     * 根据审核状态查询记录
     *
     * @param status 审核状态
     * @return 审核记录列表
     */
    List<GraduationAudit> findByStatus(GraduationAudit.AuditStatus status);

    /**
     * 根据审核状态查询记录 - 预加载学生和专业信息
     *
     * @param status 审核状态
     * @return 审核记录列表
     */
    @EntityGraph(attributePaths = {"student", "student.major", "student.major.department"})
    @Query("SELECT ga FROM GraduationAudit ga WHERE ga.status = :status")
    List<GraduationAudit> findByStatusWithDetails(@Param("status") GraduationAudit.AuditStatus status);

    /**
     * 根据审核状态和年份查询记录 - 预加载学生和专业信息
     *
     * @param status    审核状态
     * @param auditYear 审核年份
     * @return 审核记录列表
     */
    @EntityGraph(attributePaths = {"student", "student.major", "student.major.department"})
    @Query("SELECT ga FROM GraduationAudit ga " +
           "WHERE ga.status = :status AND ga.auditYear = :auditYear")
    List<GraduationAudit> findByStatusAndAuditYearWithDetails(@Param("status") GraduationAudit.AuditStatus status,
                                                               @Param("auditYear") Integer auditYear);

    /**
     * 根据学生ID和审核年份查询审核记录 - 预加载学生和专业信息
     *
     * @param studentId 学生ID
     * @param auditYear 审核年份
     * @return 审核记录
     */
    @EntityGraph(attributePaths = {"student", "student.major", "student.major.department"})
    @Query("SELECT ga FROM GraduationAudit ga " +
           "WHERE ga.student.id = :studentId AND ga.auditYear = :auditYear")
    Optional<GraduationAudit> findByStudentIdAndAuditYearWithDetails(@Param("studentId") Long studentId,
                                                                      @Param("auditYear") Integer auditYear);

    /**
     * 查询所有审核记录 - 预加载学生和专业信息
     *
     * @return 审核记录列表
     */
    @EntityGraph(attributePaths = {"student", "student.major", "student.major.department"})
    @Query("SELECT ga FROM GraduationAudit ga")
    List<GraduationAudit> findAllWithDetails();

    /**
     * 根据学生ID查询最新的审核记录（按审核时间倒序）
     *
     * @param studentId 学生ID
     * @return 审核记录
     */
    @EntityGraph(attributePaths = {"student", "student.major", "student.major.department"})
    @Query("SELECT ga FROM GraduationAudit ga " +
           "WHERE ga.student.id = :studentId " +
           "ORDER BY ga.auditedAt DESC")
    Optional<GraduationAudit> findTopByStudentIdOrderByAuditedAtDesc(@Param("studentId") Long studentId);

    /**
     * 根据ID查询审核记录 - 预加载所有关联
     *
     * @param id 审核记录ID
     * @return 审核记录
     */
    @EntityGraph(attributePaths = {"student", "student.major", "student.major.department", "student.user"})
    @Query("SELECT ga FROM GraduationAudit ga WHERE ga.id = :id")
    Optional<GraduationAudit> findByIdWithDetails(@Param("id") Long id);

    /**
     * 统计指定年份的审核记录数
     *
     * @param auditYear 审核年份
     * @return 审核记录数
     */
    long countByAuditYear(Integer auditYear);

    /**
     * 统计指定年份指定状态的审核记录数
     *
     * @param auditYear 审核年份
     * @param status    审核状态
     * @return 审核记录数
     */
    long countByAuditYearAndStatus(Integer auditYear, GraduationAudit.AuditStatus status);

    /**
     * 检查学生是否存在指定年份的审核记录
     *
     * @param studentId 学生ID
     * @param auditYear 审核年份
     * @return true-存在，false-不存在
     */
    boolean existsByStudentIdAndAuditYear(Long studentId, Integer auditYear);
}

