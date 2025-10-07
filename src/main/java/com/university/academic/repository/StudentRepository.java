package com.university.academic.repository;

import com.university.academic.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 学生数据访问接口
 *
 * @author Academic System Team
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * 根据学号查询学生
     *
     * @param studentNo 学号
     * @return 学生对象
     */
    Optional<Student> findByStudentNo(String studentNo);

    /**
     * 检查学号是否存在
     *
     * @param studentNo 学号
     * @return true-存在，false-不存在
     */
    boolean existsByStudentNo(String studentNo);

    /**
     * 根据专业ID查询学生列表
     *
     * @param majorId 专业ID
     * @return 学生列表
     */
    List<Student> findByMajorId(Long majorId);

    /**
     * 查询未删除的学生（分页）- 使用EntityGraph避免N+1查询
     *
     * @param pageable 分页参数
     * @return 学生分页数据
     */
    @EntityGraph(attributePaths = {"user", "major", "major.department"})
    @Query("SELECT s FROM Student s WHERE s.deleted = false")
    Page<Student> findByDeletedFalse(Pageable pageable);

    /**
     * 搜索学生（按学号、姓名、班级）- 使用EntityGraph避免N+1查询
     *
     * @param keyword  关键词
     * @param pageable 分页参数
     * @return 学生分页数据
     */
    @EntityGraph(attributePaths = {"user", "major", "major.department"})
    @Query("SELECT s FROM Student s WHERE s.deleted = false AND " +
            "(s.studentNo LIKE %:keyword% OR s.name LIKE %:keyword% OR s.className LIKE %:keyword%)")
    Page<Student> searchStudents(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据专业ID搜索学生 - 使用EntityGraph避免N+1查询
     *
     * @param majorId  专业ID
     * @param keyword  关键词
     * @param pageable 分页参数
     * @return 学生分页数据
     */
    @EntityGraph(attributePaths = {"user", "major", "major.department"})
    @Query("SELECT s FROM Student s WHERE s.deleted = false AND s.major.id = :majorId AND " +
            "(s.studentNo LIKE %:keyword% OR s.name LIKE %:keyword% OR s.className LIKE %:keyword%)")
    Page<Student> searchStudentsByMajor(@Param("majorId") Long majorId,
                                         @Param("keyword") String keyword,
                                         Pageable pageable);

    /**
     * 根据多条件搜索学生（专业+班级）
     *
     * @param majorId   专业ID
     * @param className 班级名称
     * @param keyword   关键词
     * @param pageable  分页参数
     * @return 学生分页数据
     */
    @EntityGraph(attributePaths = {"user", "major", "major.department"})
    @Query("SELECT s FROM Student s WHERE s.deleted = false " +
            "AND (:majorId IS NULL OR s.major.id = :majorId) " +
            "AND (:className IS NULL OR s.className = :className) " +
            "AND (s.studentNo LIKE %:keyword% OR s.name LIKE %:keyword%)")
    Page<Student> searchStudentsByConditions(@Param("majorId") Long majorId,
                                              @Param("className") String className,
                                              @Param("keyword") String keyword,
                                              Pageable pageable);

    /**
     * 统计未删除的学生数量
     *
     * @return 学生数量
     */
    long countByDeletedFalse();

    /**
     * 根据用户ID查询学生
     *
     * @param userId 用户ID
     * @return 学生对象
     */
    Optional<Student> findByUserId(Long userId);

    /**
     * 根据ID查询学生详情 - 预加载所有关联
     *
     * @param id 学生ID
     * @return 学生对象
     */
    @EntityGraph(attributePaths = {"user", "major", "major.department"})
    @Query("SELECT s FROM Student s WHERE s.id = :id")
    Optional<Student> findByIdWithDetails(@Param("id") Long id);
}

