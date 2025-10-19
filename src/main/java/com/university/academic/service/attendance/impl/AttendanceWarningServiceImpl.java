package com.university.academic.service.attendance.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.academic.entity.CourseOffering;
import com.university.academic.entity.Student;
import com.university.academic.entity.Teacher;
import com.university.academic.entity.attendance.*;
import com.university.academic.exception.ErrorCode;
import com.university.academic.exception.attendance.AttendanceException;
import com.university.academic.repository.CourseOfferingRepository;
import com.university.academic.repository.StudentRepository;
import com.university.academic.repository.TeacherRepository;
import com.university.academic.repository.attendance.AttendanceDetailRepository;
import com.university.academic.repository.attendance.AttendanceRecordRepository;
import com.university.academic.repository.attendance.AttendanceStatisticsRepository;
import com.university.academic.repository.attendance.AttendanceWarningRepository;
import com.university.academic.security.SecurityUtils;
import com.university.academic.service.attendance.AttendanceNotificationService;
import com.university.academic.service.attendance.AttendanceWarningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 考勤预警服务实现类
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceWarningServiceImpl implements AttendanceWarningService {

    private final AttendanceWarningRepository warningRepository;
    private final AttendanceStatisticsRepository statisticsRepository;
    private final AttendanceRecordRepository recordRepository;
    private final AttendanceDetailRepository detailRepository;
    private final CourseOfferingRepository offeringRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ObjectMapper objectMapper;
    private final AttendanceNotificationService notificationService;

    // 预警阈值配置
    private static final int ABSENT_WARNING_THRESHOLD = 3;  // 旷课3次预警
    private static final double ABSENT_RATE_THRESHOLD = 0.33;  // 旷课达1/3课时严重预警
    private static final double LOW_ATTENDANCE_RATE = 0.70;  // 出勤率低于70%预警
    private static final int TEACHER_NO_ATTENDANCE_DAYS = 7;  // 教师7天未考勤提醒

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception.class)
    public void checkAbsenceWarning(Long studentId, Long offeringId) {
        try {
            log.info("检查学生旷课预警: studentId={}, offeringId={}", studentId, offeringId);

            AttendanceStatistics stats = statisticsRepository
                    .findByStudentIdAndOfferingId(studentId, offeringId)
                    .orElse(null);

            if (stats == null || stats.getAbsentCount() == 0) {
                return;
            }

            Student student = studentRepository.findById(studentId).orElse(null);
            CourseOffering offering = offeringRepository.findById(offeringId).orElse(null);

            if (student == null || offering == null) {
                return;
            }

            int absentCount = stats.getAbsentCount();
            int totalClasses = stats.getTotalClasses();
            double absentRate = totalClasses > 0 ? (double) absentCount / totalClasses : 0;

            // 判定预警级别
            Integer warningLevel = null;
            String message = null;

            if (absentRate >= ABSENT_RATE_THRESHOLD) {
                // 严重：旷课达1/3课时
                warningLevel = 3;
                message = String.format("学生%s在课程《%s》中旷课已达%d次（占总课时的%.0f%%），情况严重！",
                        student.getName(), offering.getCourse().getName(),
                        absentCount, absentRate * 100);
            } else if (absentCount >= ABSENT_WARNING_THRESHOLD) {
                // 中度：旷课3次
                warningLevel = 2;
                message = String.format("学生%s在课程《%s》中已旷课%d次，请关注！",
                        student.getName(), offering.getCourse().getName(), absentCount);
            }

            if (warningLevel != null) {
                createWarningIfNotExists(
                        WarningType.STUDENT_ABSENT,
                        "STUDENT",
                        studentId,
                        student.getName(),
                        offering,
                        warningLevel,
                        message,
                        stats
                );
            }
        } catch (Exception e) {
            log.error("检查学生旷课预警失败: studentId={}, offeringId={}", studentId, offeringId, e);
            // 不抛出异常，避免影响其他检查
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception.class)
    public void checkCourseAttendanceRate(Long offeringId) {
        try {
            log.info("检查课程出勤率预警: offeringId={}", offeringId);

            Double avgRate = statisticsRepository.getAverageAttendanceRateByOffering(offeringId);

            if (avgRate == null || avgRate >= LOW_ATTENDANCE_RATE * 100) {
                return;
            }

            CourseOffering offering = offeringRepository.findById(offeringId).orElse(null);
            if (offering == null) {
                return;
            }

            String message = String.format("课程《%s》的平均出勤率为%.2f%%，低于标准（70%%），请关注！",
                    offering.getCourse().getName(), avgRate);

            Map<String, Object> data = new HashMap<>();
            data.put("averageRate", avgRate);
            data.put("threshold", LOW_ATTENDANCE_RATE * 100);

            createWarningIfNotExists(
                    WarningType.COURSE_LOW_RATE,
                    "COURSE",
                    offeringId,
                    offering.getCourse().getName(),
                    offering,
                    2,
                    message,
                    data
            );
        } catch (Exception e) {
            log.error("检查课程出勤率预警失败: offeringId={}", offeringId, e);
            // 不抛出异常，避免影响其他检查
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception.class)
    public void checkTeacherAttendanceCompletion(Long teacherId) {
        try {
            log.info("检查教师考勤完成情况: teacherId={}", teacherId);

            LocalDate weekAgo = LocalDate.now().minusDays(TEACHER_NO_ATTENDANCE_DAYS);
            long recentCount = recordRepository.countByTeacherIdAndDateRange(
                    teacherId, weekAgo, LocalDate.now());

            if (recentCount > 0) {
                return;  // 最近有考勤记录
            }

            Teacher teacher = teacherRepository.findById(teacherId).orElse(null);
            if (teacher == null) {
                return;
            }

            String message = String.format("教师%s近%d天未进行考勤，请及时完成考勤工作！",
                    teacher.getName(), TEACHER_NO_ATTENDANCE_DAYS);

            createWarningIfNotExists(
                    WarningType.TEACHER_NO_ATTENDANCE,
                    "TEACHER",
                    teacherId,
                    teacher.getName(),
                    null,
                    1,
                    message,
                    null
            );
        } catch (Exception e) {
            log.error("检查教师考勤完成情况失败: teacherId={}", teacherId, e);
            // 不抛出异常，避免影响其他检查
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception.class)
    public void checkAttendanceAnomalies(Long recordId) {
        try {
            log.info("检测考勤异常: recordId={}", recordId);

            AttendanceRecord record = recordRepository.findById(recordId).orElse(null);
            if (record == null || record.getStatus() != RecordStatus.SUBMITTED) {
                return;
            }

            // 检查签到人数是否异常
            long checkedInCount = detailRepository.countByAttendanceRecordIdAndStatus(
                    recordId, AttendanceStatus.PRESENT) +
                    detailRepository.countByAttendanceRecordIdAndStatus(
                            recordId, AttendanceStatus.LATE);

            if (checkedInCount > record.getTotalStudents()) {
                String message = String.format("考勤异常：签到人数（%d人）超过应到人数（%d人）！",
                        checkedInCount, record.getTotalStudents());

                Map<String, Object> data = new HashMap<>();
                data.put("recordId", recordId);
                data.put("checkedInCount", checkedInCount);
                data.put("totalStudents", record.getTotalStudents());

                createWarning(
                        WarningType.STUDENT_ABSENT,  // 使用通用类型
                        "RECORD",
                        recordId,
                        "考勤记录#" + recordId,
                        record.getOffering(),
                        3,
                        message,
                        data
                );
            }
        } catch (Exception e) {
            log.error("检测考勤异常失败: recordId={}", recordId, e);
            // 不抛出异常，避免影响其他检查
        }
    }

    @Override
    public void sendWarningNotification(AttendanceWarning warning) {
        log.info("发送预警通知: warningId={}, type={}", warning.getId(), warning.getWarningType());
        
        try {
            notificationService.sendWarningNotification(warning);
        } catch (Exception e) {
            log.error("发送预警通知异常: warningId={}", warning.getId(), e);
            // 不抛出异常，避免影响调用方的事务
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceWarning> getWarningList(WarningStatus status) {
        if (status == null) {
            return warningRepository.findAll();
        }
        return warningRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    @Transactional
    public void handleWarning(Long warningId, String handleComment) {
        log.info("处理预警: warningId={}", warningId);

        AttendanceWarning warning = warningRepository.findById(warningId)
                .orElseThrow(() -> new AttendanceException(ErrorCode.ATTENDANCE_WARNING_NOT_FOUND));

        warning.setStatus(WarningStatus.HANDLED);
        warning.setHandledBy(SecurityUtils.getCurrentUserId());
        warning.setHandledAt(LocalDateTime.now());
        warning.setHandleComment(handleComment);

        warningRepository.save(warning);
    }

    @Override
    @Transactional
    public void ignoreWarning(Long warningId) {
        log.info("忽略预警: warningId={}", warningId);

        AttendanceWarning warning = warningRepository.findById(warningId)
                .orElseThrow(() -> new AttendanceException(ErrorCode.ATTENDANCE_WARNING_NOT_FOUND));

        warning.setStatus(WarningStatus.IGNORED);
        warning.setHandledBy(SecurityUtils.getCurrentUserId());
        warning.setHandledAt(LocalDateTime.now());

        warningRepository.save(warning);
    }

    @Override
    @Scheduled(cron = "0 0 2 * * ?")  // 每天凌晨2点执行
    public void executeAllWarningChecks() {
        log.info("开始执行定时预警检测");

        try {
            // 检查所有学生的旷课情况
            List<AttendanceStatistics> allStats = statisticsRepository.findAllWithDetails();
            for (AttendanceStatistics stats : allStats) {
                try {
                    checkAbsenceWarning(stats.getStudent().getId(), stats.getOffering().getId());
                } catch (Exception e) {
                    log.error("检查旷课预警失败: studentId={}, offeringId={}", 
                            stats.getStudent().getId(), stats.getOffering().getId(), e);
                }
            }

            // 检查所有课程的出勤率
            List<CourseOffering> offerings = offeringRepository.findAll();
            for (CourseOffering offering : offerings) {
                try {
                    checkCourseAttendanceRate(offering.getId());
                } catch (Exception e) {
                    log.error("检查课程出勤率预警失败: offeringId={}", offering.getId(), e);
                }
            }

            // 检查所有教师的考勤完成情况
            List<Teacher> teachers = teacherRepository.findAll();
            for (Teacher teacher : teachers) {
                try {
                    checkTeacherAttendanceCompletion(teacher.getId());
                } catch (Exception e) {
                    log.error("检查教师考勤完成情况失败: teacherId={}", teacher.getId(), e);
                }
            }

            log.info("定时预警检测完成");
        } catch (Exception e) {
            log.error("定时预警检测失败", e);
        }
    }

    /**
     * 创建预警（如果不存在相同的待处理预警）
     */
    private void createWarningIfNotExists(WarningType warningType, String targetType,
                                          Long targetId, String targetName,
                                          CourseOffering offering, Integer warningLevel,
                                          String message, Object data) {
        // 检查是否已存在相同的待处理预警
        boolean exists = warningRepository.existsPendingWarning(
                warningType, targetType, targetId,
                offering != null ? offering.getId() : null
        );

        if (!exists) {
            createWarning(warningType, targetType, targetId, targetName,
                    offering, warningLevel, message, data);
        }
    }

    /**
     * 创建预警记录
     */
    private void createWarning(WarningType warningType, String targetType,
                              Long targetId, String targetName,
                              CourseOffering offering, Integer warningLevel,
                              String message, Object data) {
        try {
            String warningData = data != null ? objectMapper.writeValueAsString(data) : null;

            AttendanceWarning warning = AttendanceWarning.builder()
                    .warningType(warningType)
                    .targetType(targetType)
                    .targetId(targetId)
                    .targetName(targetName)
                    .offering(offering)
                    .warningLevel(warningLevel)
                    .warningMessage(message)
                    .warningData(warningData)
                    .status(WarningStatus.PENDING)
                    .build();

            warningRepository.save(warning);

            // 发送通知（捕获异常避免影响预警创建）
            try {
                notificationService.sendWarningNotification(warning);
            } catch (Exception e) {
                log.error("发送预警通知失败: warningId={}", warning.getId(), e);
            }

            log.info("预警创建成功: type={}, target={}#{}", warningType, targetType, targetId);
        } catch (Exception e) {
            log.error("创建预警失败", e);
        }
    }
}

