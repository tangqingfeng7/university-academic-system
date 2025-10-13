package com.university.academic.repository;

import com.university.academic.entity.StudentDiscipline;
import com.university.academic.entity.enums.ApprovalStatus;
import com.university.academic.entity.enums.DisciplineStatus;
import com.university.academic.entity.enums.DisciplineType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 学生处分Repository
 *
 * @author Academic System Team
 */
@Repository
public interface StudentDisciplineRepository extends JpaRepository<StudentDiscipline, Long> {

    /**
     * 查询学生的处分记录（预加载关联数据）
     */
    @EntityGraph(attributePaths = {"student", "student.major", "reporter", "approver", "removedBy"})
    List<StudentDiscipline> findByStudentIdAndDeletedFalse(Long studentId);

    /**
     * 查询学生的有效处分记录（预加载关联数据）
     */
    @EntityGraph(attributePaths = {"student", "student.major", "reporter", "approver", "removedBy"})
    List<StudentDiscipline> findByStudentIdAndStatusAndDeletedFalse(Long studentId, DisciplineStatus status);

    /**
     * 分页查询处分记录（预加载关联数据）
     */
    @EntityGraph(attributePaths = {"student", "student.major", "reporter", "approver", "removedBy"})
    Page<StudentDiscipline> findByDeletedFalse(Pageable pageable);

    /**
     * 根据条件查询处分记录（使用EntityGraph预加载关联数据）
     */
    @EntityGraph(attributePaths = {"student", "student.major", "reporter", "approver", "removedBy"})
    @Query("SELECT d FROM StudentDiscipline d WHERE d.deleted = false " +
           "AND (:studentId IS NULL OR d.student.id = :studentId) " +
           "AND (:reporterId IS NULL OR d.reporter.id = :reporterId) " +
           "AND (:disciplineType IS NULL OR d.disciplineType = :disciplineType) " +
           "AND (:status IS NULL OR d.status = :status) " +
           "AND (:approvalStatus IS NULL OR d.approvalStatus = :approvalStatus) " +
           "AND (:startDate IS NULL OR d.punishmentDate >= :startDate) " +
           "AND (:endDate IS NULL OR d.punishmentDate <= :endDate)")
    Page<StudentDiscipline> findByConditions(
            @Param("studentId") Long studentId,
            @Param("reporterId") Long reporterId,
            @Param("disciplineType") DisciplineType disciplineType,
            @Param("status") DisciplineStatus status,
            @Param("approvalStatus") ApprovalStatus approvalStatus,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
    
    /**
     * 根据ID查询处分记录（预加载关联数据）
     */
    @EntityGraph(attributePaths = {"student", "student.major", "reporter", "approver", "removedBy"})
    Optional<StudentDiscipline> findById(Long id);

    /**
     * 统计学生的处分数量
     */
    Long countByStudentIdAndDeletedFalse(Long studentId);

    /**
     * 统计学生的有效处分数量
     */
    Long countByStudentIdAndStatusAndDeletedFalse(Long studentId, DisciplineStatus status);

    /**
     * 检查学生是否有未解除的处分
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END " +
           "FROM StudentDiscipline d " +
           "WHERE d.student.id = :studentId " +
           "AND d.status = 'ACTIVE' " +
           "AND d.deleted = false")
    boolean hasActiveDiscipline(@Param("studentId") Long studentId);
}

