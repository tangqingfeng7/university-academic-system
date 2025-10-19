package com.university.academic.service.attendance.impl;

import com.university.academic.entity.attendance.*;
import com.university.academic.exception.ErrorCode;
import com.university.academic.exception.attendance.AttendanceException;
import com.university.academic.repository.StudentRepository;
import com.university.academic.repository.attendance.AttendanceDetailRepository;
import com.university.academic.repository.attendance.AttendanceRequestRepository;
import com.university.academic.repository.attendance.AttendanceStatisticsRepository;
import com.university.academic.security.SecurityUtils;
import com.university.academic.service.attendance.AttendanceAppealService;
import com.university.academic.service.attendance.AttendanceNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考勤申诉服务实现类
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceAppealServiceImpl implements AttendanceAppealService {

    private final AttendanceRequestRepository requestRepository;
    private final AttendanceDetailRepository detailRepository;
    private final AttendanceStatisticsRepository statisticsRepository;
    private final StudentRepository studentRepository;
    private final AttendanceNotificationService notificationService;

    @Override
    @Transactional
    public AttendanceRequest submitMakeupRequest(Long studentId, Long detailId, 
                                                 String reason, String attachmentUrl) {
        log.info("提交补签申请: studentId={}, detailId={}", studentId, detailId);

        return submitRequest(studentId, detailId, RequestType.MAKEUP, reason, attachmentUrl);
    }

    @Override
    @Transactional
    public AttendanceRequest submitAppeal(Long studentId, Long detailId, 
                                         String reason, String attachmentUrl) {
        log.info("提交考勤申诉: studentId={}, detailId={}", studentId, detailId);

        return submitRequest(studentId, detailId, RequestType.APPEAL, reason, attachmentUrl);
    }

    @Override
    @Transactional
    public AttendanceRequest approveRequest(Long requestId, String approvalComment) {
        log.info("批准申请: requestId={}", requestId);

        AttendanceRequest request = requestRepository.findByIdWithDetails(requestId)
                .orElseThrow(() -> new AttendanceException(ErrorCode.ATTENDANCE_REQUEST_NOT_FOUND));

        // 验证权限：必须是该课程的教师
        Long currentTeacherId = SecurityUtils.getCurrentTeacherId();
        if (!request.getAttendanceDetail().getAttendanceRecord().getTeacher().getId()
                .equals(currentTeacherId)) {
            throw new AttendanceException(ErrorCode.ATTENDANCE_PERMISSION_DENIED, "您无权审批此申请");
        }

        // 验证状态
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new AttendanceException(ErrorCode.ATTENDANCE_REQUEST_ALREADY_PROCESSED, "申请已处理");
        }

        // 更新申请状态
        request.setStatus(RequestStatus.APPROVED);
        request.setApproverId(currentTeacherId);
        request.setApproverName(SecurityUtils.getCurrentUsername());
        request.setApprovalComment(approvalComment);
        request.setApprovalTime(LocalDateTime.now());

        request = requestRepository.save(request);

        // 更新考勤明细
        AttendanceDetail detail = request.getAttendanceDetail();
        AttendanceStatus oldStatus = detail.getStatus();
        
        if (request.getRequestType() == RequestType.MAKEUP) {
            // 补签：更新为出勤
            detail.setStatus(AttendanceStatus.PRESENT);
            detail.setIsMakeup(true);
            detail.setCheckinTime(LocalDateTime.now());
        } else {
            // 申诉：根据申诉原因更新状态（这里默认更新为出勤，实际可以更灵活）
            detail.setStatus(AttendanceStatus.PRESENT);
        }
        
        detail.setModifiedBy(currentTeacherId);
        detail.setModifyReason("申请已批准：" + (approvalComment != null ? approvalComment : ""));
        detailRepository.save(detail);

        // 更新统计数据
        updateStatisticsAfterApproval(detail, oldStatus);

        log.info("申请批准成功: requestId={}, type={}", requestId, request.getRequestType());
        
        // 发送审批结果通知给学生
        notificationService.sendApprovalResultNotification(request);

        return request;
    }

    @Override
    @Transactional
    public AttendanceRequest rejectRequest(Long requestId, String reason) {
        log.info("拒绝申请: requestId={}", requestId);

        AttendanceRequest request = requestRepository.findByIdWithDetails(requestId)
                .orElseThrow(() -> new AttendanceException(ErrorCode.ATTENDANCE_REQUEST_NOT_FOUND));

        // 验证权限
        Long currentTeacherId = SecurityUtils.getCurrentTeacherId();
        if (!request.getAttendanceDetail().getAttendanceRecord().getTeacher().getId()
                .equals(currentTeacherId)) {
            throw new AttendanceException(ErrorCode.ATTENDANCE_PERMISSION_DENIED, "您无权审批此申请");
        }

        // 验证状态
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new AttendanceException(ErrorCode.ATTENDANCE_REQUEST_ALREADY_PROCESSED, "申请已处理");
        }

        // 更新申请状态
        request.setStatus(RequestStatus.REJECTED);
        request.setApproverId(currentTeacherId);
        request.setApproverName(SecurityUtils.getCurrentUsername());
        request.setApprovalComment(reason);
        request.setApprovalTime(LocalDateTime.now());

        request = requestRepository.save(request);
        
        log.info("申请已拒绝: requestId={}", requestId);
        
        // 发送审批结果通知给学生
        notificationService.sendApprovalResultNotification(request);

        return request;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRequest> getStudentRequests(Long studentId) {
        return requestRepository.findByStudentIdWithDetails(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRequest> getStudentRequests(Long studentId, RequestStatus status) {
        if (status == null) {
            return getStudentRequests(studentId);
        }
        return requestRepository.findByStudentIdAndStatus(studentId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRequest> getPendingRequests(Long teacherId) {
        return requestRepository.findPendingByTeacherId(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRequest> getTeacherRequests(Long teacherId, RequestStatus status) {
        if (status == null) {
            return requestRepository.findByTeacherId(teacherId);
        }
        return requestRepository.findByTeacherIdAndStatus(teacherId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceRequest getRequestDetails(Long requestId) {
        return requestRepository.findByIdWithDetails(requestId)
                .orElseThrow(() -> new AttendanceException(ErrorCode.ATTENDANCE_REQUEST_NOT_FOUND));
    }

    @Override
    @Transactional
    public void cancelRequest(Long requestId) {
        log.info("取消申请: requestId={}", requestId);

        AttendanceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new AttendanceException(ErrorCode.ATTENDANCE_REQUEST_NOT_FOUND));

        // 验证权限：只有学生本人可以取消
        Long currentStudentId = SecurityUtils.getCurrentStudentId();
        if (!request.getStudent().getId().equals(currentStudentId)) {
            throw new AttendanceException(ErrorCode.FORBIDDEN, "您无权取消此申请");
        }

        // 验证状态：只能取消待审批的申请
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new AttendanceException(ErrorCode.INVALID_OPERATION, "只能取消待审批的申请");
        }

        requestRepository.delete(request);
        log.info("申请已取消: requestId={}", requestId);
    }

    /**
     * 提交申请的通用方法
     */
    private AttendanceRequest submitRequest(Long studentId, Long detailId, 
                                           RequestType requestType, String reason, String attachmentUrl) {
        // 验证参数
        if (reason == null || reason.trim().isEmpty()) {
            throw new AttendanceException(ErrorCode.PARAM_ERROR, "申请原因不能为空");
        }

        // 获取考勤明细
        AttendanceDetail detail = detailRepository.findById(detailId)
                .orElseThrow(() -> new AttendanceException(ErrorCode.ATTENDANCE_DETAIL_NOT_FOUND));

        // 验证权限：必须是本人的考勤记录
        if (!detail.getStudent().getId().equals(studentId)) {
            throw new AttendanceException(ErrorCode.FORBIDDEN, "您只能对自己的考勤记录提交申请");
        }

        // 检查是否已存在待处理或已批准的申请
        boolean exists = requestRepository.existsByStudentAndDetail(studentId, detailId);
        if (exists) {
            throw new AttendanceException(ErrorCode.DATA_ALREADY_EXISTS, 
                    "该考勤记录已存在待处理或已批准的申请");
        }

        // 创建申请
        AttendanceRequest request = AttendanceRequest.builder()
                .requestType(requestType)
                .student(studentRepository.findById(studentId).orElseThrow())
                .attendanceDetail(detail)
                .reason(reason)
                .attachmentUrl(attachmentUrl)
                .status(RequestStatus.PENDING)
                .build();

        request = requestRepository.save(request);
        
        log.info("申请提交成功: requestId={}, type={}", request.getId(), requestType);
        
        // 发送新申请通知给教师
        notificationService.sendNewRequestNotification(request);

        return request;
    }

    /**
     * 批准后更新统计数据
     */
    private void updateStatisticsAfterApproval(AttendanceDetail detail, AttendanceStatus oldStatus) {
        Long studentId = detail.getStudent().getId();
        Long offeringId = detail.getAttendanceRecord().getOffering().getId();

        AttendanceStatistics stats = statisticsRepository
                .findByStudentIdAndOfferingId(studentId, offeringId)
                .orElse(null);

        if (stats == null) {
            return;
        }

        // 减少旧状态的计数
        decrementStatusCount(stats, oldStatus);

        // 增加新状态的计数
        incrementStatusCount(stats, detail.getStatus());

        // 重新计算出勤率
        if (stats.getTotalClasses() > 0) {
            double rate = (stats.getPresentCount() + stats.getLateCount()) * 100.0 
                    / stats.getTotalClasses();
            stats.setAttendanceRate(Math.round(rate * 100.0) / 100.0);
        }

        stats.setLastUpdated(LocalDateTime.now());
        statisticsRepository.save(stats);

        log.info("统计数据已更新: studentId={}, offeringId={}", studentId, offeringId);
    }

    private void decrementStatusCount(AttendanceStatistics stats, AttendanceStatus status) {
        switch (status) {
            case PRESENT -> stats.setPresentCount(Math.max(0, stats.getPresentCount() - 1));
            case LATE -> stats.setLateCount(Math.max(0, stats.getLateCount() - 1));
            case EARLY_LEAVE -> stats.setEarlyLeaveCount(Math.max(0, stats.getEarlyLeaveCount() - 1));
            case LEAVE -> stats.setLeaveCount(Math.max(0, stats.getLeaveCount() - 1));
            case ABSENT -> stats.setAbsentCount(Math.max(0, stats.getAbsentCount() - 1));
        }
    }

    private void incrementStatusCount(AttendanceStatistics stats, AttendanceStatus status) {
        switch (status) {
            case PRESENT -> stats.setPresentCount(stats.getPresentCount() + 1);
            case LATE -> stats.setLateCount(stats.getLateCount() + 1);
            case EARLY_LEAVE -> stats.setEarlyLeaveCount(stats.getEarlyLeaveCount() + 1);
            case LEAVE -> stats.setLeaveCount(stats.getLeaveCount() + 1);
            case ABSENT -> stats.setAbsentCount(stats.getAbsentCount() + 1);
        }
    }
}

