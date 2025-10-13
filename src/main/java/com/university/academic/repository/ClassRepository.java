package com.university.academic.repository;

import com.university.academic.entity.Class;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 班级Repository
 *
 * @author Academic System Team
 */
@Repository
public interface ClassRepository extends JpaRepository<Class, Long> {

    /**
     * 根据班级代码查询（未删除）
     */
    Optional<Class> findByClassCodeAndDeletedFalse(String classCode);

    /**
     * 检查班级代码是否存在（未删除）
     */
    boolean existsByClassCodeAndDeletedFalse(String classCode);

    /**
     * 根据专业ID查询班级列表（未删除）
     */
    List<Class> findByMajorIdAndDeletedFalseOrderByEnrollmentYearDescClassCodeAsc(Long majorId);

    /**
     * 分页查询班级（未删除）
     */
    Page<Class> findByDeletedFalse(Pageable pageable);

    /**
     * 根据专业ID分页查询班级（未删除）
     */
    Page<Class> findByMajorIdAndDeletedFalse(Long majorId, Pageable pageable);

    /**
     * 根据入学年份查询班级（未删除）
     */
    List<Class> findByEnrollmentYearAndDeletedFalseOrderByClassCodeAsc(Integer enrollmentYear);

    /**
     * 根据专业ID和入学年份查询班级（未删除）
     */
    List<Class> findByMajorIdAndEnrollmentYearAndDeletedFalse(Long majorId, Integer enrollmentYear);

    /**
     * 联合查询班级信息（包含专业和院系信息）
     */
    @Query("SELECT c FROM Class c " +
           "JOIN FETCH c.major m " +
           "JOIN FETCH m.department d " +
           "WHERE c.deleted = false " +
           "ORDER BY c.enrollmentYear DESC, c.classCode ASC")
    List<Class> findAllWithMajorAndDepartment();

    /**
     * 根据ID查询班级（包含关联信息）
     */
    @Query("SELECT c FROM Class c " +
           "JOIN FETCH c.major m " +
           "JOIN FETCH m.department d " +
           "WHERE c.id = :id AND c.deleted = false")
    Optional<Class> findByIdWithDetails(@Param("id") Long id);

    /**
     * 查询所有未分配辅导员的班级
     */
    @Query("SELECT c FROM Class c " +
           "JOIN FETCH c.major m " +
           "JOIN FETCH m.department d " +
           "WHERE c.counselorId IS NULL AND c.deleted = false " +
           "ORDER BY c.enrollmentYear DESC, c.classCode ASC")
    List<Class> findUnassignedClasses();

    /**
     * 根据专业查询未分配辅导员的班级
     */
    @Query("SELECT c FROM Class c " +
           "JOIN FETCH c.major m " +
           "JOIN FETCH m.department d " +
           "WHERE c.major.id = :majorId AND c.counselorId IS NULL AND c.deleted = false " +
           "ORDER BY c.enrollmentYear DESC, c.classCode ASC")
    List<Class> findUnassignedClassesByMajor(@Param("majorId") Long majorId);

    /**
     * 根据入学年份查询未分配辅导员的班级
     */
    @Query("SELECT c FROM Class c " +
           "JOIN FETCH c.major m " +
           "JOIN FETCH m.department d " +
           "WHERE c.enrollmentYear = :enrollmentYear AND c.counselorId IS NULL AND c.deleted = false " +
           "ORDER BY c.classCode ASC")
    List<Class> findUnassignedClassesByYear(@Param("enrollmentYear") Integer enrollmentYear);

    /**
     * 统计指定教师（辅导员）负责的班级数量
     */
    @Query("SELECT COUNT(c) FROM Class c WHERE c.counselorId = :counselorId AND c.deleted = false")
    long countByCounselorId(@Param("counselorId") Long counselorId);

    /**
     * 查询指定教师负责的所有班级
     */
    @Query("SELECT c FROM Class c " +
           "JOIN FETCH c.major m " +
           "JOIN FETCH m.department d " +
           "WHERE c.counselorId = :counselorId AND c.deleted = false " +
           "ORDER BY c.enrollmentYear DESC, c.classCode ASC")
    List<Class> findByCounselorId(@Param("counselorId") Long counselorId);
}

