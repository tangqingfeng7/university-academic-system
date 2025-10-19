package com.university.academic.service.attendance;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 考勤统计服务接口
 * 提供考勤数据的统计和分析功能
 *
 * @author Academic System Team
 */
public interface AttendanceStatisticsService {

    /**
     * 获取课程考勤统计
     * 包含出勤率、迟到率、旷课率等统计指标
     *
     * @param offeringId 开课计划ID
     * @return 统计数据Map
     */
    Map<String, Object> getCourseStatistics(Long offeringId);

    /**
     * 获取学生个人考勤统计
     *
     * @param studentId  学生ID
     * @param offeringId 开课计划ID
     * @return 统计数据Map
     */
    Map<String, Object> getStudentStatistics(Long studentId, Long offeringId);

    /**
     * 获取学生在某学期的所有课程考勤统计
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 统计数据列表
     */
    List<Map<String, Object>> getStudentSemesterStatistics(Long studentId, Long semesterId);

    /**
     * 获取院系考勤统计
     *
     * @param departmentId 院系ID
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 统计数据Map
     */
    Map<String, Object> getDepartmentStatistics(Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取教师考勤统计
     *
     * @param teacherId 教师ID
     * @return 统计数据Map
     */
    Map<String, Object> getTeacherStatistics(Long teacherId);

    /**
     * 获取教师在指定时间范围内的考勤统计
     *
     * @param teacherId 教师ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计数据Map
     */
    Map<String, Object> getTeacherStatistics(Long teacherId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取课程出勤率趋势数据
     *
     * @param offeringId 开课计划ID
     * @return 趋势数据列表（日期、出勤率）
     */
    List<Map<String, Object>> getAttendanceTrend(Long offeringId);

    /**
     * 刷新统计缓存
     *
     * @param offeringId 开课计划ID
     */
    void refreshStatisticsCache(Long offeringId);
}

