package com.university.academic.repository;

import com.university.academic.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 成绩数据访问接口
 * 提供成绩数据的CRUD操作
 *
 * @author Academic System Team
 */
@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    /**
     * 根据选课记录ID查询成绩
     *
     * @param courseSelectionId 选课记录ID
     * @return 成绩对象
     */
    Optional<Grade> findByCourseSelectionId(Long courseSelectionId);

    /**
     * 查询开课计划的所有成绩
     *
     * @param offeringId 开课计划ID
     * @return 成绩列表
     */
    @Query("SELECT g FROM Grade g " +
            "WHERE g.courseSelection.offering.id = :offeringId")
    List<Grade> findByOfferingId(@Param("offeringId") Long offeringId);

    /**
     * 查询学生的所有成绩
     *
     * @param studentId 学生ID
     * @return 成绩列表
     */
    @Query("SELECT g FROM Grade g " +
            "WHERE g.courseSelection.student.id = :studentId")
    List<Grade> findByStudentId(@Param("studentId") Long studentId);

    /**
     * 查询学生在指定学期的成绩
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 成绩列表
     */
    @Query("SELECT g FROM Grade g " +
            "WHERE g.courseSelection.student.id = :studentId " +
            "AND g.courseSelection.offering.semester.id = :semesterId")
    List<Grade> findByStudentAndSemester(@Param("studentId") Long studentId,
                                         @Param("semesterId") Long semesterId);

    /**
     * 查询学生的已公布成绩（预加载所有关联）
     *
     * @param studentId 学生ID
     * @return 成绩列表
     */
    @Query("SELECT DISTINCT g FROM Grade g " +
            "LEFT JOIN FETCH g.courseSelection cs " +
            "LEFT JOIN FETCH cs.student st " +
            "LEFT JOIN FETCH cs.offering co " +
            "LEFT JOIN FETCH co.course c " +
            "LEFT JOIN FETCH co.semester s " +
            "LEFT JOIN FETCH co.teacher t " +
            "WHERE st.id = :studentId " +
            "AND g.status = 'PUBLISHED'")
    List<Grade> findPublishedByStudentId(@Param("studentId") Long studentId);

    /**
     * 查询学生在指定学期的已公布成绩（预加载所有关联）
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 成绩列表
     */
    @Query("SELECT DISTINCT g FROM Grade g " +
            "LEFT JOIN FETCH g.courseSelection cs " +
            "LEFT JOIN FETCH cs.student st " +
            "LEFT JOIN FETCH cs.offering co " +
            "LEFT JOIN FETCH co.course c " +
            "LEFT JOIN FETCH co.semester s " +
            "LEFT JOIN FETCH co.teacher t " +
            "WHERE st.id = :studentId " +
            "AND s.id = :semesterId " +
            "AND g.status = 'PUBLISHED'")
    List<Grade> findPublishedByStudentAndSemester(@Param("studentId") Long studentId,
                                                   @Param("semesterId") Long semesterId);

    /**
     * 检查成绩是否存在
     *
     * @param courseSelectionId 选课记录ID
     * @return true-存在，false-不存在
     */
    boolean existsByCourseSelectionId(Long courseSelectionId);

    /**
     * 查询指定学期的所有成绩
     *
     * @param semesterId 学期ID
     * @return 成绩列表
     */
    @Query("SELECT g FROM Grade g " +
            "WHERE g.courseSelection.offering.semester.id = :semesterId")
    List<Grade> findBySemesterId(@Param("semesterId") Long semesterId);
}

