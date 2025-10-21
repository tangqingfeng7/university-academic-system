package com.university.academic.repository;

import com.university.academic.entity.CourseOffering;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 开课计划数据访问接口
 * 提供开课计划数据的CRUD操作
 *
 * @author Academic System Team
 */
@Repository
public interface CourseOfferingRepository extends JpaRepository<CourseOffering, Long> {

    /**
     * 根据ID查询开课计划（带所有关联实体）
     *
     * @param id 开课计划ID
     * @return 开课计划对象
     */
    @Query("SELECT co FROM CourseOffering co " +
           "LEFT JOIN FETCH co.semester " +
           "LEFT JOIN FETCH co.course c " +
           "LEFT JOIN FETCH c.department " +
           "LEFT JOIN FETCH co.teacher t " +
           "LEFT JOIN FETCH t.department " +
           "WHERE co.id = :id")
    @NonNull
    java.util.Optional<CourseOffering> findById(@NonNull @Param("id") Long id);

    /**
     * 根据学期ID查询开课计划列表（带关联实体）
     *
     * @param semesterId 学期ID
     * @return 开课计划列表
     */
    @Query("SELECT DISTINCT co FROM CourseOffering co " +
           "LEFT JOIN FETCH co.semester " +
           "LEFT JOIN FETCH co.course c " +
           "LEFT JOIN FETCH c.department " +
           "LEFT JOIN FETCH co.teacher t " +
           "LEFT JOIN FETCH t.department " +
           "WHERE co.semester.id = :semesterId")
    List<CourseOffering> findBySemesterId(@Param("semesterId") Long semesterId);

    /**
     * 根据学期ID分页查询开课计划
     *
     * @param semesterId 学期ID
     * @param pageable   分页参数
     * @return 开课计划分页数据
     */
    Page<CourseOffering> findBySemesterId(Long semesterId, Pageable pageable);

    /**
     * 根据课程ID查询开课计划列表（带关联实体）
     *
     * @param courseId 课程ID
     * @return 开课计划列表
     */
    @Query("SELECT DISTINCT co FROM CourseOffering co " +
           "LEFT JOIN FETCH co.semester " +
           "LEFT JOIN FETCH co.course c " +
           "LEFT JOIN FETCH c.department " +
           "LEFT JOIN FETCH co.teacher t " +
           "LEFT JOIN FETCH t.department " +
           "WHERE co.course.id = :courseId")
    List<CourseOffering> findByCourseId(@Param("courseId") Long courseId);

    /**
     * 根据教师ID查询开课计划列表
     *
     * @param teacherId 教师ID
     * @return 开课计划列表
     */
    @Query("SELECT co FROM CourseOffering co " +
           "LEFT JOIN FETCH co.semester " +
           "LEFT JOIN FETCH co.course c " +
           "LEFT JOIN FETCH c.department " +
           "LEFT JOIN FETCH co.teacher t " +
           "LEFT JOIN FETCH t.department " +
           "WHERE co.teacher.id = :teacherId")
    List<CourseOffering> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 根据教师ID和学期ID查询开课计划列表（带关联实体）
     *
     * @param teacherId  教师ID
     * @param semesterId 学期ID
     * @return 开课计划列表
     */
    @Query("SELECT DISTINCT co FROM CourseOffering co " +
           "LEFT JOIN FETCH co.semester " +
           "LEFT JOIN FETCH co.course c " +
           "LEFT JOIN FETCH c.department " +
           "LEFT JOIN FETCH co.teacher t " +
           "LEFT JOIN FETCH t.department " +
           "WHERE co.teacher.id = :teacherId AND co.semester.id = :semesterId")
    List<CourseOffering> findByTeacherIdAndSemesterId(@Param("teacherId") Long teacherId, 
                                                       @Param("semesterId") Long semesterId);

    /**
     * 根据学期、课程、教师组合查询（可选筛选）
     *
     * @param semesterId 学期ID（可选）
     * @param courseId   课程ID（可选）
     * @param teacherId  教师ID（可选）
     * @param pageable   分页参数
     * @return 开课计划分页数据
     */
    @Query("SELECT co FROM CourseOffering co " +
           "LEFT JOIN FETCH co.semester " +
           "LEFT JOIN FETCH co.course c " +
           "LEFT JOIN FETCH c.department " +
           "LEFT JOIN FETCH co.teacher t " +
           "LEFT JOIN FETCH t.department " +
           "WHERE " +
           "(:semesterId IS NULL OR co.semester.id = :semesterId) AND " +
           "(:courseId IS NULL OR co.course.id = :courseId) AND " +
           "(:teacherId IS NULL OR co.teacher.id = :teacherId)")
    Page<CourseOffering> findWithFilters(@Param("semesterId") Long semesterId,
                                         @Param("courseId") Long courseId,
                                         @Param("teacherId") Long teacherId,
                                         Pageable pageable);

    /**
     * 查询教师在指定学期的所有开课计划（排除指定ID）
     * 用于检测时间冲突
     *
     * @param teacherId  教师ID
     * @param semesterId 学期ID
     * @param excludeId  排除的开课计划ID（更新时排除自己）
     * @return 开课计划列表
     */
    @Query("SELECT co FROM CourseOffering co " +
           "LEFT JOIN FETCH co.semester " +
           "LEFT JOIN FETCH co.course c " +
           "LEFT JOIN FETCH c.department " +
           "LEFT JOIN FETCH co.teacher t " +
           "LEFT JOIN FETCH t.department " +
           "WHERE " +
           "co.teacher.id = :teacherId AND " +
           "co.semester.id = :semesterId AND " +
           "(:excludeId IS NULL OR co.id != :excludeId) AND " +
           "co.status != 'CANCELLED'")
    List<CourseOffering> findByTeacherAndSemesterExcluding(
            @Param("teacherId") Long teacherId,
            @Param("semesterId") Long semesterId,
            @Param("excludeId") Long excludeId);

    /**
     * 查询在指定学期使用指定教室的所有开课计划（排除指定ID）
     * 用于检测教室冲突
     *
     * @param location   教室
     * @param semesterId 学期ID
     * @param excludeId  排除的开课计划ID
     * @return 开课计划列表
     */
    @Query("SELECT co FROM CourseOffering co WHERE " +
            "co.location = :location AND " +
            "co.semester.id = :semesterId AND " +
            "(:excludeId IS NULL OR co.id != :excludeId) AND " +
            "co.status != 'CANCELLED'")
    List<CourseOffering> findByLocationAndSemesterExcluding(
            @Param("location") String location,
            @Param("semesterId") Long semesterId,
            @Param("excludeId") Long excludeId);

    /**
     * 统计开课计划数量
     *
     * @return 开课计划总数
     */
    long count();

    /**
     * 统计指定学期的开课计划数量
     *
     * @param semesterId 学期ID
     * @return 开课计划数量
     */
    long countBySemesterId(Long semesterId);

    /**
     * 查询已发布的开课计划
     *
     * @param semesterId 学期ID
     * @param pageable   分页参数
     * @return 开课计划分页数据
     */
    @Query("SELECT co FROM CourseOffering co WHERE " +
            "co.semester.id = :semesterId AND " +
            "co.status = 'PUBLISHED'")
    Page<CourseOffering> findPublishedBySemester(@Param("semesterId") Long semesterId, Pageable pageable);
}

