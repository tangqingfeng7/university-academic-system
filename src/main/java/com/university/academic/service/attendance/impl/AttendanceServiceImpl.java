package com.university.academic.service.attendance.impl;

import com.university.academic.entity.CourseOffering;
import com.university.academic.entity.CourseSelection;
import com.university.academic.entity.LeaveRequest;
import com.university.academic.entity.Student;
import com.university.academic.entity.attendance.*;
import com.university.academic.exception.ErrorCode;
import com.university.academic.exception.attendance.AttendanceAlreadySubmittedException;
import com.university.academic.exception.attendance.AttendanceException;
import com.university.academic.exception.attendance.AttendanceNotFoundException;
import com.university.academic.repository.CourseOfferingRepository;
import com.university.academic.repository.CourseSelectionRepository;
import com.university.academic.repository.LeaveRequestRepository;
import com.university.academic.repository.attendance.AttendanceDetailRepository;
import com.university.academic.repository.attendance.AttendanceRecordRepository;
import com.university.academic.repository.attendance.AttendanceStatisticsRepository;
import com.university.academic.security.SecurityUtils;
import com.university.academic.service.attendance.AttendanceNotificationService;
import com.university.academic.service.attendance.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 考勤服务实现类
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceDetailRepository attendanceDetailRepository;
    private final AttendanceStatisticsRepository attendanceStatisticsRepository;
    private final CourseOfferingRepository offeringRepository;
    private final CourseSelectionRepository selectionRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendanceNotificationService notificationService;

    @Override
    @Transactional
    public AttendanceRecord startAttendance(Long offeringId, AttendanceMethod method) {
        log.info("开始考勤: offeringId={}, method={}", offeringId, method);

        // 获取开课计划
        CourseOffering offering = offeringRepository.findById(offeringId)
                .orElseThrow(() -> new AttendanceException(ErrorCode.OFFERING_NOT_FOUND));

        // 验证权限：教师只能操作自己的课程
        Long currentTeacherId = SecurityUtils.getCurrentTeacherId();
        if (!offering.getTeacher().getId().equals(currentTeacherId)) {
            throw new AttendanceException(ErrorCode.ATTENDANCE_PERMISSION_DENIED, "您无权对此课程进行考勤");
        }

        // 创建考勤记录
        AttendanceRecord record = AttendanceRecord.builder()
                .offering(offering)
                .teacher(offering.getTeacher())
                .attendanceDate(LocalDate.now())
                .attendanceTime(LocalTime.now())
                .method(method)
                .status(RecordStatus.IN_PROGRESS)
                .totalStudents(0)
                .presentCount(0)
                .absentCount(0)
                .lateCount(0)
                .leaveCount(0)
                .build();

        record = attendanceRecordRepository.save(record);

        // 获取选课学生列表
        List<CourseSelection> selections = selectionRepository.findByOfferingId(offeringId)
                .stream()
                .filter(s -> s.getStatus() == CourseSelection.SelectionStatus.SELECTED)
                .toList();

        // 查询今天已批准的请假学生
        Map<Long, LeaveRequest> approvedLeaveMap = getApprovedLeaveRequests(selections);

        // 创建考勤明细（默认状态为缺勤，有请假的标记为请假）
        List<AttendanceDetail> details = new ArrayList<>();
        for (CourseSelection selection : selections) {
            Student student = selection.getStudent();
            AttendanceStatus defaultStatus = AttendanceStatus.ABSENT;
            
            // 如果学生有已批准的请假，设置为请假状态
            if (approvedLeaveMap.containsKey(student.getId())) {
                defaultStatus = AttendanceStatus.LEAVE;
            }

            AttendanceDetail detail = AttendanceDetail.builder()
                    .attendanceRecord(record)
                    .student(student)
                    .status(defaultStatus)
                    .isMakeup(false)
                    .build();
            details.add(detail);
        }

        attendanceDetailRepository.saveAll(details);

        // 更新应到人数
        record.setTotalStudents(details.size());
        record.setLeaveCount((int) details.stream()
                .filter(d -> d.getStatus() == AttendanceStatus.LEAVE)
                .count());
        record.setAbsentCount(details.size() - record.getLeaveCount());

        AttendanceRecord savedRecord = attendanceRecordRepository.save(record);
        log.info("考勤创建成功: recordId={}, totalStudents={}", savedRecord.getId(), savedRecord.getTotalStudents());

        return savedRecord;
    }

    @Override
    @Transactional
    public AttendanceDetail recordAttendance(Long recordId, Long studentId, AttendanceStatus status, String remarks) {
        log.info("记录考勤: recordId={}, studentId={}, status={}", recordId, studentId, status);

        // 获取并验证考勤记录
        getAndValidateRecord(recordId);

        // 查找考勤明细
        AttendanceDetail detail = attendanceDetailRepository
                .findByAttendanceRecordIdAndStudentId(recordId, studentId)
                .orElseThrow(() -> new AttendanceException(ErrorCode.STUDENT_NOT_IN_COURSE, "学生不在选课名单中"));

        // 更新状态
        detail.setStatus(status);
        detail.setRemarks(remarks);
        detail.setModifiedBy(SecurityUtils.getCurrentUserId());

        return attendanceDetailRepository.save(detail);
    }

    @Override
    @Transactional
    public int batchRecordAttendance(Long recordId, List<Long> studentIds, AttendanceStatus status) {
        log.info("批量记录考勤: recordId={}, studentCount={}, status={}", recordId, studentIds.size(), status);

        // 验证考勤记录
        getAndValidateRecord(recordId);

        // 获取所有考勤明细
        List<AttendanceDetail> details = attendanceDetailRepository.findByAttendanceRecordId(recordId);
        
        Map<Long, AttendanceDetail> detailMap = details.stream()
                .collect(Collectors.toMap(d -> d.getStudent().getId(), d -> d));

        // 更新状态
        int count = 0;
        for (Long studentId : studentIds) {
            AttendanceDetail detail = detailMap.get(studentId);
            if (detail != null) {
                detail.setStatus(status);
                detail.setModifiedBy(SecurityUtils.getCurrentUserId());
                attendanceDetailRepository.save(detail);
                count++;
            }
        }

        return count;
    }

    @Override
    @Transactional
    public AttendanceRecord submitAttendance(Long recordId) {
        log.info("提交考勤: recordId={}", recordId);

        // 获取考勤记录
        AttendanceRecord record = attendanceRecordRepository.findByIdWithDetails(recordId)
                .orElseThrow(() -> new AttendanceNotFoundException("考勤记录不存在"));

        // 验证权限
        Long currentTeacherId = SecurityUtils.getCurrentTeacherId();
        if (!record.getTeacher().getId().equals(currentTeacherId)) {
            throw new AttendanceException(ErrorCode.ATTENDANCE_PERMISSION_DENIED, "您无权操作此考勤");
        }

        // 验证状态
        if (record.getStatus() == RecordStatus.SUBMITTED) {
            throw new AttendanceAlreadySubmittedException("考勤已提交");
        }

        // 获取所有明细
        List<AttendanceDetail> details = attendanceDetailRepository.findByAttendanceRecordId(recordId);

        // 计算统计数据
        long presentCount = details.stream().filter(d -> d.getStatus() == AttendanceStatus.PRESENT).count();
        long lateCount = details.stream().filter(d -> d.getStatus() == AttendanceStatus.LATE).count();
        long leaveCount = details.stream().filter(d -> d.getStatus() == AttendanceStatus.LEAVE).count();
        long absentCount = details.stream().filter(d -> d.getStatus() == AttendanceStatus.ABSENT).count();

        record.setPresentCount((int) presentCount);
        record.setLateCount((int) lateCount);
        record.setLeaveCount((int) leaveCount);
        record.setAbsentCount((int) absentCount);

        // 计算出勤率：(出勤+迟到) / 总人数
        if (record.getTotalStudents() > 0) {
            double rate = (presentCount + lateCount) * 100.0 / record.getTotalStudents();
            record.setAttendanceRate(Math.round(rate * 100.0) / 100.0);
        }

        // 更新状态
        record.setStatus(RecordStatus.SUBMITTED);
        record = attendanceRecordRepository.save(record);

        // 更新统计数据
        updateStatistics(record.getOffering().getId(), details);

        // 发送缺勤通知
        sendAbsenceNotifications(record, details);

        log.info("考勤提交成功: recordId={}, rate={}%", recordId, record.getAttendanceRate());
        return record;
    }

    @Override
    @Transactional
    public void cancelAttendance(Long recordId) {
        log.info("取消考勤: recordId={}", recordId);

        AttendanceRecord record = getAndValidateRecord(recordId);

        // 只能取消进行中的考勤
        if (record.getStatus() == RecordStatus.SUBMITTED) {
            throw new AttendanceException(ErrorCode.ATTENDANCE_ALREADY_SUBMITTED, "已提交的考勤无法取消");
        }

        record.setStatus(RecordStatus.CANCELLED);
        attendanceRecordRepository.save(record);
    }

    @Override
    @Transactional
    public AttendanceDetail updateAttendanceStatus(Long detailId, AttendanceStatus status, String modifyReason) {
        log.info("更新考勤状态: detailId={}, status={}", detailId, status);

        AttendanceDetail detail = attendanceDetailRepository.findById(detailId)
                .orElseThrow(() -> new AttendanceException(ErrorCode.ATTENDANCE_DETAIL_NOT_FOUND));

        // 验证考勤记录状态
        AttendanceRecord record = detail.getAttendanceRecord();
        if (record.getStatus() == RecordStatus.SUBMITTED) {
            throw new AttendanceAlreadySubmittedException("考勤已提交，如需修改请说明原因");
        }

        // 验证权限
        Long currentTeacherId = SecurityUtils.getCurrentTeacherId();
        if (!record.getTeacher().getId().equals(currentTeacherId)) {
            throw new AttendanceException(ErrorCode.ATTENDANCE_PERMISSION_DENIED);
        }

        detail.setStatus(status);
        detail.setModifiedBy(SecurityUtils.getCurrentUserId());
        detail.setModifyReason(modifyReason);

        return attendanceDetailRepository.save(detail);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceRecord getAttendanceRecord(Long recordId) {
        return attendanceRecordRepository.findByIdWithDetails(recordId)
                .orElseThrow(() -> new AttendanceNotFoundException());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceDetail> getAttendanceDetails(Long recordId) {
        return attendanceDetailRepository.findByAttendanceRecordIdWithStudent(recordId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceDetail> getStudentAttendanceList(Long studentId, Long offeringId) {
        List<AttendanceDetail> details = attendanceDetailRepository.findByStudentIdAndOfferingId(studentId, offeringId);
        // 触发懒加载以确保数据在transaction内被加载
        details.forEach(detail -> {
            if (detail.getAttendanceRecord() != null) {
                detail.getAttendanceRecord().getAttendanceDate();
                if (detail.getAttendanceRecord().getOffering() != null) {
                    detail.getAttendanceRecord().getOffering().getId();
                    if (detail.getAttendanceRecord().getOffering().getCourse() != null) {
                        detail.getAttendanceRecord().getOffering().getCourse().getName();
                    }
                }
                if (detail.getAttendanceRecord().getTeacher() != null) {
                    detail.getAttendanceRecord().getTeacher().getName();
                }
            }
        });
        return details;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRecord> getTeacherAttendanceList(Long teacherId) {
        // 使用JOIN FETCH查询确保数据已加载
        List<AttendanceRecord> records = attendanceRecordRepository.findByTeacherId(teacherId);
        // 触发懒加载
        records.forEach(r -> {
            if (r.getOffering() != null && r.getOffering().getCourse() != null) {
                r.getOffering().getCourse().getName();
            }
            if (r.getTeacher() != null) {
                r.getTeacher().getName();
            }
        });
        return records;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRecord> getOfferingAttendanceList(Long offeringId) {
        List<AttendanceRecord> records = attendanceRecordRepository.findByOfferingIdWithDetails(offeringId);
        // 触发懒加载
        records.forEach(r -> {
            if (r.getOffering() != null && r.getOffering().getCourse() != null) {
                r.getOffering().getCourse().getName();
            }
            if (r.getTeacher() != null) {
                r.getTeacher().getName();
            }
        });
        return records;
    }

    /**
     * 获取并验证考勤记录
     */
    private AttendanceRecord getAndValidateRecord(Long recordId) {
        AttendanceRecord record = attendanceRecordRepository.findById(recordId)
                .orElseThrow(() -> new AttendanceNotFoundException());

        // 验证权限
        Long currentTeacherId = SecurityUtils.getCurrentTeacherId();
        if (!record.getTeacher().getId().equals(currentTeacherId)) {
            throw new AttendanceException(ErrorCode.ATTENDANCE_PERMISSION_DENIED, "您无权操作此考勤");
        }

        // 验证状态
        if (record.getStatus() != RecordStatus.IN_PROGRESS) {
            throw new AttendanceException(ErrorCode.ATTENDANCE_NOT_IN_PROGRESS, "考勤不在进行中");
        }

        return record;
    }

    /**
     * 查询今天已批准的请假申请
     */
    private Map<Long, LeaveRequest> getApprovedLeaveRequests(List<CourseSelection> selections) {
        LocalDate today = LocalDate.now();
        List<Long> studentIds = selections.stream()
                .map(s -> s.getStudent().getId())
                .toList();

        if (studentIds.isEmpty()) {
            return Map.of();
        }

        return leaveRequestRepository.findAll().stream()
                .filter(lr -> lr.getApplicantType() == LeaveRequest.ApplicantType.STUDENT)
                .filter(lr -> studentIds.contains(lr.getApplicantId()))
                .filter(lr -> lr.getStatus() == LeaveRequest.ApprovalStatus.APPROVED)
                .filter(lr -> !lr.getStartDate().isAfter(today) && !lr.getEndDate().isBefore(today))
                .collect(Collectors.toMap(
                        LeaveRequest::getApplicantId,
                        lr -> lr,
                        (existing, replacement) -> existing
                ));
    }

    /**
     * 更新考勤统计数据
     */
    private void updateStatistics(Long offeringId, List<AttendanceDetail> details) {
        for (AttendanceDetail detail : details) {
            Long studentId = detail.getStudent().getId();

            // 查找或创建统计记录
            AttendanceStatistics statistics = attendanceStatisticsRepository
                    .findByStudentIdAndOfferingId(studentId, offeringId)
                    .orElse(AttendanceStatistics.builder()
                            .student(detail.getStudent())
                            .offering(detail.getAttendanceRecord().getOffering())
                            .totalClasses(0)
                            .presentCount(0)
                            .lateCount(0)
                            .earlyLeaveCount(0)
                            .leaveCount(0)
                            .absentCount(0)
                            .build());

            // 更新总课次
            statistics.setTotalClasses(statistics.getTotalClasses() + 1);

            // 更新各状态计数
            switch (detail.getStatus()) {
                case PRESENT -> statistics.setPresentCount(statistics.getPresentCount() + 1);
                case LATE -> statistics.setLateCount(statistics.getLateCount() + 1);
                case EARLY_LEAVE -> statistics.setEarlyLeaveCount(statistics.getEarlyLeaveCount() + 1);
                case LEAVE -> statistics.setLeaveCount(statistics.getLeaveCount() + 1);
                case ABSENT -> statistics.setAbsentCount(statistics.getAbsentCount() + 1);
            }

            // 计算出勤率
            if (statistics.getTotalClasses() > 0) {
                double rate = (statistics.getPresentCount() + statistics.getLateCount()) * 100.0 
                        / statistics.getTotalClasses();
                statistics.setAttendanceRate(Math.round(rate * 100.0) / 100.0);
            }

            statistics.setLastUpdated(LocalDateTime.now());
            attendanceStatisticsRepository.save(statistics);
        }
    }

    /**
     * 发送缺勤通知
     */
    private void sendAbsenceNotifications(AttendanceRecord record, List<AttendanceDetail> details) {
        // 获取缺勤和旷课的学生
        List<Long> absentStudentUserIds = details.stream()
                .filter(d -> d.getStatus() == AttendanceStatus.ABSENT || 
                            d.getStatus() == AttendanceStatus.LATE)
                .filter(d -> d.getStudent().getUser() != null)
                .map(d -> d.getStudent().getUser().getId())
                .toList();

        if (!absentStudentUserIds.isEmpty()) {
            String courseName = record.getOffering().getCourse().getName();
            String attendanceDate = record.getAttendanceDate().toString();
            
            notificationService.sendAbsenceNotification(
                    absentStudentUserIds, courseName, attendanceDate);
        }
    }
}

