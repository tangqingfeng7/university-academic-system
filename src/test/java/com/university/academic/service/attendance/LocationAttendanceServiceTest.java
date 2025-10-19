package com.university.academic.service.attendance;

import com.university.academic.entity.attendance.*;
import com.university.academic.exception.attendance.DuplicateCheckinException;
import com.university.academic.exception.attendance.LocationOutOfRangeException;
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
 * 定位签到服务集成测试
 *
 * @author Academic System Team
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("定位签到服务集成测试")
class LocationAttendanceServiceTest {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private LocationAttendanceService locationService;

    private Long testOfferingId;
    private Long testStudentId;

    // 测试坐标（北京天安门）
    private static final double TEST_LATITUDE = 39.9042;
    private static final double TEST_LONGITUDE = 116.4074;
    private static final int TEST_RADIUS = 100;

    @BeforeEach
    void setUp() {
        testOfferingId = 1L;
        testStudentId = 1L;
    }

    @Test
    @DisplayName("测试设置地理围栏")
    void testSetGeofence() {
        // 开始定位签到
        AttendanceRecord record = attendanceService.startAttendance(
                testOfferingId, AttendanceMethod.LOCATION);

        // 设置地理围栏
        locationService.setGeofence(
                record.getId(),
                TEST_LATITUDE,
                TEST_LONGITUDE,
                TEST_RADIUS
        );

        // 验证围栏已设置
        AttendanceRecord updated = attendanceService.getAttendanceRecord(record.getId());
        assertThat(updated.getLatitude()).isEqualTo(TEST_LATITUDE);
        assertThat(updated.getLongitude()).isEqualTo(TEST_LONGITUDE);
        assertThat(updated.getGeofenceRadius()).isEqualTo(TEST_RADIUS);
    }

    @Test
    @DisplayName("测试距离计算（Haversine公式）")
    void testCalculateDistance() {
        // 计算两个相近点的距离
        double lat1 = 39.9042;
        double lon1 = 116.4074;
        double lat2 = 39.9052;  // 北偏约111米
        double lon2 = 116.4074;

        double distance = locationService.calculateDistance(lat1, lon1, lat2, lon2);

        // 距离应该约为111米（纬度每度约111km）
        assertThat(distance).isBetween(100.0, 120.0);
    }

    @Test
    @DisplayName("测试位置验证")
    void testValidateLocation() {
        // 测试范围内的位置
        boolean inRange = locationService.validateLocation(
                TEST_LATITUDE, TEST_LONGITUDE,
                TEST_LATITUDE + 0.0005, TEST_LONGITUDE + 0.0005,  // 约50米
                TEST_RADIUS
        );
        assertThat(inRange).isTrue();

        // 测试范围外的位置
        boolean outRange = locationService.validateLocation(
                TEST_LATITUDE, TEST_LONGITUDE,
                TEST_LATITUDE + 0.01, TEST_LONGITUDE + 0.01,  // 约1000米
                TEST_RADIUS
        );
        assertThat(outRange).isFalse();
    }

    @Test
    @DisplayName("测试学生定位签到成功")
    void testLocationCheckInSuccess() {
        // 开始定位签到
        AttendanceRecord record = attendanceService.startAttendance(
                testOfferingId, AttendanceMethod.LOCATION);

        // 设置地理围栏
        locationService.setGeofence(
                record.getId(),
                TEST_LATITUDE,
                TEST_LONGITUDE,
                TEST_RADIUS
        );

        // 学生在范围内签到
        AttendanceDetail detail = locationService.checkIn(
                record.getId(),
                testStudentId,
                TEST_LATITUDE + 0.0003,  // 约30米
                TEST_LONGITUDE + 0.0003
        );

        assertThat(detail).isNotNull();
        assertThat(detail.getStatus()).isIn(AttendanceStatus.PRESENT, AttendanceStatus.LATE);
        assertThat(detail.getCheckinTime()).isNotNull();
        assertThat(detail.getCheckinLatitude()).isNotNull();
        assertThat(detail.getCheckinLongitude()).isNotNull();
    }

    @Test
    @DisplayName("测试定位签到超出范围")
    void testLocationCheckInOutOfRange() {
        // 开始定位签到
        AttendanceRecord record = attendanceService.startAttendance(
                testOfferingId, AttendanceMethod.LOCATION);

        // 设置地理围栏
        locationService.setGeofence(
                record.getId(),
                TEST_LATITUDE,
                TEST_LONGITUDE,
                TEST_RADIUS
        );

        // 学生在范围外签到应该抛出异常
        assertThatThrownBy(() ->
                locationService.checkIn(
                        record.getId(),
                        testStudentId,
                        TEST_LATITUDE + 0.01,  // 约1000米
                        TEST_LONGITUDE + 0.01
                )
        ).isInstanceOf(LocationOutOfRangeException.class);
    }

    @Test
    @DisplayName("测试定位签到重复检测")
    void testLocationDuplicateCheckIn() {
        // 开始定位签到
        AttendanceRecord record = attendanceService.startAttendance(
                testOfferingId, AttendanceMethod.LOCATION);

        locationService.setGeofence(record.getId(), TEST_LATITUDE, TEST_LONGITUDE, TEST_RADIUS);

        // 第一次签到
        locationService.checkIn(record.getId(), testStudentId, TEST_LATITUDE, TEST_LONGITUDE);

        // 第二次签到应该抛出异常
        assertThatThrownBy(() ->
                locationService.checkIn(record.getId(), testStudentId, TEST_LATITUDE, TEST_LONGITUDE)
        ).isInstanceOf(DuplicateCheckinException.class);
    }
}

