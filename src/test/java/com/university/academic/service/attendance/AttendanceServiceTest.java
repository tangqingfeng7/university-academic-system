package com.university.academic.service.attendance;

import com.university.academic.entity.attendance.*;
import com.university.academic.exception.attendance.AttendanceAlreadySubmittedException;
import com.university.academic.exception.attendance.AttendanceNotFoundException;
import com.university.academic.repository.attendance.AttendanceStatisticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 考勤服务集成测试
 *
 * @author Academic System Team
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("考勤服务集成测试")
class AttendanceServiceTest {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private AttendanceStatisticsRepository statisticsRepository;

    private Long testOfferingId;

    @BeforeEach
    void setUp() {
        // 使用现有的测试数据
        testOfferingId = 1L;
    }

    @Test
    @DisplayName("测试手动点名流程")
    void testManualAttendanceFlow() {
        // 1. 开始考勤
        AttendanceRecord record = attendanceService.startAttendance(testOfferingId, AttendanceMethod.MANUAL);

        assertThat(record).isNotNull();
        assertThat(record.getId()).isNotNull();
        assertThat(record.getMethod()).isEqualTo(AttendanceMethod.MANUAL);
        assertThat(record.getStatus()).isEqualTo(RecordStatus.IN_PROGRESS);
        assertThat(record.getTotalStudents()).isGreaterThan(0);

        // 2. 记录考勤
        List<AttendanceDetail> details = attendanceService.getAttendanceDetails(record.getId());
        assertThat(details).isNotEmpty();

        AttendanceDetail firstDetail = details.get(0);
        AttendanceDetail updated = attendanceService.recordAttendance(
                record.getId(),
                firstDetail.getStudent().getId(),
                AttendanceStatus.PRESENT,
                "测试出勤"
        );

        assertThat(updated.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(updated.getRemarks()).isEqualTo("测试出勤");

        // 3. 提交考勤
        AttendanceRecord submitted = attendanceService.submitAttendance(record.getId());

        assertThat(submitted.getStatus()).isEqualTo(RecordStatus.SUBMITTED);
        assertThat(submitted.getAttendanceRate()).isNotNull();
        assertThat(submitted.getAttendanceRate()).isGreaterThanOrEqualTo(0.0);
    }

    @Test
    @DisplayName("测试批量记录考勤")
    void testBatchRecordAttendance() {
        // 开始考勤
        AttendanceRecord record = attendanceService.startAttendance(testOfferingId, AttendanceMethod.MANUAL);

        // 获取所有明细
        List<AttendanceDetail> details = attendanceService.getAttendanceDetails(record.getId());
        List<Long> studentIds = details.stream()
                .limit(5)
                .map(d -> d.getStudent().getId())
                .toList();

        // 批量标记为出勤
        int count = attendanceService.batchRecordAttendance(
                record.getId(),
                studentIds,
                AttendanceStatus.PRESENT
        );

        assertThat(count).isEqualTo(studentIds.size());

        // 验证状态已更新
        List<AttendanceDetail> updated = attendanceService.getAttendanceDetails(record.getId());
        long presentCount = updated.stream()
                .filter(d -> d.getStatus() == AttendanceStatus.PRESENT)
                .count();

        assertThat(presentCount).isGreaterThanOrEqualTo(studentIds.size());
    }

    @Test
    @DisplayName("测试考勤统计数据更新")
    void testStatisticsUpdate() {
        // 开始考勤
        AttendanceRecord record = attendanceService.startAttendance(testOfferingId, AttendanceMethod.MANUAL);

        // 记录一些考勤
        List<AttendanceDetail> details = attendanceService.getAttendanceDetails(record.getId());
        if (!details.isEmpty()) {
            attendanceService.recordAttendance(
                    record.getId(),
                    details.get(0).getStudent().getId(),
                    AttendanceStatus.PRESENT,
                    null
            );
        }

        // 提交考勤
        attendanceService.submitAttendance(record.getId());

        // 验证统计数据已创建或更新
        if (!details.isEmpty()) {
            AttendanceStatistics stats = statisticsRepository
                    .findByStudentIdAndOfferingId(
                            details.get(0).getStudent().getId(),
                            testOfferingId
                    )
                    .orElse(null);

            assertThat(stats).isNotNull();
            assertThat(stats.getTotalClasses()).isGreaterThan(0);
            assertThat(stats.getAttendanceRate()).isNotNull();
        }
    }

    @Test
    @DisplayName("测试已提交考勤无法修改")
    void testCannotModifySubmittedAttendance() {
        // 开始并提交考勤
        AttendanceRecord record = attendanceService.startAttendance(testOfferingId, AttendanceMethod.MANUAL);
        attendanceService.submitAttendance(record.getId());

        // 尝试修改应该抛出异常
        List<AttendanceDetail> details = attendanceService.getAttendanceDetails(record.getId());
        if (!details.isEmpty()) {
            assertThatThrownBy(() ->
                    attendanceService.recordAttendance(
                            record.getId(),
                            details.get(0).getStudent().getId(),
                            AttendanceStatus.LATE,
                            null
                    )
            ).isInstanceOf(AttendanceAlreadySubmittedException.class);
        }
    }

    @Test
    @DisplayName("测试取消考勤")
    void testCancelAttendance() {
        // 开始考勤
        AttendanceRecord record = attendanceService.startAttendance(testOfferingId, AttendanceMethod.MANUAL);
        assertThat(record.getStatus()).isEqualTo(RecordStatus.IN_PROGRESS);

        // 取消考勤
        attendanceService.cancelAttendance(record.getId());

        // 验证状态已更新
        AttendanceRecord cancelled = attendanceService.getAttendanceRecord(record.getId());
        assertThat(cancelled.getStatus()).isEqualTo(RecordStatus.CANCELLED);
    }

    @Test
    @DisplayName("测试查询不存在的考勤记录")
    void testGetNonExistentRecord() {
        assertThatThrownBy(() ->
                attendanceService.getAttendanceRecord(999999L)
        ).isInstanceOf(AttendanceNotFoundException.class);
    }
}

