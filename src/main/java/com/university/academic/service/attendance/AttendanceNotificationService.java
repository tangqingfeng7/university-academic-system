package com.university.academic.service.attendance;

import com.university.academic.entity.Notification;
import com.university.academic.entity.User;
import com.university.academic.entity.attendance.AttendanceRequest;
import com.university.academic.entity.attendance.AttendanceWarning;
import com.university.academic.repository.NotificationRepository;
import com.university.academic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考勤通知服务
 * 处理考勤相关的通知发送
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceNotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * 发送缺勤通知给学生
     *
     * @param studentUserIds 学生用户ID列表
     * @param courseName     课程名称
     * @param attendanceDate 考勤日期
     */
    @Transactional
    public void sendAbsenceNotification(List<Long> studentUserIds, String courseName, 
                                       String attendanceDate) {
        if (studentUserIds.isEmpty()) {
            return;
        }

        log.info("发送缺勤通知: 学生数量={}, 课程={}", studentUserIds.size(), courseName);

        // 获取系统用户作为发布者
        User systemUser = userRepository.findByUsername("admin")
                .orElseGet(() -> userRepository.findById(1L).orElse(null));

        if (systemUser == null) {
            log.warn("未找到系统用户，无法发送通知");
            return;
        }

        for (Long userId : studentUserIds) {
            try {
                String content = String.format("您在《%s》课程的%s考勤中缺勤，请注意按时上课。",
                        courseName, attendanceDate);

                Notification notification = Notification.builder()
                        .title("考勤缺勤提醒")
                        .content(content)
                        .type(Notification.NotificationType.ATTENDANCE)
                        .publisher(systemUser)
                        .targetRole("STUDENT")
                        .publishTime(LocalDateTime.now())
                        .active(true)
                        .build();

                notificationRepository.save(notification);
                log.debug("缺勤通知已发送: userId={}", userId);
            } catch (Exception e) {
                log.error("发送缺勤通知失败: userId={}", userId, e);
            }
        }
    }

    /**
     * 发送预警通知
     *
     * @param warning 预警记录
     */
    @Transactional
    public void sendWarningNotification(AttendanceWarning warning) {
        log.info("发送预警通知: warningId={}, type={}", warning.getId(), warning.getWarningType());

        User systemUser = userRepository.findByUsername("admin")
                .orElseGet(() -> userRepository.findById(1L).orElse(null));

        if (systemUser == null) {
            log.warn("未找到系统用户，无法发送预警通知");
            return;
        }

        try {
            Notification notification = Notification.builder()
                    .title("考勤预警 - " + warning.getWarningType().getDescription())
                    .content(warning.getWarningMessage())
                    .type(Notification.NotificationType.ATTENDANCE_WARNING)
                    .publisher(systemUser)
                    .targetRole(determineTargetRole(warning.getTargetType()))
                    .publishTime(LocalDateTime.now())
                    .active(true)
                    .build();

            notificationRepository.save(notification);
            log.info("预警通知已发送: warningId={}", warning.getId());
        } catch (Exception e) {
            log.error("发送预警通知失败: warningId={}", warning.getId(), e);
        }
    }

    /**
     * 发送申请审批结果通知
     *
     * @param request 申请记录
     */
    @Transactional
    public void sendApprovalResultNotification(AttendanceRequest request) {
        log.info("发送审批结果通知: requestId={}, status={}", request.getId(), request.getStatus());

        if (request.getStudent() == null || request.getStudent().getUser() == null) {
            log.warn("学生用户信息不完整，无法发送通知");
            return;
        }

        User approver = userRepository.findById(request.getApproverId()).orElse(null);
        if (approver == null) {
            log.warn("未找到审批人: approverId={}", request.getApproverId());
            return;
        }

        try {
            String title;
            String content;
            
            switch (request.getStatus()) {
                case APPROVED -> {
                    title = "考勤" + request.getRequestType().getDescription() + "已通过";
                    content = String.format("您的%s申请已被批准。审批意见：%s",
                            request.getRequestType().getDescription(),
                            request.getApprovalComment() != null ? request.getApprovalComment() : "无");
                }
                case REJECTED -> {
                    title = "考勤" + request.getRequestType().getDescription() + "被拒绝";
                    content = String.format("您的%s申请被拒绝。拒绝原因：%s",
                            request.getRequestType().getDescription(),
                            request.getApprovalComment() != null ? request.getApprovalComment() : "无");
                }
                default -> {
                    return;
                }
            }

            Notification notification = Notification.builder()
                    .title(title)
                    .content(content)
                    .type(Notification.NotificationType.ATTENDANCE_APPEAL)
                    .publisher(approver)
                    .targetRole("STUDENT")
                    .publishTime(LocalDateTime.now())
                    .active(true)
                    .build();

            notificationRepository.save(notification);
            log.info("审批结果通知已发送: requestId={}, studentId={}", 
                    request.getId(), request.getStudent().getId());
        } catch (Exception e) {
            log.error("发送审批结果通知失败: requestId={}", request.getId(), e);
        }
    }

    /**
     * 发送新申请通知给教师
     *
     * @param request 申请记录
     */
    @Transactional
    public void sendNewRequestNotification(AttendanceRequest request) {
        log.info("发送新申请通知: requestId={}, teacherId={}", 
                request.getId(), 
                request.getAttendanceDetail().getAttendanceRecord().getTeacher().getId());

        if (request.getStudent() == null || request.getStudent().getUser() == null) {
            log.warn("学生用户信息不完整，无法发送通知");
            return;
        }

        try {
            String content = String.format("学生%s提交了%s申请，请及时审批。",
                    request.getStudent().getName(),
                    request.getRequestType().getDescription());

            Notification notification = Notification.builder()
                    .title("考勤申请待审批")
                    .content(content)
                    .type(Notification.NotificationType.ATTENDANCE_APPEAL)
                    .publisher(request.getStudent().getUser())
                    .targetRole("TEACHER")
                    .publishTime(LocalDateTime.now())
                    .active(true)
                    .build();

            notificationRepository.save(notification);
            log.info("新申请通知已发送: requestId={}", request.getId());
        } catch (Exception e) {
            log.error("发送新申请通知失败: requestId={}", request.getId(), e);
        }
    }

    /**
     * 根据目标类型确定通知角色
     */
    private String determineTargetRole(String targetType) {
        return switch (targetType) {
            case "STUDENT" -> "STUDENT";
            case "TEACHER" -> "TEACHER";
            case "COURSE" -> "TEACHER";
            default -> "ALL";
        };
    }
}

