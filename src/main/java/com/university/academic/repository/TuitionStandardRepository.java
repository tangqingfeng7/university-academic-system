package com.university.academic.repository;

import com.university.academic.entity.tuition.TuitionStandard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 学费标准数据访问接口
 *
 * @author Academic System Team
 */
@Repository
public interface TuitionStandardRepository extends JpaRepository<TuitionStandard, Long> {

    /**
     * 根据专业和学年查询学费标准
     */
    @Query("SELECT ts FROM TuitionStandard ts " +
           "WHERE ts.major.id = :majorId " +
           "AND ts.academicYear = :academicYear " +
           "AND ts.gradeLevel = :gradeLevel " +
           "AND ts.active = true")
    Optional<TuitionStandard> findByMajorAndYearAndGrade(
        @Param("majorId") Long majorId,
        @Param("academicYear") String academicYear,
        @Param("gradeLevel") Integer gradeLevel
    );

    /**
     * 查询指定学年的所有学费标准
     */
    @Query("SELECT ts FROM TuitionStandard ts " +
           "WHERE ts.academicYear = :academicYear " +
           "AND ts.active = true " +
           "ORDER BY ts.major.name, ts.gradeLevel")
    List<TuitionStandard> findByAcademicYear(@Param("academicYear") String academicYear);

    /**
     * 查询指定专业的所有学费标准
     */
    @Query("SELECT ts FROM TuitionStandard ts " +
           "WHERE ts.major.id = :majorId " +
           "AND ts.active = true " +
           "ORDER BY ts.academicYear DESC, ts.gradeLevel")
    List<TuitionStandard> findByMajorId(@Param("majorId") Long majorId);

    /**
     * 检查是否存在相同的学费标准
     */
    @Query("SELECT COUNT(ts) > 0 FROM TuitionStandard ts " +
           "WHERE ts.major.id = :majorId " +
           "AND ts.academicYear = :academicYear " +
           "AND ts.gradeLevel = :gradeLevel " +
           "AND (:excludeId IS NULL OR ts.id != :excludeId)")
    boolean existsByMajorAndYearAndGrade(
        @Param("majorId") Long majorId,
        @Param("academicYear") String academicYear,
        @Param("gradeLevel") Integer gradeLevel,
        @Param("excludeId") Long excludeId
    );

    /**
     * 查询所有启用的学费标准
     */
    List<TuitionStandard> findByActiveTrue();

    /**
     * 分页多条件查询学费标准
     */
    @Query(value = "SELECT ts FROM TuitionStandard ts " +
           "LEFT JOIN FETCH ts.major m " +
           "LEFT JOIN FETCH m.department " +
           "WHERE (:academicYear IS NULL OR ts.academicYear = :academicYear) " +
           "AND (:majorId IS NULL OR ts.major.id = :majorId) " +
           "AND (:gradeLevel IS NULL OR ts.gradeLevel = :gradeLevel) " +
           "AND (:active IS NULL OR ts.active = :active) " +
           "ORDER BY ts.academicYear DESC, ts.major.name, ts.gradeLevel",
           countQuery = "SELECT COUNT(ts) FROM TuitionStandard ts " +
           "WHERE (:academicYear IS NULL OR ts.academicYear = :academicYear) " +
           "AND (:majorId IS NULL OR ts.major.id = :majorId) " +
           "AND (:gradeLevel IS NULL OR ts.gradeLevel = :gradeLevel) " +
           "AND (:active IS NULL OR ts.active = :active)")
    Page<TuitionStandard> searchStandards(
        @Param("academicYear") String academicYear,
        @Param("majorId") Long majorId,
        @Param("gradeLevel") Integer gradeLevel,
        @Param("active") Boolean active,
        Pageable pageable
    );
}

