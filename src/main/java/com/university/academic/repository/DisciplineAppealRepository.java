package com.university.academic.repository;

import com.university.academic.entity.DisciplineAppeal;
import com.university.academic.entity.enums.AppealStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 处分申诉Repository
 *
 * @author Academic System Team
 */
@Repository
public interface DisciplineAppealRepository extends JpaRepository<DisciplineAppeal, Long> {

    /**
     * 查询学生的申诉记录
     */
    List<DisciplineAppeal> findByStudentId(Long studentId);

    /**
     * 查询处分的申诉记录
     */
    List<DisciplineAppeal> findByDisciplineId(Long disciplineId);

    /**
     * 查询待审核的申诉
     */
    Page<DisciplineAppeal> findByStatus(AppealStatus status, Pageable pageable);

    /**
     * 根据条件查询申诉
     */
    @Query("SELECT a FROM DisciplineAppeal a WHERE " +
           "(:status IS NULL OR a.status = :status) " +
           "AND (:studentId IS NULL OR a.student.id = :studentId)")
    Page<DisciplineAppeal> findByConditions(
            @Param("status") AppealStatus status,
            @Param("studentId") Long studentId,
            Pageable pageable);

    /**
     * 检查处分是否有待审核的申诉
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
           "FROM DisciplineAppeal a " +
           "WHERE a.discipline.id = :disciplineId " +
           "AND a.status = 'PENDING'")
    boolean hasPendingAppeal(@Param("disciplineId") Long disciplineId);

    /**
     * 查询处分的最新申诉
     */
    Optional<DisciplineAppeal> findFirstByDisciplineIdOrderByCreatedAtDesc(Long disciplineId);
}

