package com.university.academic.service.attendance.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.university.academic.entity.attendance.*;
import com.university.academic.exception.ErrorCode;
import com.university.academic.exception.attendance.*;
import com.university.academic.repository.attendance.AttendanceDetailRepository;
import com.university.academic.repository.attendance.AttendanceRecordRepository;
import com.university.academic.security.SecurityUtils;
import com.university.academic.service.attendance.QRCodeAttendanceService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 二维码签到服务实现类
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QRCodeAttendanceServiceImpl implements QRCodeAttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceDetailRepository attendanceDetailRepository;

    /**
     * 二维码令牌缓存
     * Key: qrToken, Value: recordId
     */
    private Cache<String, Long> qrCodeCache;

    /**
     * 考勤记录到令牌的映射缓存
     * Key: recordId, Value: qrToken
     */
    private Cache<Long, String> recordTokenCache;

    /**
     * 默认二维码有效期（分钟）
     */
    private static final int QR_CODE_EXPIRE_MINUTES = 5;

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

    @PostConstruct
    public void init() {
        // 初始化二维码缓存，5分钟过期
        qrCodeCache = Caffeine.newBuilder()
                .expireAfterWrite(QR_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();

        // 初始化记录-令牌映射缓存
        recordTokenCache = Caffeine.newBuilder()
                .expireAfterWrite(QR_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();

        log.info("二维码缓存初始化完成，过期时间: {}分钟", QR_CODE_EXPIRE_MINUTES);
    }

    @Override
    @Transactional
    public String generateQRCode(Long recordId) {
        log.info("生成签到二维码: recordId={}", recordId);

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
        if (record.getMethod() != AttendanceMethod.QRCODE) {
            throw new AttendanceException(ErrorCode.ATTENDANCE_METHOD_NOT_SUPPORTED, "该考勤不支持扫码签到");
        }

        // 如果已有令牌，先失效
        String oldToken = recordTokenCache.getIfPresent(recordId);
        if (oldToken != null) {
            qrCodeCache.invalidate(oldToken);
        }

        // 生成唯一令牌
        String qrToken = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(QR_CODE_EXPIRE_MINUTES);

        // 更新考勤记录
        record.setQrToken(qrToken);
        record.setQrExpireTime(expireTime);
        attendanceRecordRepository.save(record);

        // 缓存令牌
        qrCodeCache.put(qrToken, recordId);
        recordTokenCache.put(recordId, qrToken);

        log.info("二维码生成成功: token={}, expireTime={}", qrToken, expireTime);
        return qrToken;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateQRCode(String qrToken) {
        // 检查缓存
        Long recordId = qrCodeCache.getIfPresent(qrToken);
        if (recordId == null) {
            log.warn("二维码令牌不存在或已过期: token={}", qrToken);
            return false;
        }

        // 从数据库验证
        AttendanceRecord record = attendanceRecordRepository.findByQrToken(qrToken)
                .orElse(null);

        if (record == null) {
            log.warn("数据库中未找到二维码令牌: token={}", qrToken);
            qrCodeCache.invalidate(qrToken);
            return false;
        }

        // 检查过期时间
        if (record.getQrExpireTime().isBefore(LocalDateTime.now())) {
            log.warn("二维码已过期: token={}, expireTime={}", qrToken, record.getQrExpireTime());
            qrCodeCache.invalidate(qrToken);
            return false;
        }

        // 检查考勤状态
        if (record.getStatus() != RecordStatus.IN_PROGRESS) {
            log.warn("考勤已结束: token={}, status={}", qrToken, record.getStatus());
            return false;
        }

        return true;
    }

    @Override
    @Transactional
    public AttendanceDetail checkIn(String qrToken, Long studentId) {
        log.info("学生扫码签到: token={}, studentId={}", qrToken, studentId);

        // 验证二维码
        if (!validateQRCode(qrToken)) {
            throw new QRCodeExpiredException();
        }

        // 获取考勤记录
        Long recordId = qrCodeCache.getIfPresent(qrToken);
        AttendanceRecord record = attendanceRecordRepository.findById(recordId)
                .orElseThrow(() -> new AttendanceNotFoundException());

        // 查找考勤明细
        AttendanceDetail detail = attendanceDetailRepository
                .findByAttendanceRecordIdAndStudentId(recordId, studentId)
                .orElseThrow(() -> new AttendanceException(ErrorCode.STUDENT_NOT_IN_COURSE));

        // 检查是否已签到
        if (detail.getCheckinTime() != null) {
            throw new DuplicateCheckinException("您已签到，请勿重复操作");
        }

        // 判定签到时间和状态
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        LocalTime attendanceTime = record.getAttendanceTime();

        AttendanceStatus status = determineAttendanceStatus(currentTime, attendanceTime);

        // 更新签到信息
        detail.setStatus(status);
        detail.setCheckinTime(now);

        AttendanceDetail savedDetail = attendanceDetailRepository.save(detail);
        log.info("签到成功: studentId={}, status={}, checkinTime={}", studentId, status, now);

        return savedDetail;
    }

    @Override
    @Transactional
    public void invalidateQRCode(Long recordId) {
        log.info("使二维码失效: recordId={}", recordId);

        // 从缓存中移除
        String qrToken = recordTokenCache.getIfPresent(recordId);
        if (qrToken != null) {
            qrCodeCache.invalidate(qrToken);
            recordTokenCache.invalidate(recordId);
        }

        // 更新数据库
        AttendanceRecord record = attendanceRecordRepository.findById(recordId).orElse(null);
        if (record != null) {
            record.setQrToken(null);
            record.setQrExpireTime(null);
            attendanceRecordRepository.save(record);
        }
    }

    @Override
    @Transactional
    public String refreshQRCode(Long recordId) {
        log.info("刷新二维码: recordId={}", recordId);

        // 先使旧的失效
        invalidateQRCode(recordId);

        // 生成新的
        return generateQRCode(recordId);
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

