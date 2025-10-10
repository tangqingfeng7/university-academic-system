package com.university.academic.repository;

import com.university.academic.entity.TeacherEvaluation;
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
 * 教师评价数据访问接口
 *
 * @author Academic System Team
 */
@Repository
public interface TeacherEvaluationRepository extends JpaRepository<TeacherEvaluation, Long> {

    /**
     * 检查学生是否已评价某位教师的某门课程
     *
     * @param studentId  学生ID
     * @param teacherId  教师ID
     * @param offeringId 开课计划ID
     * @return true-已评价，false-未评价
     */
    boolean existsByStudentIdAndTeacherIdAndCourseOfferingId(Long studentId,
                                                              Long teacherId,
                                                              Long offeringId);

    /**
     * 查询学生对某位教师的评价
     *
     * @param studentId  学生ID
     * @param teacherId  教师ID
     * @param offeringId 开课计划ID
     * @return 评价对象
     */
    @Query("SELECT te FROM TeacherEvaluation te " +
           "WHERE te.student.id = :studentId AND te.teacher.id = :teacherId " +
           "AND te.courseOffering.id = :offeringId")
    Optional<TeacherEvaluation> findByStudentAndTeacherAndOffering(@Param("studentId") Long studentId,
                                                                     @Param("teacherId") Long teacherId,
                                                                     @Param("offeringId") Long offeringId);

    /**
     * 查询学生的所有教师评价（分页）
     *
     * @param studentId 学生ID
     * @param pageable  分页参数
     * @return 评价分页数据
     */
    @Query("SELECT te FROM TeacherEvaluation te " +
           "JOIN FETCH te.teacher t " +
           "JOIN FETCH te.courseOffering co " +
           "WHERE te.student.id = :studentId " +
           "ORDER BY te.createdAt DESC")
    Page<TeacherEvaluation> findByStudentId(@Param("studentId") Long studentId, Pageable pageable);

    /**
     * 查询某位教师的所有评价
     *
     * @param teacherId 教师ID
     * @return 评价列表
     */
    @Query("SELECT te FROM TeacherEvaluation te " +
           "WHERE te.teacher.id = :teacherId AND te.status = 'SUBMITTED'")
    List<TeacherEvaluation> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 查询某位教师的所有评价（分页）
     *
     * @param teacherId 教师ID
     * @param pageable  分页参数
     * @return 评价分页数据
     */
    @Query("SELECT te FROM TeacherEvaluation te " +
           "WHERE te.teacher.id = :teacherId AND te.status = 'SUBMITTED'")
    Page<TeacherEvaluation> findByTeacherId(@Param("teacherId") Long teacherId, Pageable pageable);

    /**
     * 查询某位教师在某学期的所有评价
     *
     * @param teacherId  教师ID
     * @param semesterId 学期ID
     * @return 评价列表
     */
    @Query("SELECT te FROM TeacherEvaluation te " +
           "JOIN FETCH te.courseOffering co " +
           "WHERE te.teacher.id = :teacherId " +
           "AND co.semester.id = :semesterId " +
           "AND te.status = 'SUBMITTED'")
    List<TeacherEvaluation> findByTeacherIdAndSemesterId(@Param("teacherId") Long teacherId,
                                                          @Param("semesterId") Long semesterId);

    /**
     * 查询某位教师在某学期的所有评价（分页）
     *
     * @param teacherId  教师ID
     * @param semesterId 学期ID
     * @param pageable   分页参数
     * @return 评价分页数据
     */
    @Query("SELECT te FROM TeacherEvaluation te " +
           "JOIN te.courseOffering co " +
           "WHERE te.teacher.id = :teacherId " +
           "AND co.semester.id = :semesterId " +
           "AND te.status = 'SUBMITTED'")
    Page<TeacherEvaluation> findByTeacherIdAndSemesterId(@Param("teacherId") Long teacherId,
                                                          @Param("semesterId") Long semesterId,
                                                          Pageable pageable);

    /**
     * 查询某位教师的所有评价（带学生信息，用于管理员查看）
     *
     * @param teacherId 教师ID
     * @param pageable  分页参数
     * @return 评价分页数据
     */
    @Query("SELECT te FROM TeacherEvaluation te " +
           "JOIN FETCH te.student s " +
           "JOIN FETCH te.courseOffering co " +
           "WHERE te.teacher.id = :teacherId")
    Page<TeacherEvaluation> findByTeacherIdWithDetails(@Param("teacherId") Long teacherId,
                                                        Pageable pageable);

    /**
     * 统计某位教师的评价数量
     *
     * @param teacherId 教师ID
     * @return 评价数量
     */
    long countByTeacherId(Long teacherId);

    /**
     * 统计某位教师的已提交评价数量
     *
     * @param teacherId 教师ID
     * @param status    评价状态
     * @return 评价数量
     */
    long countByTeacherIdAndStatus(Long teacherId, EvaluationStatus status);

    /**
     * 计算某位教师的平均教学能力评分
     *
     * @param teacherId 教师ID
     * @return 平均评分
     */
    @Query("SELECT AVG(te.teachingRating) FROM TeacherEvaluation te " +
           "WHERE te.teacher.id = :teacherId AND te.status = 'SUBMITTED'")
    Double calculateAverageTeachingRating(@Param("teacherId") Long teacherId);

    /**
     * 计算某位教师的平均教学态度评分
     *
     * @param teacherId 教师ID
     * @return 平均评分
     */
    @Query("SELECT AVG(te.attitudeRating) FROM TeacherEvaluation te " +
           "WHERE te.teacher.id = :teacherId AND te.status = 'SUBMITTED'")
    Double calculateAverageAttitudeRating(@Param("teacherId") Long teacherId);

    /**
     * 计算某位教师的平均教学内容评分
     *
     * @param teacherId 教师ID
     * @return 平均评分
     */
    @Query("SELECT AVG(te.contentRating) FROM TeacherEvaluation te " +
           "WHERE te.teacher.id = :teacherId AND te.status = 'SUBMITTED'")
    Double calculateAverageContentRating(@Param("teacherId") Long teacherId);

    /**
     * 查询某门课程的教师评价
     *
     * @param offeringId 开课计划ID
     * @return 评价列表
     */
    @Query("SELECT te FROM TeacherEvaluation te " +
           "WHERE te.courseOffering.id = :offeringId AND te.status = 'SUBMITTED'")
    List<TeacherEvaluation> findByOfferingId(@Param("offeringId") Long offeringId);
}

