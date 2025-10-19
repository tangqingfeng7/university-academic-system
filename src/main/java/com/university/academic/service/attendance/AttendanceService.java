package com.university.academic.service.attendance;

import com.university.academic.entity.attendance.AttendanceDetail;
import com.university.academic.entity.attendance.AttendanceMethod;
import com.university.academic.entity.attendance.AttendanceRecord;
import com.university.academic.entity.attendance.AttendanceStatus;

import java.util.List;

/**
 * 考勤服务接口
 * 提供考勤记录的核心业务逻辑
 *
 * @author Academic System Team
 */
public interface AttendanceService {

    /**
     * 开始考勤
     * 创建考勤记录并初始化所有学生的考勤明细
     *
     * @param offeringId 开课计划ID
     * @param method     考勤方式
     * @return 考勤记录
     */
    AttendanceRecord startAttendance(Long offeringId, AttendanceMethod method);

    /**
     * 记录学生考勤状态（手动点名）
     *
     * @param recordId  考勤记录ID
     * @param studentId 学生ID
     * @param status    考勤状态
     * @param remarks   备注
     * @return 考勤明细
     */
    AttendanceDetail recordAttendance(Long recordId, Long studentId, AttendanceStatus status, String remarks);

    /**
     * 批量记录学生考勤状态
     *
     * @param recordId   考勤记录ID
     * @param studentIds 学生ID列表
     * @param status     考勤状态
     * @return 更新数量
     */
    int batchRecordAttendance(Long recordId, List<Long> studentIds, AttendanceStatus status);

    /**
     * 提交考勤
     * 计算统计数据并更新考勤记录状态
     *
     * @param recordId 考勤记录ID
     * @return 考勤记录
     */
    AttendanceRecord submitAttendance(Long recordId);

    /**
     * 取消考勤
     *
     * @param recordId 考勤记录ID
     */
    void cancelAttendance(Long recordId);

    /**
     * 更新考勤明细状态
     *
     * @param detailId     考勤明细ID
     * @param status       新状态
     * @param modifyReason 修改原因
     * @return 考勤明细
     */
    AttendanceDetail updateAttendanceStatus(Long detailId, AttendanceStatus status, String modifyReason);

    /**
     * 获取考勤记录详情
     *
     * @param recordId 考勤记录ID
     * @return 考勤记录
     */
    AttendanceRecord getAttendanceRecord(Long recordId);

    /**
     * 获取考勤记录的所有明细
     *
     * @param recordId 考勤记录ID
     * @return 考勤明细列表
     */
    List<AttendanceDetail> getAttendanceDetails(Long recordId);

    /**
     * 获取学生在某课程的考勤列表
     *
     * @param studentId  学生ID
     * @param offeringId 开课计划ID
     * @return 考勤明细列表
     */
    List<AttendanceDetail> getStudentAttendanceList(Long studentId, Long offeringId);

    /**
     * 获取教师的考勤记录列表
     *
     * @param teacherId 教师ID
     * @return 考勤记录列表
     */
    List<AttendanceRecord> getTeacherAttendanceList(Long teacherId);

    /**
     * 获取开课计划的考勤记录列表
     *
     * @param offeringId 开课计划ID
     * @return 考勤记录列表
     */
    List<AttendanceRecord> getOfferingAttendanceList(Long offeringId);
}

