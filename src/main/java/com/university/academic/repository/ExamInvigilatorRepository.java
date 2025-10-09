package com.university.academic.repository;

import com.university.academic.entity.ExamInvigilator;
import com.university.academic.entity.ExamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 监考安排数据访问接口
 * 提供监考安排数据的CRUD操作
 *
 * @author Academic System Team
 */
@Repository
public interface ExamInvigilatorRepository extends JpaRepository<ExamInvigilator, Long> {

    /**
     * 根据考场ID查询监考安排列表
     *
     * @param examRoomId 考场ID
     * @return 监考安排列表
     */
    List<ExamInvigilator> findByExamRoomId(Long examRoomId);

    /**
     * 根据考场ID查询监考安排列表（带教师信息）
     *
     * @param examRoomId 考场ID
     * @return 监考安排列表
     */
    @Query("SELECT DISTINCT ei FROM ExamInvigilator ei " +
           "LEFT JOIN FETCH ei.teacher t " +
           "LEFT JOIN FETCH t.department " +
           "WHERE ei.examRoom.id = :examRoomId")
    List<ExamInvigilator> findByExamRoomIdWithTeacher(@Param("examRoomId") Long examRoomId);

    /**
     * 根据教师ID查询监考任务列表
     *
     * @param teacherId 教师ID
     * @return 监考安排列表
     */
    @Query("SELECT DISTINCT ei FROM ExamInvigilator ei " +
           "LEFT JOIN FETCH ei.examRoom er " +
           "LEFT JOIN FETCH er.exam e " +
           "LEFT JOIN FETCH e.courseOffering co " +
           "LEFT JOIN FETCH co.course " +
           "LEFT JOIN FETCH co.semester " +
           "WHERE ei.teacher.id = :teacherId")
    List<ExamInvigilator> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 根据教师ID和考试状态查询监考任务
     *
     * @param teacherId 教师ID
     * @param statuses  考试状态列表
     * @return 监考安排列表
     */
    @Query("SELECT DISTINCT ei FROM ExamInvigilator ei " +
           "LEFT JOIN FETCH ei.examRoom er " +
           "LEFT JOIN FETCH er.exam e " +
           "LEFT JOIN FETCH e.courseOffering co " +
           "LEFT JOIN FETCH co.course " +
           "LEFT JOIN FETCH co.semester " +
           "WHERE ei.teacher.id = :teacherId " +
           "AND e.status IN :statuses")
    List<ExamInvigilator> findByTeacherIdAndStatuses(
            @Param("teacherId") Long teacherId,
            @Param("statuses") List<ExamStatus> statuses);

    /**
     * 根据教师ID和学期ID查询监考任务
     *
     * @param teacherId  教师ID
     * @param semesterId 学期ID
     * @return 监考安排列表
     */
    @Query("SELECT DISTINCT ei FROM ExamInvigilator ei " +
           "LEFT JOIN FETCH ei.examRoom er " +
           "LEFT JOIN FETCH er.exam e " +
           "LEFT JOIN FETCH e.courseOffering co " +
           "LEFT JOIN FETCH co.course " +
           "LEFT JOIN FETCH co.semester " +
           "WHERE ei.teacher.id = :teacherId " +
           "AND co.semester.id = :semesterId")
    List<ExamInvigilator> findByTeacherIdAndSemesterId(
            @Param("teacherId") Long teacherId,
            @Param("semesterId") Long semesterId);

    /**
     * 检查教师在指定时间段是否有监考任务
     *
     * @param teacherId 教师ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return true-有监考，false-无监考
     */
    @Query("SELECT CASE WHEN COUNT(ei) > 0 THEN true ELSE false END " +
           "FROM ExamInvigilator ei " +
           "WHERE ei.teacher.id = :teacherId " +
           "AND ei.examRoom.exam.examTime BETWEEN :startTime AND :endTime " +
           "AND ei.examRoom.exam.status != 'CANCELLED'")
    boolean hasInvigilationInTimeRange(
            @Param("teacherId") Long teacherId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 检查教师在指定时间段是否有监考任务（排除指定考试）
     *
     * @param teacherId     教师ID
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param excludeExamId 排除的考试ID
     * @return true-有监考，false-无监考
     */
    @Query("SELECT CASE WHEN COUNT(ei) > 0 THEN true ELSE false END " +
           "FROM ExamInvigilator ei " +
           "WHERE ei.teacher.id = :teacherId " +
           "AND ei.examRoom.exam.examTime BETWEEN :startTime AND :endTime " +
           "AND ei.examRoom.exam.status != 'CANCELLED' " +
           "AND (:excludeExamId IS NULL OR ei.examRoom.exam.id != :excludeExamId)")
    boolean hasInvigilationInTimeRangeExcluding(
            @Param("teacherId") Long teacherId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeExamId") Long excludeExamId);

    /**
     * 根据考试ID查询所有监考安排
     *
     * @param examId 考试ID
     * @return 监考安排列表
     */
    @Query("SELECT ei FROM ExamInvigilator ei " +
           "WHERE ei.examRoom.exam.id = :examId")
    List<ExamInvigilator> findByExamId(@Param("examId") Long examId);

    /**
     * 统计教师的监考次数（指定学期）
     *
     * @param teacherId  教师ID
     * @param semesterId 学期ID
     * @return 监考次数
     */
    @Query("SELECT COUNT(ei) FROM ExamInvigilator ei " +
           "WHERE ei.teacher.id = :teacherId " +
           "AND ei.examRoom.exam.courseOffering.semester.id = :semesterId " +
           "AND ei.examRoom.exam.status != 'CANCELLED'")
    long countByTeacherIdAndSemesterId(
            @Param("teacherId") Long teacherId,
            @Param("semesterId") Long semesterId);

    /**
     * 统计考场的监考人数
     *
     * @param examRoomId 考场ID
     * @return 监考人数
     */
    long countByExamRoomId(Long examRoomId);

    /**
     * 根据考场ID和教师ID删除监考安排
     *
     * @param examRoomId 考场ID
     * @param teacherId  教师ID
     */
    @Modifying
    @Query("DELETE FROM ExamInvigilator ei WHERE ei.examRoom.id = :examRoomId AND ei.teacher.id = :teacherId")
    void deleteByExamRoomIdAndTeacherId(@Param("examRoomId") Long examRoomId, @Param("teacherId") Long teacherId);
}

