package com.university.academic.repository.attendance;

import com.university.academic.entity.attendance.AttendanceRequest;
import com.university.academic.entity.attendance.RequestStatus;
import com.university.academic.entity.attendance.RequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 考勤申请数据访问接口
 *
 * @author Academic System Team
 */
@Repository
public interface AttendanceRequestRepository extends JpaRepository<AttendanceRequest, Long> {

    /**
     * 查询学生的所有申请（预加载关联）
     *
     * @param studentId 学生ID
     * @return 申请列表
     */
    @Query("SELECT DISTINCT ar FROM AttendanceRequest ar " +
            "LEFT JOIN FETCH ar.student s " +
            "LEFT JOIN FETCH ar.attendanceDetail ad " +
            "LEFT JOIN FETCH ad.attendanceRecord rec " +
            "LEFT JOIN FETCH rec.offering o " +
            "LEFT JOIN FETCH o.course " +
            "WHERE ar.student.id = :studentId " +
            "ORDER BY ar.createdAt DESC")
    List<AttendanceRequest> findByStudentIdWithDetails(@Param("studentId") Long studentId);

    /**
     * 查询学生指定状态的申请
     *
     * @param studentId 学生ID
     * @param status    申请状态
     * @return 申请列表
     */
    @Query("SELECT DISTINCT ar FROM AttendanceRequest ar " +
            "LEFT JOIN FETCH ar.student " +
            "LEFT JOIN FETCH ar.attendanceDetail ad " +
            "LEFT JOIN FETCH ad.attendanceRecord " +
            "WHERE ar.student.id = :studentId " +
            "AND ar.status = :status " +
            "ORDER BY ar.createdAt DESC")
    List<AttendanceRequest> findByStudentIdAndStatus(
            @Param("studentId") Long studentId,
            @Param("status") RequestStatus status);

    /**
     * 查询教师待审批的申请列表
     *
     * @param teacherId 教师ID
     * @return 申请列表
     */
    @Query("SELECT DISTINCT ar FROM AttendanceRequest ar " +
            "LEFT JOIN FETCH ar.student s " +
            "LEFT JOIN FETCH ar.attendanceDetail ad " +
            "LEFT JOIN FETCH ad.attendanceRecord rec " +
            "LEFT JOIN FETCH rec.offering o " +
            "LEFT JOIN FETCH o.course " +
            "WHERE rec.teacher.id = :teacherId " +
            "AND ar.status = 'PENDING' " +
            "ORDER BY ar.createdAt ASC")
    List<AttendanceRequest> findPendingByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 查询教师的所有申请（包括已处理）
     *
     * @param teacherId 教师ID
     * @return 申请列表
     */
    @Query("SELECT DISTINCT ar FROM AttendanceRequest ar " +
            "LEFT JOIN FETCH ar.student " +
            "LEFT JOIN FETCH ar.attendanceDetail ad " +
            "LEFT JOIN FETCH ad.attendanceRecord rec " +
            "WHERE rec.teacher.id = :teacherId " +
            "ORDER BY ar.createdAt DESC")
    List<AttendanceRequest> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 查询教师指定状态的申请
     *
     * @param teacherId 教师ID
     * @param status    申请状态
     * @return 申请列表
     */
    @Query("SELECT DISTINCT ar FROM AttendanceRequest ar " +
            "LEFT JOIN FETCH ar.student " +
            "LEFT JOIN FETCH ar.attendanceDetail ad " +
            "LEFT JOIN FETCH ad.attendanceRecord rec " +
            "WHERE rec.teacher.id = :teacherId " +
            "AND ar.status = :status " +
            "ORDER BY ar.createdAt DESC")
    List<AttendanceRequest> findByTeacherIdAndStatus(
            @Param("teacherId") Long teacherId,
            @Param("status") RequestStatus status);

    /**
     * 根据ID查询申请（预加载关联）
     *
     * @param id 申请ID
     * @return 申请对象
     */
    @Query("SELECT DISTINCT ar FROM AttendanceRequest ar " +
            "LEFT JOIN FETCH ar.student s " +
            "LEFT JOIN FETCH s.user " +
            "LEFT JOIN FETCH ar.attendanceDetail ad " +
            "LEFT JOIN FETCH ad.attendanceRecord rec " +
            "LEFT JOIN FETCH rec.offering o " +
            "LEFT JOIN FETCH o.course " +
            "LEFT JOIN FETCH rec.teacher t " +
            "LEFT JOIN FETCH t.user " +
            "WHERE ar.id = :id")
    Optional<AttendanceRequest> findByIdWithDetails(@Param("id") Long id);

    /**
     * 检查学生是否已对某条考勤明细提交过申请
     *
     * @param studentId 学生ID
     * @param detailId  考勤明细ID
     * @param status    申请状态（PENDING或APPROVED）
     * @return true-已存在，false-不存在
     */
    @Query("SELECT CASE WHEN COUNT(ar) > 0 THEN true ELSE false END " +
            "FROM AttendanceRequest ar " +
            "WHERE ar.student.id = :studentId " +
            "AND ar.attendanceDetail.id = :detailId " +
            "AND ar.status IN ('PENDING', 'APPROVED')")
    boolean existsByStudentAndDetail(
            @Param("studentId") Long studentId,
            @Param("detailId") Long detailId);

    /**
     * 统计教师待审批的申请数量
     *
     * @param teacherId 教师ID
     * @return 待审批数量
     */
    @Query("SELECT COUNT(ar) FROM AttendanceRequest ar " +
            "WHERE ar.attendanceDetail.attendanceRecord.teacher.id = :teacherId " +
            "AND ar.status = 'PENDING'")
    long countPendingByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 查询指定类型的待审批申请
     *
     * @param teacherId   教师ID
     * @param requestType 申请类型
     * @return 申请列表
     */
    @Query("SELECT DISTINCT ar FROM AttendanceRequest ar " +
            "LEFT JOIN FETCH ar.student " +
            "LEFT JOIN FETCH ar.attendanceDetail ad " +
            "WHERE ad.attendanceRecord.teacher.id = :teacherId " +
            "AND ar.requestType = :requestType " +
            "AND ar.status = 'PENDING' " +
            "ORDER BY ar.createdAt ASC")
    List<AttendanceRequest> findPendingByTeacherIdAndType(
            @Param("teacherId") Long teacherId,
            @Param("requestType") RequestType requestType);
}

