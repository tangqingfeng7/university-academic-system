package com.university.academic.repository;

import com.university.academic.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 课程数据访问接口
 * 提供课程数据的CRUD操作
 *
 * @author Academic System Team
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * 根据ID查询课程（带院系信息）
     *
     * @param id 课程ID
     * @return 课程对象
     */
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.department WHERE c.id = :id")
    @NonNull
    Optional<Course> findById(@NonNull @Param("id") Long id);

    /**
     * 根据课程编号查询课程
     *
     * @param courseNo 课程编号
     * @return 课程对象
     */
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.department WHERE c.courseNo = :courseNo")
    Optional<Course> findByCourseNo(@Param("courseNo") String courseNo);

    /**
     * 检查课程编号是否存在
     *
     * @param courseNo 课程编号
     * @return true-存在，false-不存在
     */
    boolean existsByCourseNo(String courseNo);

    /**
     * 根据院系ID查询课程列表
     *
     * @param departmentId 院系ID
     * @return 课程列表
     */
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.department WHERE c.department.id = :departmentId")
    List<Course> findByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 查询所有课程（带院系信息）
     *
     * @return 课程列表
     */
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.department")
    List<Course> findAllWithDepartment();

    /**
     * 查询所有课程（分页，带院系信息）
     *
     * @param pageable 分页参数
     * @return 课程分页数据
     */
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.department")
    Page<Course> findAllWithDepartment(Pageable pageable);

    /**
     * 搜索课程（按课程编号、名称）
     *
     * @param keyword  关键词
     * @param pageable 分页参数
     * @return 课程分页数据
     */
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.department WHERE " +
            "c.courseNo LIKE CONCAT('%', :keyword, '%') OR " +
            "c.name LIKE CONCAT('%', :keyword, '%')")
    Page<Course> searchCourses(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 按院系和类型搜索课程
     *
     * @param departmentId 院系ID（可选）
     * @param type         课程类型（可选）
     * @param keyword      关键词（可选）
     * @param pageable     分页参数
     * @return 课程分页数据
     */
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.department WHERE " +
            "(:departmentId IS NULL OR c.department.id = :departmentId) AND " +
            "(:type IS NULL OR c.type = :type) AND " +
            "(:keyword IS NULL OR c.courseNo LIKE CONCAT('%', :keyword, '%') OR c.name LIKE CONCAT('%', :keyword, '%'))")
    Page<Course> searchCoursesWithFilters(@Param("departmentId") Long departmentId,
                                          @Param("type") Course.CourseType type,
                                          @Param("keyword") String keyword,
                                          Pageable pageable);

    /**
     * 统计课程数量
     *
     * @return 课程总数
     */
    long count();

    /**
     * 检查课程是否有开课计划
     *
     * @param courseId 课程ID
     * @return true-有开课计划，false-无开课计划
     */
    @Query("SELECT CASE WHEN COUNT(co) > 0 THEN true ELSE false END " +
            "FROM CourseOffering co WHERE co.course.id = :courseId")
    boolean hasOfferings(@Param("courseId") Long courseId);

    /**
     * 检查课程是否有学生选课
     *
     * @param courseId 课程ID
     * @return true-有学生选课，false-无学生选课
     */
    @Query("SELECT CASE WHEN COUNT(cs) > 0 THEN true ELSE false END " +
            "FROM CourseSelection cs " +
            "WHERE cs.offering.course.id = :courseId")
    boolean hasStudentSelections(@Param("courseId") Long courseId);
}

