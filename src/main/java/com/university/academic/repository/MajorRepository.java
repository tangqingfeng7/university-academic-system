package com.university.academic.repository;

import com.university.academic.entity.Major;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 专业数据访问接口
 *
 * @author Academic System Team
 */
@Repository
public interface MajorRepository extends JpaRepository<Major, Long> {

    /**
     * 根据ID查询专业（带院系信息）
     *
     * @param id 专业ID
     * @return 专业对象
     */
    @Query("SELECT m FROM Major m LEFT JOIN FETCH m.department WHERE m.id = :id")
    Optional<Major> findById(@Param("id") Long id);

    /**
     * 根据专业代码查询专业（带院系信息）
     *
     * @param code 专业代码
     * @return 专业对象
     */
    @Query("SELECT m FROM Major m LEFT JOIN FETCH m.department WHERE m.code = :code")
    Optional<Major> findByCode(@Param("code") String code);

    /**
     * 检查专业代码是否存在
     *
     * @param code 专业代码
     * @return true-存在，false-不存在
     */
    boolean existsByCode(String code);

    /**
     * 根据院系ID查询专业列表（带院系信息）
     *
     * @param departmentId 院系ID
     * @return 专业列表
     */
    @Query("SELECT m FROM Major m LEFT JOIN FETCH m.department WHERE m.department.id = :departmentId")
    List<Major> findByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 根据专业名称查询专业
     *
     * @param name 专业名称
     * @return 专业对象
     */
    Optional<Major> findByName(String name);

    /**
     * 查询所有专业（带院系信息）
     *
     * @return 专业列表
     */
    @Query("SELECT m FROM Major m LEFT JOIN FETCH m.department")
    List<Major> findAllWithDepartment();

    /**
     * 分页查询所有专业（带院系信息）
     *
     * @param pageable 分页参数
     * @return 专业分页数据
     */
    @Query("SELECT m FROM Major m LEFT JOIN FETCH m.department")
    Page<Major> findAllWithDepartment(Pageable pageable);

    /**
     * 统计专业下未删除的学生数量
     *
     * @param majorId 专业ID
     * @return 学生数量
     */
    @Query("SELECT COUNT(s) FROM Student s WHERE s.major.id = :majorId AND s.deleted = false")
    long countStudentsByMajorId(@Param("majorId") Long majorId);
}

