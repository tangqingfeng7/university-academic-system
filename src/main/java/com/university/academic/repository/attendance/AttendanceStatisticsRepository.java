package com.university.academic.repository.attendance;

import com.university.academic.entity.attendance.AttendanceStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 考勤统计数据访问接口
 * 提供考勤统计数据的CRUD操作
 *
 * @author Academic System Team
 */
@Repository
public interface AttendanceStatisticsRepository extends JpaRepository<AttendanceStatistics, Long> {

    /**
     * 查询学生在指定开课计划的统计数据
     *
     * @param studentId  学生ID
     * @param offeringId 开课计划ID
     * @return 统计数据对象
     */
    Optional<AttendanceStatistics> findByStudentIdAndOfferingId(Long studentId, Long offeringId);

    /**
     * 查询学生在指定开课计划的统计数据（预加载关联实体）
     *
     * @param studentId  学生ID
     * @param offeringId 开课计划ID
     * @return 统计数据对象
     */
    @Query("SELECT DISTINCT ats FROM AttendanceStatistics ats " +
            "LEFT JOIN FETCH ats.student s " +
            "LEFT JOIN FETCH ats.offering o " +
            "LEFT JOIN FETCH o.course " +
            "WHERE ats.student.id = :studentId " +
            "AND ats.offering.id = :offeringId")
    Optional<AttendanceStatistics> findByStudentIdAndOfferingIdWithDetails(
            @Param("studentId") Long studentId,
            @Param("offeringId") Long offeringId);

    /**
     * 查询学生的所有考勤统计
     *
     * @param studentId 学生ID
     * @return 统计数据列表
     */
    @Query("SELECT DISTINCT ats FROM AttendanceStatistics ats " +
            "LEFT JOIN FETCH ats.offering o " +
            "LEFT JOIN FETCH o.course c " +
            "LEFT JOIN FETCH o.semester s " +
            "WHERE ats.student.id = :studentId " +
            "ORDER BY s.startDate DESC")
    List<AttendanceStatistics> findByStudentIdWithDetails(@Param("studentId") Long studentId);

    /**
     * 查询开课计划的所有学生统计数据
     *
     * @param offeringId 开课计划ID
     * @return 统计数据列表
     */
    @Query("SELECT DISTINCT ats FROM AttendanceStatistics ats " +
            "LEFT JOIN FETCH ats.student s " +
            "LEFT JOIN FETCH s.major " +
            "WHERE ats.offering.id = :offeringId " +
            "ORDER BY s.studentNo")
    List<AttendanceStatistics> findByOfferingIdWithStudent(@Param("offeringId") Long offeringId);

    /**
     * 查询出勤率低于指定值的学生统计
     *
     * @param offeringId      开课计划ID
     * @param attendanceRate  出勤率阈值
     * @return 统计数据列表
     */
    @Query("SELECT DISTINCT ats FROM AttendanceStatistics ats " +
            "LEFT JOIN FETCH ats.student s " +
            "WHERE ats.offering.id = :offeringId " +
            "AND ats.attendanceRate < :attendanceRate " +
            "ORDER BY ats.attendanceRate ASC")
    List<AttendanceStatistics> findLowAttendanceByOffering(
            @Param("offeringId") Long offeringId,
            @Param("attendanceRate") Double attendanceRate);

    /**
     * 查询旷课次数超过指定值的学生统计
     *
     * @param offeringId   开课计划ID
     * @param absentCount  旷课次数阈值
     * @return 统计数据列表
     */
    @Query("SELECT DISTINCT ats FROM AttendanceStatistics ats " +
            "LEFT JOIN FETCH ats.student s " +
            "WHERE ats.offering.id = :offeringId " +
            "AND ats.absentCount >= :absentCount " +
            "ORDER BY ats.absentCount DESC")
    List<AttendanceStatistics> findHighAbsentByOffering(
            @Param("offeringId") Long offeringId,
            @Param("absentCount") Integer absentCount);

    /**
     * 批量更新总课次
     *
     * @param offeringId 开课计划ID
     */
    @Modifying
    @Query("UPDATE AttendanceStatistics ats " +
            "SET ats.totalClasses = ats.totalClasses + 1 " +
            "WHERE ats.offering.id = :offeringId")
    void incrementTotalClassesByOffering(@Param("offeringId") Long offeringId);

    /**
     * 批量更新出勤率
     *
     * @param offeringId 开课计划ID
     */
    @Modifying
    @Query("UPDATE AttendanceStatistics ats " +
            "SET ats.attendanceRate = " +
            "CASE WHEN ats.totalClasses > 0 " +
            "THEN (ats.presentCount + ats.lateCount) * 1.0 / ats.totalClasses " +
            "ELSE 0.0 END " +
            "WHERE ats.offering.id = :offeringId")
    void updateAttendanceRateByOffering(@Param("offeringId") Long offeringId);

    /**
     * 统计开课计划的平均出勤率
     *
     * @param offeringId 开课计划ID
     * @return 平均出勤率
     */
    @Query("SELECT AVG(ats.attendanceRate) FROM AttendanceStatistics ats " +
            "WHERE ats.offering.id = :offeringId")
    Double getAverageAttendanceRateByOffering(@Param("offeringId") Long offeringId);

    /**
     * 统计开课计划中出勤率低于阈值的学生数量
     *
     * @param offeringId     开课计划ID
     * @param attendanceRate 出勤率阈值
     * @return 学生数量
     */
    @Query("SELECT COUNT(ats) FROM AttendanceStatistics ats " +
            "WHERE ats.offering.id = :offeringId " +
            "AND ats.attendanceRate < :attendanceRate")
    long countLowAttendanceByOffering(
            @Param("offeringId") Long offeringId,
            @Param("attendanceRate") Double attendanceRate);

    /**
     * 检查统计记录是否存在
     *
     * @param studentId  学生ID
     * @param offeringId 开课计划ID
     * @return true-存在，false-不存在
     */
    boolean existsByStudentIdAndOfferingId(Long studentId, Long offeringId);

    /**
     * 删除开课计划的所有统计数据
     *
     * @param offeringId 开课计划ID
     */
    @Modifying
    @Query("DELETE FROM AttendanceStatistics ats WHERE ats.offering.id = :offeringId")
    void deleteByOfferingId(@Param("offeringId") Long offeringId);

    /**
     * 查询学生在指定学期的所有统计数据
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 统计数据列表
     */
    @Query("SELECT DISTINCT ats FROM AttendanceStatistics ats " +
            "LEFT JOIN FETCH ats.offering o " +
            "LEFT JOIN FETCH o.course " +
            "WHERE ats.student.id = :studentId " +
            "AND o.semester.id = :semesterId")
    List<AttendanceStatistics> findByStudentIdAndSemesterId(
            @Param("studentId") Long studentId,
            @Param("semesterId") Long semesterId);

    /**
     * 查询所有统计数据（预加载关联实体）
     * 用于预警检测等批量操作
     *
     * @return 统计数据列表
     */
    @Query("SELECT DISTINCT ats FROM AttendanceStatistics ats " +
            "LEFT JOIN FETCH ats.student s " +
            "LEFT JOIN FETCH ats.offering o " +
            "LEFT JOIN FETCH o.course")
    List<AttendanceStatistics> findAllWithDetails();
}

