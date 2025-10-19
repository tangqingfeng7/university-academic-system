package com.university.academic.service.attendance;

import com.university.academic.entity.attendance.AttendanceWarning;
import com.university.academic.entity.attendance.WarningStatus;

import java.util.List;

/**
 * 考勤预警服务接口
 * 提供考勤异常检测和预警功能
 *
 * @author Academic System Team
 */
public interface AttendanceWarningService {

    /**
     * 检查学生旷课预警
     * 当学生旷课次数达到阈值时创建预警
     *
     * @param studentId  学生ID
     * @param offeringId 开课计划ID
     */
    void checkAbsenceWarning(Long studentId, Long offeringId);

    /**
     * 检查课程出勤率预警
     * 当课程整体出勤率低于阈值时创建预警
     *
     * @param offeringId 开课计划ID
     */
    void checkCourseAttendanceRate(Long offeringId);

    /**
     * 检查教师考勤完成情况
     * 当教师连续未考勤时创建提醒
     *
     * @param teacherId 教师ID
     */
    void checkTeacherAttendanceCompletion(Long teacherId);

    /**
     * 检测考勤异常
     * 如签到人数超过选课人数等异常情况
     *
     * @param recordId 考勤记录ID
     */
    void checkAttendanceAnomalies(Long recordId);

    /**
     * 发送预警通知
     *
     * @param warning 预警记录
     */
    void sendWarningNotification(AttendanceWarning warning);

    /**
     * 获取预警列表
     *
     * @param status 预警状态（null表示全部）
     * @return 预警列表
     */
    List<AttendanceWarning> getWarningList(WarningStatus status);

    /**
     * 处理预警
     *
     * @param warningId     预警ID
     * @param handleComment 处理意见
     */
    void handleWarning(Long warningId, String handleComment);

    /**
     * 忽略预警
     *
     * @param warningId 预警ID
     */
    void ignoreWarning(Long warningId);

    /**
     * 执行全部预警检测（定时任务调用）
     */
    void executeAllWarningChecks();
}

