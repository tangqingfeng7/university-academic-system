package com.university.academic.repository;

import com.university.academic.entity.CoursePrerequisite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 先修课程数据访问接口
 * 提供先修课程关系的CRUD操作
 *
 * @author Academic System Team
 */
@Repository
public interface CoursePrerequisiteRepository extends JpaRepository<CoursePrerequisite, Long> {

    /**
     * 根据课程ID查询所有先修课程
     *
     * @param courseId 课程ID
     * @return 先修课程列表
     */
    @Query("SELECT cp FROM CoursePrerequisite cp " +
           "LEFT JOIN FETCH cp.prerequisiteCourse pc " +
           "LEFT JOIN FETCH pc.department " +
           "WHERE cp.course.id = :courseId")
    List<CoursePrerequisite> findByCourseId(@Param("courseId") Long courseId);

    /**
     * 查找某个课程和先修课程的关系
     *
     * @param courseId             课程ID
     * @param prerequisiteCourseId 先修课程ID
     * @return 先修课程关系
     */
    @Query("SELECT cp FROM CoursePrerequisite cp " +
           "LEFT JOIN FETCH cp.prerequisiteCourse pc " +
           "LEFT JOIN FETCH pc.department " +
           "WHERE cp.course.id = :courseId AND cp.prerequisiteCourse.id = :prerequisiteCourseId")
    Optional<CoursePrerequisite> findByCourseIdAndPrerequisiteCourseId(
            @Param("courseId") Long courseId,
            @Param("prerequisiteCourseId") Long prerequisiteCourseId);

    /**
     * 检查是否存在先修课程关系
     *
     * @param courseId             课程ID
     * @param prerequisiteCourseId 先修课程ID
     * @return true-存在，false-不存在
     */
    @Query("SELECT CASE WHEN COUNT(cp) > 0 THEN true ELSE false END " +
            "FROM CoursePrerequisite cp " +
            "WHERE cp.course.id = :courseId AND cp.prerequisiteCourse.id = :prerequisiteCourseId")
    boolean existsByCourseIdAndPrerequisiteCourseId(
            @Param("courseId") Long courseId,
            @Param("prerequisiteCourseId") Long prerequisiteCourseId);

    /**
     * 删除课程的所有先修课程
     *
     * @param courseId 课程ID
     */
    @Query("DELETE FROM CoursePrerequisite cp WHERE cp.course.id = :courseId")
    void deleteByCourseId(@Param("courseId") Long courseId);

    /**
     * 检查是否存在循环依赖
     * 检查prerequisiteCourseId是否依赖courseId
     *
     * @param courseId             课程ID
     * @param prerequisiteCourseId 先修课程ID
     * @return true-存在循环依赖，false-不存在循环依赖
     */
    @Query("SELECT CASE WHEN COUNT(cp) > 0 THEN true ELSE false END " +
            "FROM CoursePrerequisite cp " +
            "WHERE cp.course.id = :prerequisiteCourseId AND cp.prerequisiteCourse.id = :courseId")
    boolean hasCircularDependency(
            @Param("courseId") Long courseId,
            @Param("prerequisiteCourseId") Long prerequisiteCourseId);
}

