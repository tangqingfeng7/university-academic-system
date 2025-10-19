package com.university.academic.service.attendance;

import com.university.academic.entity.attendance.*;
import com.university.academic.exception.attendance.DuplicateCheckinException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 扫码签到服务集成测试
 *
 * @author Academic System Team
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("扫码签到服务集成测试")
class QRCodeAttendanceServiceTest {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private QRCodeAttendanceService qrCodeService;

    private Long testOfferingId;
    private Long testStudentId;

    @BeforeEach
    void setUp() {
        testOfferingId = 1L;
        testStudentId = 1L;
    }

    @Test
    @DisplayName("测试生成二维码")
    void testGenerateQRCode() {
        // 开始扫码签到
        AttendanceRecord record = attendanceService.startAttendance(
                testOfferingId, AttendanceMethod.QRCODE);

        // 生成二维码
        String qrToken = qrCodeService.generateQRCode(record.getId());

        assertThat(qrToken).isNotNull();
        assertThat(qrToken).isNotEmpty();
        assertThat(qrToken.length()).isEqualTo(32); // UUID去掉"-"后的长度
    }

    @Test
    @DisplayName("测试验证二维码有效性")
    void testValidateQRCode() {
        // 开始扫码签到并生成二维码
        AttendanceRecord record = attendanceService.startAttendance(
                testOfferingId, AttendanceMethod.QRCODE);
        String qrToken = qrCodeService.generateQRCode(record.getId());

        // 验证二维码有效
        boolean isValid = qrCodeService.validateQRCode(qrToken);
        assertThat(isValid).isTrue();

        // 验证无效的令牌
        boolean isInvalid = qrCodeService.validateQRCode("invalid_token");
        assertThat(isInvalid).isFalse();
    }

    @Test
    @DisplayName("测试学生扫码签到")
    void testStudentCheckIn() {
        // 开始扫码签到并生成二维码
        AttendanceRecord record = attendanceService.startAttendance(
                testOfferingId, AttendanceMethod.QRCODE);
        String qrToken = qrCodeService.generateQRCode(record.getId());

        // 学生扫码签到
        AttendanceDetail detail = qrCodeService.checkIn(qrToken, testStudentId);

        assertThat(detail).isNotNull();
        assertThat(detail.getStatus()).isIn(AttendanceStatus.PRESENT, AttendanceStatus.LATE);
        assertThat(detail.getCheckinTime()).isNotNull();
    }

    @Test
    @DisplayName("测试重复签到检测")
    void testDuplicateCheckIn() {
        // 开始扫码签到并生成二维码
        AttendanceRecord record = attendanceService.startAttendance(
                testOfferingId, AttendanceMethod.QRCODE);
        String qrToken = qrCodeService.generateQRCode(record.getId());

        // 第一次签到
        qrCodeService.checkIn(qrToken, testStudentId);

        // 第二次签到应该抛出异常
        assertThatThrownBy(() ->
                qrCodeService.checkIn(qrToken, testStudentId)
        ).isInstanceOf(DuplicateCheckinException.class);
    }

    @Test
    @DisplayName("测试二维码刷新")
    void testRefreshQRCode() {
        // 开始扫码签到并生成二维码
        AttendanceRecord record = attendanceService.startAttendance(
                testOfferingId, AttendanceMethod.QRCODE);
        String oldToken = qrCodeService.generateQRCode(record.getId());

        // 刷新二维码
        String newToken = qrCodeService.refreshQRCode(record.getId());

        assertThat(newToken).isNotNull();
        assertThat(newToken).isNotEqualTo(oldToken);

        // 旧令牌应该无效
        boolean oldValid = qrCodeService.validateQRCode(oldToken);
        assertThat(oldValid).isFalse();

        // 新令牌应该有效
        boolean newValid = qrCodeService.validateQRCode(newToken);
        assertThat(newValid).isTrue();
    }

    @Test
    @DisplayName("测试使二维码失效")
    void testInvalidateQRCode() {
        // 开始扫码签到并生成二维码
        AttendanceRecord record = attendanceService.startAttendance(
                testOfferingId, AttendanceMethod.QRCODE);
        String qrToken = qrCodeService.generateQRCode(record.getId());

        // 验证二维码有效
        assertThat(qrCodeService.validateQRCode(qrToken)).isTrue();

        // 使二维码失效
        qrCodeService.invalidateQRCode(record.getId());

        // 验证二维码已失效
        assertThat(qrCodeService.validateQRCode(qrToken)).isFalse();
    }
}

