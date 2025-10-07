package com.university.academic.repository;

import com.university.academic.entity.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 教师数据访问接口
 * 提供教师数据的CRUD操作
 *
 * @author Academic System Team
 */
@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    /**
     * 根据ID查询教师（带部门和用户信息）
     *
     * @param id 教师ID
     * @return 教师对象
     */
    @Query("SELECT t FROM Teacher t LEFT JOIN FETCH t.department LEFT JOIN FETCH t.user WHERE t.id = :id")
    Optional<Teacher> findById(@Param("id") Long id);

    /**
     * 根据工号查询教师
     *
     * @param teacherNo 工号
     * @return 教师对象
     */
    @Query("SELECT t FROM Teacher t LEFT JOIN FETCH t.department LEFT JOIN FETCH t.user WHERE t.teacherNo = :teacherNo")
    Optional<Teacher> findByTeacherNo(@Param("teacherNo") String teacherNo);

    /**
     * 检查工号是否存在
     *
     * @param teacherNo 工号
     * @return true-存在，false-不存在
     */
    boolean existsByTeacherNo(String teacherNo);

    /**
     * 根据院系ID查询教师列表
     *
     * @param departmentId 院系ID
     * @return 教师列表
     */
    @Query("SELECT t FROM Teacher t LEFT JOIN FETCH t.department LEFT JOIN FETCH t.user WHERE t.department.id = :departmentId")
    List<Teacher> findByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 查询所有教师（带部门和用户信息）
     *
     * @return 教师列表
     */
    @Query("SELECT t FROM Teacher t LEFT JOIN FETCH t.department LEFT JOIN FETCH t.user")
    List<Teacher> findAllWithDepartment();

    /**
     * 查询所有教师（分页，带部门和用户信息）
     *
     * @param pageable 分页参数
     * @return 教师分页数据
     */
    @Query("SELECT t FROM Teacher t LEFT JOIN FETCH t.department LEFT JOIN FETCH t.user")
    Page<Teacher> findAllWithDepartment(Pageable pageable);

    /**
     * 搜索教师（按工号、姓名、职称）
     *
     * @param keyword  关键词
     * @param pageable 分页参数
     * @return 教师分页数据
     */
    @Query("SELECT t FROM Teacher t LEFT JOIN FETCH t.department LEFT JOIN FETCH t.user WHERE " +
            "t.teacherNo LIKE %:keyword% OR " +
            "t.name LIKE %:keyword% OR " +
            "t.title LIKE %:keyword%")
    Page<Teacher> searchTeachers(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 按院系搜索教师
     *
     * @param departmentId 院系ID
     * @param keyword      关键词
     * @param pageable     分页参数
     * @return 教师分页数据
     */
    @Query("SELECT t FROM Teacher t LEFT JOIN FETCH t.department LEFT JOIN FETCH t.user WHERE t.department.id = :departmentId AND " +
            "(t.teacherNo LIKE %:keyword% OR t.name LIKE %:keyword% OR t.title LIKE %:keyword%)")
    Page<Teacher> searchTeachersByDepartment(@Param("departmentId") Long departmentId,
                                            @Param("keyword") String keyword,
                                            Pageable pageable);

    /**
     * 统计教师数量
     *
     * @return 教师总数
     */
    long count();

    /**
     * 检查教师是否有授课任务
     *
     * @param teacherId 教师ID
     * @return true-有授课任务，false-无授课任务
     */
    @Query("SELECT CASE WHEN COUNT(co) > 0 THEN true ELSE false END " +
            "FROM CourseOffering co WHERE co.teacher.id = :teacherId")
    boolean hasActiveOfferings(@Param("teacherId") Long teacherId);

    /**
     * 根据用户ID查询教师
     *
     * @param userId 用户ID
     * @return 教师对象
     */
    @Query("SELECT t FROM Teacher t LEFT JOIN FETCH t.department LEFT JOIN FETCH t.user WHERE t.user.id = :userId")
    Optional<Teacher> findByUserId(@Param("userId") Long userId);
}

