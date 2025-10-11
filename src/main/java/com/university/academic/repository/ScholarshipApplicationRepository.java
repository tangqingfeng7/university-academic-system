package com.university.academic.repository;

import com.university.academic.entity.ApplicationStatus;
import com.university.academic.entity.ScholarshipApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 奖学金申请Repository接口
 */
@Repository
public interface ScholarshipApplicationRepository extends JpaRepository<ScholarshipApplication, Long>, 
        JpaSpecificationExecutor<ScholarshipApplication> {
    
    /**
     * 查询学生的申请记录
     *
     * @param studentId 学生ID
     * @return 申请记录列表
     */
    @Query("SELECT sa FROM ScholarshipApplication sa " +
           "JOIN FETCH sa.scholarship " +
           "WHERE sa.student.id = :studentId " +
           "ORDER BY sa.submittedAt DESC")
    List<ScholarshipApplication> findByStudentIdWithScholarship(@Param("studentId") Long studentId);
    
    /**
     * 查询学生在指定学年的申请
     *
     * @param studentId 学生ID
     * @param academicYear 学年
     * @return 申请记录列表
     */
    List<ScholarshipApplication> findByStudentIdAndAcademicYear(Long studentId, String academicYear);
    
    /**
     * 检查学生是否已申请指定奖学金
     *
     * @param studentId 学生ID
     * @param scholarshipId 奖学金ID
     * @param academicYear 学年
     * @return 是否已申请
     */
    boolean existsByStudentIdAndScholarshipIdAndAcademicYear(
            Long studentId, Long scholarshipId, String academicYear);
    
    /**
     * 按奖学金和学年查询申请
     *
     * @param scholarshipId 奖学金ID
     * @param academicYear 学年
     * @param pageable 分页参数
     * @return 申请记录分页
     */
    @Query("SELECT sa FROM ScholarshipApplication sa " +
           "JOIN FETCH sa.student s " +
           "WHERE sa.scholarship.id = :scholarshipId " +
           "AND sa.academicYear = :academicYear " +
           "ORDER BY sa.comprehensiveScore DESC, sa.submittedAt ASC")
    Page<ScholarshipApplication> findByScholarshipIdAndAcademicYearOrderByScore(
            @Param("scholarshipId") Long scholarshipId,
            @Param("academicYear") String academicYear,
            Pageable pageable);
    
    /**
     * 按状态查询申请
     *
     * @param status 申请状态
     * @param pageable 分页参数
     * @return 申请记录分页
     */
    @Query("SELECT sa FROM ScholarshipApplication sa " +
           "JOIN FETCH sa.scholarship " +
           "JOIN FETCH sa.student " +
           "WHERE sa.status = :status " +
           "ORDER BY sa.submittedAt DESC")
    Page<ScholarshipApplication> findByStatusWithDetails(
            @Param("status") ApplicationStatus status, Pageable pageable);
    
    /**
     * 查询指定奖学金和学年的申请统计
     *
     * @param scholarshipId 奖学金ID
     * @param academicYear 学年
     * @return 申请数量
     */
    long countByScholarshipIdAndAcademicYear(Long scholarshipId, String academicYear);
    
    /**
     * 查询指定奖学金、学年和状态的申请数量
     *
     * @param scholarshipId 奖学金ID
     * @param academicYear 学年
     * @param status 申请状态
     * @return 申请数量
     */
    long countByScholarshipIdAndAcademicYearAndStatus(
            Long scholarshipId, String academicYear, ApplicationStatus status);
    
    /**
     * 查询指定奖学金和学年的所有申请（带学生信息）
     *
     * @param scholarshipId 奖学金ID
     * @param academicYear 学年
     * @return 申请记录列表
     */
    @Query("SELECT sa FROM ScholarshipApplication sa " +
           "JOIN FETCH sa.student " +
           "WHERE sa.scholarship.id = :scholarshipId " +
           "AND sa.academicYear = :academicYear " +
           "ORDER BY sa.submittedAt ASC")
    List<ScholarshipApplication> findByScholarshipIdAndAcademicYear(
            @Param("scholarshipId") Long scholarshipId,
            @Param("academicYear") String academicYear);
}

