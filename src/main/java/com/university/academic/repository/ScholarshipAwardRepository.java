package com.university.academic.repository;

import com.university.academic.entity.ScholarshipAward;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 奖学金获奖记录Repository接口
 */
@Repository
public interface ScholarshipAwardRepository extends JpaRepository<ScholarshipAward, Long> {
    
    /**
     * 查询学生的获奖记录
     *
     * @param studentId 学生ID
     * @return 获奖记录列表
     */
    @Query("SELECT sa FROM ScholarshipAward sa " +
           "JOIN FETCH sa.scholarship " +
           "WHERE sa.student.id = :studentId " +
           "ORDER BY sa.awardedAt DESC")
    List<ScholarshipAward> findByStudentIdWithScholarship(@Param("studentId") Long studentId);
    
    /**
     * 查询指定奖学金和学年的获奖记录
     *
     * @param scholarshipId 奖学金ID
     * @param academicYear 学年
     * @param pageable 分页参数
     * @return 获奖记录分页
     */
    @Query("SELECT sa FROM ScholarshipAward sa " +
           "JOIN FETCH sa.student s " +
           "JOIN FETCH s.major m " +
           "JOIN FETCH m.department " +
           "JOIN FETCH sa.scholarship " +
           "WHERE sa.scholarship.id = :scholarshipId " +
           "AND sa.academicYear = :academicYear " +
           "ORDER BY sa.awardedAt DESC")
    Page<ScholarshipAward> findByScholarshipIdAndAcademicYear(
            @Param("scholarshipId") Long scholarshipId,
            @Param("academicYear") String academicYear,
            Pageable pageable);
    
    /**
     * 查询指定学年的所有获奖记录（不分页）
     *
     * @param academicYear 学年
     * @return 获奖记录列表
     */
    @Query("SELECT sa FROM ScholarshipAward sa " +
           "JOIN FETCH sa.student s " +
           "JOIN FETCH s.major m " +
           "JOIN FETCH m.department " +
           "JOIN FETCH sa.scholarship " +
           "WHERE sa.academicYear = :academicYear " +
           "ORDER BY sa.scholarship.level, sa.awardedAt DESC")
    List<ScholarshipAward> findByAcademicYear(@Param("academicYear") String academicYear);
    
    /**
     * 查询已公示的获奖记录
     *
     * @param academicYear 学年
     * @param pageable 分页参数
     * @return 获奖记录分页
     */
    @Query("SELECT sa FROM ScholarshipAward sa " +
           "JOIN FETCH sa.student s " +
           "JOIN FETCH s.major m " +
           "JOIN FETCH m.department " +
           "JOIN FETCH sa.scholarship " +
           "WHERE sa.academicYear = :academicYear " +
           "AND sa.published = true " +
           "ORDER BY sa.scholarship.level, sa.awardedAt DESC")
    Page<ScholarshipAward> findPublishedByAcademicYear(
            @Param("academicYear") String academicYear,
            Pageable pageable);
    
    /**
     * 检查学生在指定学年是否已获得该奖学金
     *
     * @param studentId 学生ID
     * @param scholarshipId 奖学金ID
     * @param academicYear 学年
     * @return 是否已获奖
     */
    boolean existsByStudentIdAndScholarshipIdAndAcademicYear(
            Long studentId, Long scholarshipId, String academicYear);
    
    /**
     * 统计指定学年的获奖人数
     *
     * @param academicYear 学年
     * @return 获奖人数
     */
    long countByAcademicYear(String academicYear);
    
    /**
     * 查询所有获奖记录（带关联数据）
     *
     * @param pageable 分页参数
     * @return 获奖记录分页
     */
    @Query("SELECT sa FROM ScholarshipAward sa " +
           "JOIN FETCH sa.student s " +
           "JOIN FETCH s.major m " +
           "JOIN FETCH m.department " +
           "JOIN FETCH sa.scholarship " +
           "ORDER BY sa.awardedAt DESC")
    Page<ScholarshipAward> findAllWithDetails(Pageable pageable);
}
