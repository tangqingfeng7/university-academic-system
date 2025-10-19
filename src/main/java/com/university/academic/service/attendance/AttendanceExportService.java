package com.university.academic.service.attendance;

import java.time.LocalDate;

/**
 * 考勤导出服务接口
 * 提供考勤数据导出和报表生成功能
 *
 * @author Academic System Team
 */
public interface AttendanceExportService {

    /**
     * 导出课程考勤Excel
     *
     * @param offeringId 开课计划ID
     * @return Excel文件字节数组
     */
    byte[] exportCourseAttendance(Long offeringId);

    /**
     * 导出院系考勤统计Excel
     *
     * @param departmentId 院系ID
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return Excel文件字节数组
     */
    byte[] exportDepartmentStatistics(Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * 导出教师考勤统计Excel
     *
     * @param teacherId 教师ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return Excel文件字节数组
     */
    byte[] exportTeacherStatistics(Long teacherId, LocalDate startDate, LocalDate endDate);

    /**
     * 生成月度考勤PDF报告
     *
     * @param departmentId 院系ID
     * @param year         年份
     * @param month        月份
     * @return PDF文件字节数组
     */
    byte[] generateMonthlyReport(Long departmentId, Integer year, Integer month);

    /**
     * 生成学期考勤PDF报告
     *
     * @param semesterId 学期ID
     * @return PDF文件字节数组
     */
    byte[] generateSemesterReport(Long semesterId);
}

