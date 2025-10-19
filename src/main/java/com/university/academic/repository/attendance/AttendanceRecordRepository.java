package com.university.academic.repository.attendance;

import com.university.academic.entity.attendance.AttendanceRecord;
import com.university.academic.entity.attendance.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 考勤记录数据访问接口
 * 提供考勤记录的CRUD操作
 *
 * @author Academic System Team
 */
@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    /**
     * 根据ID查询考勤记录（预加载所有关联）
     *
     * @param id 考勤记录ID
     * @return 考勤记录对象
     */
    @Query("SELECT DISTINCT ar FROM AttendanceRecord ar " +
            "LEFT JOIN FETCH ar.offering o " +
            "LEFT JOIN FETCH o.course c " +
            "LEFT JOIN FETCH o.semester s " +
            "LEFT JOIN FETCH ar.teacher t " +
            "LEFT JOIN FETCH t.department " +
            "WHERE ar.id = :id")
    Optional<AttendanceRecord> findByIdWithDetails(@Param("id") Long id);

    /**
     * 查询开课计划的所有考勤记录
     *
     * @param offeringId 开课计划ID
     * @return 考勤记录列表
     */
    List<AttendanceRecord> findByOfferingId(Long offeringId);

    /**
     * 查询开课计划的所有考勤记录（带关联实体）
     *
     * @param offeringId 开课计划ID
     * @return 考勤记录列表
     */
    @Query("SELECT DISTINCT ar FROM AttendanceRecord ar " +
            "LEFT JOIN FETCH ar.offering o " +
            "LEFT JOIN FETCH o.course " +
            "LEFT JOIN FETCH ar.teacher " +
            "WHERE ar.offering.id = :offeringId " +
            "ORDER BY ar.attendanceDate DESC, ar.attendanceTime DESC")
    List<AttendanceRecord> findByOfferingIdWithDetails(@Param("offeringId") Long offeringId);

    /**
     * 查询教师的考勤记录
     *
     * @param teacherId 教师ID
     * @return 考勤记录列表
     */
    @Query("SELECT DISTINCT ar FROM AttendanceRecord ar " +
            "LEFT JOIN FETCH ar.offering o " +
            "LEFT JOIN FETCH o.course " +
            "LEFT JOIN FETCH o.semester " +
            "WHERE ar.teacher.id = :teacherId " +
            "ORDER BY ar.attendanceDate DESC, ar.attendanceTime DESC")
    List<AttendanceRecord> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 查询教师在指定日期范围内的考勤记录
     *
     * @param teacherId 教师ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 考勤记录列表
     */
    @Query("SELECT DISTINCT ar FROM AttendanceRecord ar " +
            "LEFT JOIN FETCH ar.offering o " +
            "LEFT JOIN FETCH o.course " +
            "WHERE ar.teacher.id = :teacherId " +
            "AND ar.attendanceDate BETWEEN :startDate AND :endDate " +
            "ORDER BY ar.attendanceDate DESC, ar.attendanceTime DESC")
    List<AttendanceRecord> findByTeacherIdAndDateRange(
            @Param("teacherId") Long teacherId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 查询开课计划在指定日期的考勤记录
     *
     * @param offeringId      开课计划ID
     * @param attendanceDate  考勤日期
     * @return 考勤记录列表
     */
    List<AttendanceRecord> findByOfferingIdAndAttendanceDate(Long offeringId, LocalDate attendanceDate);

    /**
     * 查询指定状态的考勤记录
     *
     * @param offeringId 开课计划ID
     * @param status     记录状态
     * @return 考勤记录列表
     */
    List<AttendanceRecord> findByOfferingIdAndStatus(Long offeringId, RecordStatus status);

    /**
     * 根据二维码令牌查询考勤记录
     *
     * @param qrToken 二维码令牌
     * @return 考勤记录对象
     */
    Optional<AttendanceRecord> findByQrToken(String qrToken);

    /**
     * 统计开课计划的考勤次数
     *
     * @param offeringId 开课计划ID
     * @return 考勤次数
     */
    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar " +
            "WHERE ar.offering.id = :offeringId " +
            "AND ar.status = 'SUBMITTED'")
    long countSubmittedByOfferingId(@Param("offeringId") Long offeringId);

    /**
     * 统计教师在指定日期范围内的考勤次数
     *
     * @param teacherId 教师ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 考勤次数
     */
    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar " +
            "WHERE ar.teacher.id = :teacherId " +
            "AND ar.attendanceDate BETWEEN :startDate AND :endDate " +
            "AND ar.status = 'SUBMITTED'")
    long countByTeacherIdAndDateRange(
            @Param("teacherId") Long teacherId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 检查开课计划在指定日期是否已有考勤记录
     *
     * @param offeringId     开课计划ID
     * @param attendanceDate 考勤日期
     * @param status         记录状态
     * @return true-存在，false-不存在
     */
    boolean existsByOfferingIdAndAttendanceDateAndStatus(
            Long offeringId, 
            LocalDate attendanceDate, 
            RecordStatus status);

    /**
     * 查询教师的进行中考勤记录
     *
     * @param teacherId 教师ID
     * @return 考勤记录列表
     */
    @Query("SELECT DISTINCT ar FROM AttendanceRecord ar " +
            "LEFT JOIN FETCH ar.offering o " +
            "LEFT JOIN FETCH o.course " +
            "WHERE ar.teacher.id = :teacherId " +
            "AND ar.status = 'IN_PROGRESS' " +
            "ORDER BY ar.attendanceDate DESC, ar.attendanceTime DESC")
    List<AttendanceRecord> findInProgressByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 查询指定院系在日期范围内的考勤记录
     *
     * @param departmentId 院系ID
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 考勤记录列表
     */
    @Query("SELECT DISTINCT ar FROM AttendanceRecord ar " +
            "LEFT JOIN FETCH ar.teacher t " +
            "LEFT JOIN FETCH t.department d " +
            "LEFT JOIN FETCH ar.offering o " +
            "LEFT JOIN FETCH o.course " +
            "WHERE d.id = :departmentId " +
            "AND ar.attendanceDate BETWEEN :startDate AND :endDate " +
            "AND ar.status = 'SUBMITTED' " +
            "ORDER BY ar.attendanceDate DESC, ar.attendanceTime DESC")
    List<AttendanceRecord> findByDepartmentIdAndDateRange(
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}

