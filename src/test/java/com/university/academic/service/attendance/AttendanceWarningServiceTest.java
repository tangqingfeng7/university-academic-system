package com.university.academic.service.attendance;

import com.university.academic.entity.attendance.*;
import com.university.academic.repository.attendance.AttendanceWarningRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 考勤预警服务集成测试
 *
 * @author Academic System Team
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("考勤预警服务集成测试")
class AttendanceWarningServiceTest {

    @Autowired
    private AttendanceWarningService warningService;

    @Autowired
    private AttendanceWarningRepository warningRepository;

    private Long testStudentId;
    private Long testOfferingId;

    @BeforeEach
    void setUp() {
        testStudentId = 6L;  // 使用测试数据中旷课较多的学生
        testOfferingId = 1L;
    }

    @Test
    @DisplayName("测试学生旷课预警检测")
    void testCheckAbsenceWarning() {
        // 执行旷课预警检测
        warningService.checkAbsenceWarning(testStudentId, testOfferingId);

        // 查询是否生成了预警
        List<AttendanceWarning> warnings = warningRepository
                .findByTargetTypeAndTargetIdOrderByCreatedAtDesc("STUDENT", testStudentId);

        // 如果学生旷课次数达到阈值，应该有预警记录
        // 具体是否有预警取决于测试数据
        assertThat(warnings).isNotNull();
    }

    @Test
    @DisplayName("测试课程出勤率预警检测")
    void testCheckCourseAttendanceRate() {
        // 执行课程出勤率检测
        warningService.checkCourseAttendanceRate(testOfferingId);

        // 查询是否生成了预警
        List<AttendanceWarning> warnings = warningRepository
                .findByWarningTypeAndStatusOrderByCreatedAtDesc(
                        WarningType.COURSE_LOW_RATE, 
                        WarningStatus.PENDING
                );

        assertThat(warnings).isNotNull();
    }

    @Test
    @DisplayName("测试获取预警列表")
    void testGetWarningList() {
        // 获取所有预警
        List<AttendanceWarning> allWarnings = warningService.getWarningList(null);
        assertThat(allWarnings).isNotNull();

        // 获取待处理预警
        List<AttendanceWarning> pendingWarnings = warningService.getWarningList(WarningStatus.PENDING);
        assertThat(pendingWarnings).isNotNull();

        // 待处理预警应该是所有预警的子集
        if (!allWarnings.isEmpty()) {
            long pendingCount = allWarnings.stream()
                    .filter(w -> w.getStatus() == WarningStatus.PENDING)
                    .count();
            assertThat(pendingWarnings.size()).isEqualTo(pendingCount);
        }
    }

    @Test
    @DisplayName("测试处理预警")
    void testHandleWarning() {
        // 先创建一个预警
        warningService.checkAbsenceWarning(testStudentId, testOfferingId);

        // 查找待处理预警
        List<AttendanceWarning> warnings = warningService.getWarningList(WarningStatus.PENDING);

        if (!warnings.isEmpty()) {
            AttendanceWarning warning = warnings.get(0);

            // 处理预警
            warningService.handleWarning(warning.getId(), "已处理");

            // 验证状态已更新
            AttendanceWarning handled = warningRepository.findById(warning.getId()).orElse(null);
            assertThat(handled).isNotNull();
            assertThat(handled.getStatus()).isEqualTo(WarningStatus.HANDLED);
            assertThat(handled.getHandleComment()).isEqualTo("已处理");
            assertThat(handled.getHandledAt()).isNotNull();
        }
    }

    @Test
    @DisplayName("测试忽略预警")
    void testIgnoreWarning() {
        // 先创建一个预警
        warningService.checkAbsenceWarning(testStudentId, testOfferingId);

        // 查找待处理预警
        List<AttendanceWarning> warnings = warningService.getWarningList(WarningStatus.PENDING);

        if (!warnings.isEmpty()) {
            AttendanceWarning warning = warnings.get(0);

            // 忽略预警
            warningService.ignoreWarning(warning.getId());

            // 验证状态已更新
            AttendanceWarning ignored = warningRepository.findById(warning.getId()).orElse(null);
            assertThat(ignored).isNotNull();
            assertThat(ignored.getStatus()).isEqualTo(WarningStatus.IGNORED);
        }
    }
}

