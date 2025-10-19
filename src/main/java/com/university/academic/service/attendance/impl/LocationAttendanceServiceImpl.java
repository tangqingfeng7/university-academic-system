package com.university.academic.service.attendance.impl;

import com.university.academic.entity.attendance.*;
import com.university.academic.exception.ErrorCode;
import com.university.academic.exception.attendance.*;
import com.university.academic.repository.attendance.AttendanceDetailRepository;
import com.university.academic.repository.attendance.AttendanceRecordRepository;
import com.university.academic.security.SecurityUtils;
import com.university.academic.service.attendance.LocationAttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 定位签到服务实现类
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationAttendanceServiceImpl implements LocationAttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceDetailRepository attendanceDetailRepository;

    /**
     * 地球半径（千米）
     */
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * 默认地理围栏半径（米）
     */
    private static final int DEFAULT_GEOFENCE_RADIUS = 100;

    /**
     * 允许提前签到的时间（分钟）
     */
    private static final int EARLY_CHECKIN_MINUTES = 5;

    /**
     * 迟到判定时间（分钟）
     */
    private static final int LATE_THRESHOLD_MINUTES = 5;

    /**
     * 最晚签到时间（分钟）
     */
    private static final int MAX_LATE_MINUTES = 15;

    @Override
    @Transactional
    public void setGeofence(Long recordId, Double latitude, Double longitude, Integer radius) {
        log.info("设置地理围栏: recordId={}, lat={}, lon={}, radius={}米", 
                recordId, latitude, longitude, radius);

        // 验证参数
        validateCoordinates(latitude, longitude);
        if (radius == null || radius <= 0) {
            radius = DEFAULT_GEOFENCE_RADIUS;
        }

        // 获取考勤记录
        AttendanceRecord record = attendanceRecordRepository.findById(recordId)
                .orElseThrow(() -> new AttendanceNotFoundException());

        // 验证权限
        Long currentTeacherId = SecurityUtils.getCurrentTeacherId();
        if (!record.getTeacher().getId().equals(currentTeacherId)) {
            throw new AttendanceException(ErrorCode.ATTENDANCE_PERMISSION_DENIED);
        }

        // 验证考勤状态
        if (record.getStatus() != RecordStatus.IN_PROGRESS) {
            throw new AttendanceException(ErrorCode.ATTENDANCE_NOT_IN_PROGRESS);
        }

        // 验证考勤方式
        if (record.getMethod() != AttendanceMethod.LOCATION) {
            throw new AttendanceException(ErrorCode.ATTENDANCE_METHOD_NOT_SUPPORTED, "该考勤不支持定位签到");
        }

        // 设置地理围栏
        record.setLatitude(latitude);
        record.setLongitude(longitude);
        record.setGeofenceRadius(radius);
        attendanceRecordRepository.save(record);

        log.info("地理围栏设置成功: recordId={}", recordId);
    }

    @Override
    public boolean validateLocation(Double lat1, Double lon1, Double lat2, Double lon2, Integer radius) {
        // 验证参数
        validateCoordinates(lat1, lon1);
        validateCoordinates(lat2, lon2);

        // 计算距离
        double distance = calculateDistance(lat1, lon1, lat2, lon2);

        // 判断是否在范围内
        return distance <= radius;
    }

    @Override
    @Transactional
    public AttendanceDetail checkIn(Long recordId, Long studentId, Double latitude, Double longitude) {
        log.info("学生定位签到: recordId={}, studentId={}, lat={}, lon={}", 
                recordId, studentId, latitude, longitude);

        // 验证坐标
        validateCoordinates(latitude, longitude);

        // 获取考勤记录
        AttendanceRecord record = attendanceRecordRepository.findById(recordId)
                .orElseThrow(() -> new AttendanceNotFoundException());

        // 验证考勤状态
        if (record.getStatus() != RecordStatus.IN_PROGRESS) {
            throw new AttendanceException(ErrorCode.ATTENDANCE_NOT_IN_PROGRESS);
        }

        // 验证考勤方式
        if (record.getMethod() != AttendanceMethod.LOCATION) {
            throw new AttendanceException(ErrorCode.ATTENDANCE_METHOD_NOT_SUPPORTED, "该考勤不支持定位签到");
        }

        // 验证地理围栏是否已设置
        if (record.getLatitude() == null || record.getLongitude() == null) {
            throw new AttendanceException(ErrorCode.INVALID_OPERATION, "地理围栏尚未设置");
        }

        // 查找考勤明细
        AttendanceDetail detail = attendanceDetailRepository
                .findByAttendanceRecordIdAndStudentId(recordId, studentId)
                .orElseThrow(() -> new AttendanceException(ErrorCode.STUDENT_NOT_IN_COURSE));

        // 检查是否已签到
        if (detail.getCheckinTime() != null) {
            throw new DuplicateCheckinException("您已签到，请勿重复操作");
        }

        // 验证位置是否在地理围栏内
        Integer radius = record.getGeofenceRadius() != null ? 
                record.getGeofenceRadius() : DEFAULT_GEOFENCE_RADIUS;

        if (!validateLocation(record.getLatitude(), record.getLongitude(), 
                latitude, longitude, radius)) {
            double distance = calculateDistance(record.getLatitude(), record.getLongitude(), 
                    latitude, longitude);
            throw new LocationOutOfRangeException(
                    String.format("您不在签到范围内（距离: %.0f米，要求: %d米）", distance, radius));
        }

        // 判定签到时间和状态
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        LocalTime attendanceTime = record.getAttendanceTime();

        AttendanceStatus status = determineAttendanceStatus(currentTime, attendanceTime);

        // 更新签到信息
        detail.setStatus(status);
        detail.setCheckinTime(now);
        detail.setCheckinLatitude(latitude);
        detail.setCheckinLongitude(longitude);

        AttendanceDetail savedDetail = attendanceDetailRepository.save(detail);
        log.info("定位签到成功: studentId={}, status={}, distance={}米", 
                studentId, status, 
                calculateDistance(record.getLatitude(), record.getLongitude(), latitude, longitude));

        return savedDetail;
    }

    @Override
    public double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        // 验证参数
        validateCoordinates(lat1, lon1);
        validateCoordinates(lat2, lon2);

        // 将角度转换为弧度
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // 使用Haversine公式计算距离
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 距离（千米）转换为米
        double distanceKm = EARTH_RADIUS_KM * c;
        return distanceKm * 1000;
    }

    /**
     * 验证坐标合法性
     *
     * @param latitude 纬度
     * @param longitude 经度
     */
    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new AttendanceException(ErrorCode.PARAM_ERROR, "坐标不能为空");
        }

        if (latitude < -90 || latitude > 90) {
            throw new AttendanceException(ErrorCode.PARAM_ERROR, "纬度必须在-90到90之间");
        }

        if (longitude < -180 || longitude > 180) {
            throw new AttendanceException(ErrorCode.PARAM_ERROR, "经度必须在-180到180之间");
        }
    }

    /**
     * 根据签到时间判定考勤状态
     *
     * @param checkinTime    签到时间
     * @param attendanceTime 考勤开始时间
     * @return 考勤状态
     */
    private AttendanceStatus determineAttendanceStatus(LocalTime checkinTime, LocalTime attendanceTime) {
        // 允许提前5分钟签到
        LocalTime earlyAllowedTime = attendanceTime.minusMinutes(EARLY_CHECKIN_MINUTES);
        // 迟到判定时间（开始后5分钟内）
        LocalTime lateThresholdTime = attendanceTime.plusMinutes(LATE_THRESHOLD_MINUTES);
        // 最晚签到时间（开始后15分钟）
        LocalTime maxLateTime = attendanceTime.plusMinutes(MAX_LATE_MINUTES);

        if (checkinTime.isBefore(earlyAllowedTime)) {
            // 签到时间过早
            throw new InvalidAttendanceTimeException(ErrorCode.ATTENDANCE_TIME_NOT_VALID, "签到时间过早");
        } else if (checkinTime.isAfter(maxLateTime)) {
            // 签到时间过晚
            throw new InvalidAttendanceTimeException(ErrorCode.CHECKIN_TOO_LATE, "签到时间已过");
        } else if (checkinTime.isAfter(lateThresholdTime)) {
            // 迟到
            return AttendanceStatus.LATE;
        } else {
            // 出勤
            return AttendanceStatus.PRESENT;
        }
    }
}

