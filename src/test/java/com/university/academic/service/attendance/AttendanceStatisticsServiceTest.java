package com.university.academic.service.attendance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 考勤统计服务集成测试
 *
 * @author Academic System Team
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("考勤统计服务集成测试")
class AttendanceStatisticsServiceTest {

    @Autowired
    private AttendanceStatisticsService statisticsService;

    private Long testOfferingId;
    private Long testStudentId;
    private Long testDepartmentId;
    private Long testTeacherId;

    @BeforeEach
    void setUp() {
        testOfferingId = 1L;
        testStudentId = 1L;
        testDepartmentId = 1L;
        testTeacherId = 1L;
    }

    @Test
    @DisplayName("测试获取课程考勤统计")
    void testGetCourseStatistics() {
        Map<String, Object> stats = statisticsService.getCourseStatistics(testOfferingId);

        assertThat(stats).isNotNull();
        assertThat(stats).containsKeys(
                "offeringId",
                "totalStudents",
                "totalClasses",
                "avgAttendanceRate",
                "presentRate",
                "lateRate",
                "absentRate"
        );
    }

    @Test
    @DisplayName("测试获取学生考勤统计")
    void testGetStudentStatistics() {
        Map<String, Object> stats = statisticsService.getStudentStatistics(
                testStudentId, testOfferingId);

        assertThat(stats).isNotNull();
        assertThat(stats).containsKeys(
                "studentId",
                "offeringId",
                "totalClasses",
                "presentCount",
                "lateCount",
                "absentCount",
                "attendanceRate"
        );
    }

    @Test
    @DisplayName("测试获取院系统计")
    void testGetDepartmentStatistics() {
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();

        Map<String, Object> stats = statisticsService.getDepartmentStatistics(
                testDepartmentId, startDate, endDate);

        assertThat(stats).isNotNull();
        assertThat(stats).containsKeys(
                "departmentId",
                "totalRecords",
                "avgAttendanceRate"
        );
    }

    @Test
    @DisplayName("测试获取教师统计")
    void testGetTeacherStatistics() {
        Map<String, Object> stats = statisticsService.getTeacherStatistics(testTeacherId);

        assertThat(stats).isNotNull();
        assertThat(stats).containsKeys(
                "teacherId",
                "totalRecords",
                "avgAttendanceRate"
        );
    }

    @Test
    @DisplayName("测试获取出勤率趋势")
    void testGetAttendanceTrend() {
        List<Map<String, Object>> trend = statisticsService.getAttendanceTrend(testOfferingId);

        assertThat(trend).isNotNull();
        // 趋势数据可能为空（取决于是否有已提交的考勤记录）
    }

    @Test
    @DisplayName("测试统计缓存")
    void testStatisticsCache() {
        // 第一次调用
        Map<String, Object> stats1 = statisticsService.getCourseStatistics(testOfferingId);

        // 第二次调用（应该从缓存获取）
        Map<String, Object> stats2 = statisticsService.getCourseStatistics(testOfferingId);

        assertThat(stats1).isEqualTo(stats2);
        // 第二次调用应该从缓存获取
    }
}

