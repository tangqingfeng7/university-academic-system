package com.university.academic.repository;

import com.university.academic.entity.Exam;
import com.university.academic.entity.ExamStatus;
import com.university.academic.entity.ExamType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 考试数据访问接口
 * 提供考试数据的CRUD操作
 *
 * @author Academic System Team
 */
@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    /**
     * 根据ID查询考试（带所有关联实体）
     *
     * @param id 考试ID
     * @return 考试对象
     */
    @Query("SELECT DISTINCT e FROM Exam e " +
           "LEFT JOIN FETCH e.courseOffering co " +
           "LEFT JOIN FETCH co.semester " +
           "LEFT JOIN FETCH co.course c " +
           "LEFT JOIN FETCH c.department " +
           "LEFT JOIN FETCH co.teacher t " +
           "LEFT JOIN FETCH t.department " +
           "WHERE e.id = :id")
    Optional<Exam> findByIdWithDetails(@Param("id") Long id);

    /**
     * 根据开课计划ID查询考试列表
     *
     * @param offeringId 开课计划ID
     * @return 考试列表
     */
    List<Exam> findByCourseOfferingId(Long offeringId);

    /**
     * 根据学期和状态查询考试（分页）
     *
     * @param semesterId 学期ID（可选）
     * @param status     考试状态（可选）
     * @param pageable   分页参数
     * @return 考试分页数据
     */
    @Query("SELECT DISTINCT e FROM Exam e " +
           "LEFT JOIN FETCH e.examRooms " +
           "LEFT JOIN FETCH e.courseOffering co " +
           "LEFT JOIN FETCH co.semester " +
           "LEFT JOIN FETCH co.course " +
           "WHERE (:semesterId IS NULL OR co.semester.id = :semesterId) " +
           "AND (:status IS NULL OR e.status = :status)")
    Page<Exam> findBySemesterAndStatus(
            @Param("semesterId") Long semesterId,
            @Param("status") ExamStatus status,
            Pageable pageable);

    /**
     * 多条件查询考试（分页）
     *
     * @param semesterId 学期ID（可选）
     * @param status     考试状态（可选）
     * @param type       考试类型（可选）
     * @param courseNo   课程编号（可选）
     * @param courseName 课程名称（可选）
     * @param pageable   分页参数
     * @return 考试分页数据
     */
    @Query("SELECT DISTINCT e FROM Exam e " +
           "LEFT JOIN FETCH e.examRooms " +
           "LEFT JOIN FETCH e.courseOffering co " +
           "LEFT JOIN FETCH co.semester " +
           "LEFT JOIN FETCH co.course c " +
           "WHERE (:semesterId IS NULL OR co.semester.id = :semesterId) " +
           "AND (:status IS NULL OR e.status = :status) " +
           "AND (:type IS NULL OR e.type = :type) " +
           "AND (:courseNo IS NULL OR c.courseNo LIKE CONCAT('%', :courseNo, '%')) " +
           "AND (:courseName IS NULL OR c.name LIKE CONCAT('%', :courseName, '%'))")
    Page<Exam> findWithFilters(
            @Param("semesterId") Long semesterId,
            @Param("status") ExamStatus status,
            @Param("type") ExamType type,
            @Param("courseNo") String courseNo,
            @Param("courseName") String courseName,
            Pageable pageable);

    /**
     * 查询指定时间段内的考试
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 考试列表
     */
    @Query("SELECT e FROM Exam e " +
           "WHERE e.examTime BETWEEN :startTime AND :endTime")
    List<Exam> findByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询学生的考试列表
     *
     * @param studentId 学生ID
     * @param status    考试状态（可选）
     * @return 考试列表
     */
    @Query("SELECT DISTINCT e FROM Exam e " +
           "JOIN e.examRooms er " +
           "JOIN er.students ers " +
           "WHERE ers.student.id = :studentId " +
           "AND (:status IS NULL OR e.status = :status)")
    List<Exam> findByStudentIdAndStatus(
            @Param("studentId") Long studentId,
            @Param("status") ExamStatus status);

    /**
     * 查询教师任教课程的考试列表
     *
     * @param teacherId 教师ID
     * @return 考试列表
     */
    @Query("SELECT DISTINCT e FROM Exam e " +
           "LEFT JOIN FETCH e.courseOffering co " +
           "LEFT JOIN FETCH co.semester " +
           "LEFT JOIN FETCH co.course " +
           "WHERE co.teacher.id = :teacherId")
    List<Exam> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 查询教师任教课程的考试列表（指定学期）
     *
     * @param teacherId  教师ID
     * @param semesterId 学期ID
     * @return 考试列表
     */
    @Query("SELECT DISTINCT e FROM Exam e " +
           "LEFT JOIN FETCH e.courseOffering co " +
           "LEFT JOIN FETCH co.semester " +
           "LEFT JOIN FETCH co.course " +
           "WHERE co.teacher.id = :teacherId " +
           "AND co.semester.id = :semesterId")
    List<Exam> findByTeacherIdAndSemesterId(
            @Param("teacherId") Long teacherId,
            @Param("semesterId") Long semesterId);

    /**
     * 根据状态查询考试列表
     *
     * @param status 考试状态
     * @return 考试列表
     */
    List<Exam> findByStatus(ExamStatus status);

    /**
     * 查询指定时间段内教师的考试（用于冲突检测）
     *
     * @param teacherId 教师ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 考试列表
     */
    @Query("SELECT e FROM Exam e " +
           "WHERE e.courseOffering.teacher.id = :teacherId " +
           "AND e.examTime BETWEEN :startTime AND :endTime " +
           "AND e.status != 'CANCELLED'")
    List<Exam> findByTeacherIdAndTimeRange(
            @Param("teacherId") Long teacherId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 统计学期内的考试数量
     *
     * @param semesterId 学期ID
     * @return 考试数量
     */
    @Query("SELECT COUNT(e) FROM Exam e " +
           "WHERE e.courseOffering.semester.id = :semesterId")
    long countBySemesterId(@Param("semesterId") Long semesterId);

    /**
     * 统计学期内指定状态的考试数量
     *
     * @param semesterId 学期ID
     * @param status     考试状态
     * @return 考试数量
     */
    @Query("SELECT COUNT(e) FROM Exam e " +
           "WHERE e.courseOffering.semester.id = :semesterId " +
           "AND e.status = :status")
    long countBySemesterIdAndStatus(
            @Param("semesterId") Long semesterId,
            @Param("status") ExamStatus status);
}

