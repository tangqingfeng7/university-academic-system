package com.university.academic.repository;

import com.university.academic.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 学期数据访问接口
 * 提供学期数据的CRUD操作
 *
 * @author Academic System Team
 */
@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {

    /**
     * 查询当前活动学期
     *
     * @return 当前活动学期
     */
    Optional<Semester> findByActiveTrue();

    /**
     * 根据学年和学期类型查询学期
     *
     * @param academicYear 学年
     * @param semesterType 学期类型
     * @return 学期对象
     */
    Optional<Semester> findByAcademicYearAndSemesterType(String academicYear, Integer semesterType);

    /**
     * 检查学年和学期类型是否已存在
     *
     * @param academicYear 学年
     * @param semesterType 学期类型
     * @return true-存在，false-不存在
     */
    boolean existsByAcademicYearAndSemesterType(String academicYear, Integer semesterType);

    /**
     * 查询所有学期（按学年和学期类型降序排序）
     *
     * @return 学期列表
     */
    @Query("SELECT s FROM Semester s ORDER BY s.academicYear DESC, s.semesterType DESC")
    List<Semester> findAllOrderByDesc();

    /**
     * 将所有学期设置为非活动状态
     */
    @Modifying
    @Query("UPDATE Semester s SET s.active = false WHERE s.active = true")
    void deactivateAllSemesters();

    /**
     * 检查学期是否有开课计划
     *
     * @param semesterId 学期ID
     * @return true-有开课计划，false-无开课计划
     */
    @Query("SELECT CASE WHEN COUNT(co) > 0 THEN true ELSE false END " +
            "FROM CourseOffering co WHERE co.semester.id = :semesterId")
    boolean hasOfferings(@Param("semesterId") Long semesterId);

    /**
     * 统计学期数量
     *
     * @return 学期总数
     */
    long count();
}

