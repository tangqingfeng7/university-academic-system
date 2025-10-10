package com.university.academic.repository;

import com.university.academic.entity.CourseEvaluation;
import com.university.academic.entity.EvaluationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 课程评价数据访问接口
 *
 * @author Academic System Team
 */
@Repository
public interface CourseEvaluationRepository extends JpaRepository<CourseEvaluation, Long> {

    /**
     * 检查学生是否已评价某门课程
     *
     * @param studentId       学生ID
     * @param offeringId      开课计划ID
     * @return true-已评价，false-未评价
     */
    boolean existsByStudentIdAndCourseOfferingId(Long studentId, Long offeringId);

    /**
     * 查询学生对某门课程的评价
     *
     * @param studentId  学生ID
     * @param offeringId 开课计划ID
     * @return 评价对象
     */
    @Query("SELECT ce FROM CourseEvaluation ce " +
           "WHERE ce.student.id = :studentId AND ce.courseOffering.id = :offeringId")
    Optional<CourseEvaluation> findByStudentIdAndOfferingId(@Param("studentId") Long studentId,
                                                             @Param("offeringId") Long offeringId);

    /**
     * 查询学生的所有评价（分页）
     *
     * @param studentId 学生ID
     * @param pageable  分页参数
     * @return 评价分页数据
     */
    @Query("SELECT ce FROM CourseEvaluation ce " +
           "JOIN FETCH ce.courseOffering co " +
           "JOIN FETCH co.course c " +
           "WHERE ce.student.id = :studentId " +
           "ORDER BY ce.createdAt DESC")
    Page<CourseEvaluation> findByStudentId(@Param("studentId") Long studentId, Pageable pageable);

    /**
     * 查询某门课程的所有评价
     *
     * @param offeringId 开课计划ID
     * @return 评价列表
     */
    @Query("SELECT ce FROM CourseEvaluation ce " +
           "WHERE ce.courseOffering.id = :offeringId AND ce.status = 'SUBMITTED'")
    List<CourseEvaluation> findByOfferingId(@Param("offeringId") Long offeringId);

    /**
     * 查询某门课程的所有评价（带学生信息，用于管理员查看）
     *
     * @param offeringId 开课计划ID
     * @param pageable   分页参数
     * @return 评价分页数据
     */
    @Query("SELECT ce FROM CourseEvaluation ce " +
           "JOIN FETCH ce.student s " +
           "WHERE ce.courseOffering.id = :offeringId")
    Page<CourseEvaluation> findByOfferingIdWithDetails(@Param("offeringId") Long offeringId,
                                                        Pageable pageable);

    /**
     * 统计某门课程的评价数量
     *
     * @param offeringId 开课计划ID
     * @return 评价数量
     */
    long countByCourseOfferingId(Long offeringId);

    /**
     * 统计某门课程的已提交评价数量
     *
     * @param offeringId 开课计划ID
     * @param status     评价状态
     * @return 评价数量
     */
    long countByCourseOfferingIdAndStatus(Long offeringId, EvaluationStatus status);

    /**
     * 查询某学期的所有评价
     *
     * @param semesterId 学期ID
     * @param pageable   分页参数
     * @return 评价分页数据
     */
    @Query("SELECT ce FROM CourseEvaluation ce " +
           "JOIN FETCH ce.courseOffering co " +
           "JOIN FETCH co.course c " +
           "WHERE ce.semesterId = :semesterId")
    Page<CourseEvaluation> findBySemesterId(@Param("semesterId") Long semesterId, Pageable pageable);

    /**
     * 计算某门课程的平均评分
     *
     * @param offeringId 开课计划ID
     * @return 平均评分
     */
    @Query("SELECT AVG(ce.rating) FROM CourseEvaluation ce " +
           "WHERE ce.courseOffering.id = :offeringId AND ce.status = 'SUBMITTED'")
    Double calculateAverageRating(@Param("offeringId") Long offeringId);

    /**
     * 统计某门课程的各星级评价数量
     *
     * @param offeringId 开课计划ID
     * @param rating     星级
     * @return 评价数量
     */
    @Query("SELECT COUNT(ce) FROM CourseEvaluation ce " +
           "WHERE ce.courseOffering.id = :offeringId AND ce.rating = :rating " +
           "AND ce.status = 'SUBMITTED'")
    long countByOfferingIdAndRating(@Param("offeringId") Long offeringId,
                                    @Param("rating") Integer rating);

    /**
     * 查询学生在某学期可以评价的课程（已选课但未评价）
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 开课计划ID列表
     */
    @Query("SELECT cs.offering.id FROM CourseSelection cs " +
           "WHERE cs.student.id = :studentId " +
           "AND cs.offering.semester.id = :semesterId " +
           "AND cs.status = 'SELECTED' " +
           "AND NOT EXISTS (" +
           "    SELECT 1 FROM CourseEvaluation ce " +
           "    WHERE ce.student.id = :studentId " +
           "    AND ce.courseOffering.id = cs.offering.id" +
           ")")
    List<Long> findUnevaluatedOfferingIds(@Param("studentId") Long studentId,
                                           @Param("semesterId") Long semesterId);
}

