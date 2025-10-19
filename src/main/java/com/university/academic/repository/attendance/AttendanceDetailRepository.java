package com.university.academic.repository.attendance;

import com.university.academic.entity.attendance.AttendanceDetail;
import com.university.academic.entity.attendance.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 考勤明细数据访问接口
 * 提供考勤明细的CRUD操作
 *
 * @author Academic System Team
 */
@Repository
public interface AttendanceDetailRepository extends JpaRepository<AttendanceDetail, Long> {

    /**
     * 查询考勤记录的所有明细
     *
     * @param recordId 考勤记录ID
     * @return 考勤明细列表
     */
    List<AttendanceDetail> findByAttendanceRecordId(Long recordId);

    /**
     * 查询考勤记录的所有明细（预加载学生信息）
     *
     * @param recordId 考勤记录ID
     * @return 考勤明细列表
     */
    @Query("SELECT DISTINCT ad FROM AttendanceDetail ad " +
            "LEFT JOIN FETCH ad.student s " +
            "LEFT JOIN FETCH s.user " +
            "LEFT JOIN FETCH s.major m " +
            "LEFT JOIN FETCH m.department " +
            "WHERE ad.attendanceRecord.id = :recordId " +
            "ORDER BY s.studentNo")
    List<AttendanceDetail> findByAttendanceRecordIdWithStudent(@Param("recordId") Long recordId);

    /**
     * 查询学生在指定考勤记录中的明细
     *
     * @param recordId  考勤记录ID
     * @param studentId 学生ID
     * @return 考勤明细对象
     */
    Optional<AttendanceDetail> findByAttendanceRecordIdAndStudentId(Long recordId, Long studentId);

    /**
     * 查询学生在指定开课计划的所有考勤明细
     *
     * @param studentId  学生ID
     * @param offeringId 开课计划ID
     * @return 考勤明细列表
     */
    @Query("SELECT DISTINCT ad FROM AttendanceDetail ad " +
            "LEFT JOIN FETCH ad.attendanceRecord ar " +
            "LEFT JOIN FETCH ar.offering o " +
            "LEFT JOIN FETCH o.course c " +
            "LEFT JOIN FETCH ar.teacher t " +
            "WHERE ad.student.id = :studentId " +
            "AND ar.offering.id = :offeringId " +
            "ORDER BY ar.attendanceDate DESC, ar.attendanceTime DESC")
    List<AttendanceDetail> findByStudentIdAndOfferingId(
            @Param("studentId") Long studentId,
            @Param("offeringId") Long offeringId);

    /**
     * 查询学生的所有考勤明细
     *
     * @param studentId 学生ID
     * @return 考勤明细列表
     */
    @Query("SELECT DISTINCT ad FROM AttendanceDetail ad " +
            "LEFT JOIN FETCH ad.attendanceRecord ar " +
            "LEFT JOIN FETCH ar.offering o " +
            "LEFT JOIN FETCH o.course " +
            "LEFT JOIN FETCH o.semester " +
            "WHERE ad.student.id = :studentId " +
            "ORDER BY ar.attendanceDate DESC, ar.attendanceTime DESC")
    List<AttendanceDetail> findByStudentIdWithDetails(@Param("studentId") Long studentId);

    /**
     * 统计考勤记录中指定状态的人数
     *
     * @param recordId 考勤记录ID
     * @param status   考勤状态
     * @return 人数
     */
    @Query("SELECT COUNT(ad) FROM AttendanceDetail ad " +
            "WHERE ad.attendanceRecord.id = :recordId " +
            "AND ad.status = :status")
    long countByAttendanceRecordIdAndStatus(
            @Param("recordId") Long recordId,
            @Param("status") AttendanceStatus status);

    /**
     * 批量更新考勤状态
     *
     * @param detailIds 考勤明细ID列表
     * @param status    新的考勤状态
     */
    @Modifying
    @Query("UPDATE AttendanceDetail ad SET ad.status = :status " +
            "WHERE ad.id IN :detailIds")
    void updateStatusBatch(@Param("detailIds") List<Long> detailIds,
                          @Param("status") AttendanceStatus status);

    /**
     * 批量更新考勤状态和备注
     *
     * @param detailIds    考勤明细ID列表
     * @param status       新的考勤状态
     * @param modifiedBy   修改人ID
     * @param modifyReason 修改原因
     */
    @Modifying
    @Query("UPDATE AttendanceDetail ad " +
            "SET ad.status = :status, " +
            "ad.modifiedBy = :modifiedBy, " +
            "ad.modifyReason = :modifyReason " +
            "WHERE ad.id IN :detailIds")
    void updateStatusWithReasonBatch(
            @Param("detailIds") List<Long> detailIds,
            @Param("status") AttendanceStatus status,
            @Param("modifiedBy") Long modifiedBy,
            @Param("modifyReason") String modifyReason);

    /**
     * 统计学生在指定开课计划中的各类考勤次数
     *
     * @param studentId  学生ID
     * @param offeringId 开课计划ID
     * @param status     考勤状态
     * @return 次数
     */
    @Query("SELECT COUNT(ad) FROM AttendanceDetail ad " +
            "WHERE ad.student.id = :studentId " +
            "AND ad.attendanceRecord.offering.id = :offeringId " +
            "AND ad.status = :status")
    long countByStudentIdAndOfferingIdAndStatus(
            @Param("studentId") Long studentId,
            @Param("offeringId") Long offeringId,
            @Param("status") AttendanceStatus status);

    /**
     * 检查学生是否已签到
     *
     * @param recordId  考勤记录ID
     * @param studentId 学生ID
     * @return true-已签到，false-未签到
     */
    @Query("SELECT CASE WHEN COUNT(ad) > 0 THEN true ELSE false END " +
            "FROM AttendanceDetail ad " +
            "WHERE ad.attendanceRecord.id = :recordId " +
            "AND ad.student.id = :studentId " +
            "AND ad.checkinTime IS NOT NULL")
    boolean hasCheckedIn(@Param("recordId") Long recordId,
                        @Param("studentId") Long studentId);

    /**
     * 删除考勤记录的所有明细
     *
     * @param recordId 考勤记录ID
     */
    @Modifying
    @Query("DELETE FROM AttendanceDetail ad WHERE ad.attendanceRecord.id = :recordId")
    void deleteByAttendanceRecordId(@Param("recordId") Long recordId);
}

