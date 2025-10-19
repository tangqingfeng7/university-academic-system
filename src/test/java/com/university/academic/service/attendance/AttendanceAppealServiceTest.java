package com.university.academic.service.attendance;

import com.university.academic.entity.attendance.*;
import com.university.academic.exception.attendance.AttendanceException;
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
 * 考勤申请服务集成测试
 *
 * @author Academic System Team
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("考勤申请服务集成测试")
class AttendanceAppealServiceTest {

    @Autowired
    private AttendanceAppealService appealService;

    @Autowired
    private AttendanceService attendanceService;

    private Long testStudentId;
    private Long testOfferingId;

    @BeforeEach
    void setUp() {
        testStudentId = 1L;
        testOfferingId = 1L;
    }

    @Test
    @DisplayName("测试提交补签申请")
    void testSubmitMakeupRequest() {
        // 先创建一次考勤
        AttendanceRecord record = attendanceService.startAttendance(
                testOfferingId, AttendanceMethod.MANUAL);
        attendanceService.submitAttendance(record.getId());

        // 获取考勤明细
        List<AttendanceDetail> details = attendanceService.getAttendanceDetails(record.getId());
        if (!details.isEmpty()) {
            AttendanceDetail detail = details.get(0);

            // 提交补签申请
            AttendanceRequest request = appealService.submitMakeupRequest(
                    testStudentId,
                    detail.getId(),
                    "测试补签原因",
                    null
            );

            assertThat(request).isNotNull();
            assertThat(request.getRequestType()).isEqualTo(RequestType.MAKEUP);
            assertThat(request.getStatus()).isEqualTo(RequestStatus.PENDING);
            assertThat(request.getReason()).isEqualTo("测试补签原因");
        }
    }

    @Test
    @DisplayName("测试提交考勤申诉")
    void testSubmitAppeal() {
        // 先创建一次考勤
        AttendanceRecord record = attendanceService.startAttendance(
                testOfferingId, AttendanceMethod.MANUAL);
        attendanceService.submitAttendance(record.getId());

        // 获取考勤明细
        List<AttendanceDetail> details = attendanceService.getAttendanceDetails(record.getId());
        if (!details.isEmpty()) {
            AttendanceDetail detail = details.get(0);

            // 提交申诉
            AttendanceRequest request = appealService.submitAppeal(
                    testStudentId,
                    detail.getId(),
                    "测试申诉原因",
                    null
            );

            assertThat(request).isNotNull();
            assertThat(request.getRequestType()).isEqualTo(RequestType.APPEAL);
            assertThat(request.getStatus()).isEqualTo(RequestStatus.PENDING);
        }
    }

    @Test
    @DisplayName("测试重复申请检测")
    void testDuplicateRequest() {
        // 先创建一次考勤
        AttendanceRecord record = attendanceService.startAttendance(
                testOfferingId, AttendanceMethod.MANUAL);
        attendanceService.submitAttendance(record.getId());

        List<AttendanceDetail> details = attendanceService.getAttendanceDetails(record.getId());
        if (!details.isEmpty()) {
            AttendanceDetail detail = details.get(0);

            // 第一次提交
            appealService.submitMakeupRequest(testStudentId, detail.getId(), "原因1", null);

            // 第二次提交应该抛出异常
            assertThatThrownBy(() ->
                    appealService.submitMakeupRequest(testStudentId, detail.getId(), "原因2", null)
            ).isInstanceOf(AttendanceException.class);
        }
    }

    @Test
    @DisplayName("测试获取学生申请列表")
    void testGetStudentRequests() {
        List<AttendanceRequest> requests = appealService.getStudentRequests(testStudentId);
        assertThat(requests).isNotNull();

        // 按状态查询
        List<AttendanceRequest> pending = appealService.getStudentRequests(
                testStudentId, RequestStatus.PENDING);
        assertThat(pending).isNotNull();
    }

    @Test
    @DisplayName("测试获取教师待审批申请")
    void testGetPendingRequests() {
        Long teacherId = 1L;
        List<AttendanceRequest> requests = appealService.getPendingRequests(teacherId);
        assertThat(requests).isNotNull();
    }
}

