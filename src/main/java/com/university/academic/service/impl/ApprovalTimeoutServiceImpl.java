package com.university.academic.service.impl;

import com.university.academic.dto.CreateNotificationRequest;
import com.university.academic.entity.ApprovalStatus;
import com.university.academic.entity.StudentStatusChange;
import com.university.academic.entity.User;
import com.university.academic.repository.StudentStatusChangeRepository;
import com.university.academic.repository.UserRepository;
import com.university.academic.service.ApprovalTimeoutService;
import com.university.academic.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 审批超时检查Service实现类
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalTimeoutServiceImpl implements ApprovalTimeoutService {

    private final StudentStatusChangeRepository statusChangeRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public int checkAndMarkOverdueApplications() {
        log.info("开始检查超时的审批申请");

        // 查询所有待审批且未标记超时的申请
        Page<StudentStatusChange> pendingPage = statusChangeRepository
                .findByStatus(ApprovalStatus.PENDING, PageRequest.of(0, 1000));

        int overdueCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (StudentStatusChange statusChange : pendingPage.getContent()) {
            // 检查是否已超时
            if (!statusChange.getIsOverdue() &&
                    statusChange.getDeadline() != null &&
                    statusChange.getDeadline().isBefore(now)) {

                // 标记为超时
                statusChange.setIsOverdue(true);
                statusChangeRepository.save(statusChange);
                overdueCount++;

                log.warn("审批申请已超时: ID={}, 学生={}, 截止时间={}, 当前时间={}",
                        statusChange.getId(),
                        statusChange.getStudent().getName(),
                        statusChange.getDeadline(),
                        now);
            }
        }

        log.info("超时检查完成，共标记{}个超时申请", overdueCount);
        return overdueCount;
    }

    @Override
    @Transactional
    public int sendOverdueReminders() {
        log.info("开始发送超时提醒通知");

        // 查询所有超时的待审批申请
        Page<StudentStatusChange> pendingPage = statusChangeRepository
                .findByStatus(ApprovalStatus.PENDING, PageRequest.of(0, 1000));

        int reminderCount = 0;

        for (StudentStatusChange statusChange : pendingPage.getContent()) {
            if (statusChange.getIsOverdue()) {
                // 发送超时提醒
                sendOverdueNotification(statusChange);
                reminderCount++;
            }
        }

        log.info("超时提醒发送完成，共发送{}条通知", reminderCount);
        return reminderCount;
    }

    @Override
    @Transactional
    public int sendUpcomingDeadlineReminders() {
        log.info("开始发送即将超时提醒通知");

        // 查询所有待审批的申请
        Page<StudentStatusChange> pendingPage = statusChangeRepository
                .findByStatus(ApprovalStatus.PENDING, PageRequest.of(0, 1000));

        int reminderCount = 0;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);

        for (StudentStatusChange statusChange : pendingPage.getContent()) {
            // 检查是否在24小时内到期
            if (statusChange.getDeadline() != null &&
                    statusChange.getDeadline().isAfter(now) &&
                    statusChange.getDeadline().isBefore(tomorrow)) {

                // 发送即将超时提醒
                sendUpcomingDeadlineNotification(statusChange);
                reminderCount++;
            }
        }

        log.info("即将超时提醒发送完成，共发送{}条通知", reminderCount);
        return reminderCount;
    }

    /**
     * 发送超时通知
     *
     * @param statusChange 异动申请
     */
    private void sendOverdueNotification(StudentStatusChange statusChange) {
        try {
            String title = "学籍异动申请审批超时提醒";
            String content = String.format(
                    "您有一个学籍异动申请已超时未审批：\n\n" +
                            "申请编号：%d\n" +
                            "学生：%s\n" +
                            "异动类型：%s\n" +
                            "截止时间：%s\n" +
                            "当前审批级别：%s\n\n" +
                            "请尽快处理该申请。",
                    statusChange.getId(),
                    statusChange.getStudent().getName(),
                    statusChange.getType().getDescription(),
                    statusChange.getDeadline(),
                    getApprovalLevelName(statusChange.getApprovalLevel())
            );

            CreateNotificationRequest request = CreateNotificationRequest.builder()
                    .title(title)
                    .content(content)
                    .type("SYSTEM")
                    .targetRole("ADMIN")
                    .build();

            // 使用第一个管理员用户作为系统通知发布者
            Long publisherId = getSystemPublisherId();
            notificationService.publishNotification(request, publisherId);

            log.info("超时通知已发送: 申请ID={}", statusChange.getId());

        } catch (Exception e) {
            log.error("发送超时通知失败: 申请ID={}", statusChange.getId(), e);
        }
    }

    /**
     * 发送即将超时通知
     *
     * @param statusChange 异动申请
     */
    private void sendUpcomingDeadlineNotification(StudentStatusChange statusChange) {
        try {
            String title = "学籍异动申请即将超时提醒";
            String content = String.format(
                    "您有一个学籍异动申请即将超时：\n\n" +
                            "申请编号：%d\n" +
                            "学生：%s\n" +
                            "异动类型：%s\n" +
                            "截止时间：%s\n" +
                            "当前审批级别：%s\n\n" +
                            "请及时处理该申请。",
                    statusChange.getId(),
                    statusChange.getStudent().getName(),
                    statusChange.getType().getDescription(),
                    statusChange.getDeadline(),
                    getApprovalLevelName(statusChange.getApprovalLevel())
            );

            CreateNotificationRequest request = CreateNotificationRequest.builder()
                    .title(title)
                    .content(content)
                    .type("SYSTEM")
                    .targetRole("ADMIN")
                    .build();

            // 使用第一个管理员用户作为系统通知发布者
            Long publisherId = getSystemPublisherId();
            notificationService.publishNotification(request, publisherId);

            log.info("即将超时通知已发送: 申请ID={}", statusChange.getId());

        } catch (Exception e) {
            log.error("发送即将超时通知失败: 申请ID={}", statusChange.getId(), e);
        }
    }

    /**
     * 获取审批级别名称
     *
     * @param level 审批级别
     * @return 级别名称
     */
    private String getApprovalLevelName(Integer level) {
        return switch (level) {
            case 1 -> "辅导员";
            case 2 -> "院系";
            case 3 -> "教务处";
            default -> "未知";
        };
    }

    /**
     * 获取系统通知发布者ID
     * 使用第一个管理员用户作为系统通知发布者
     *
     * @return 发布者ID
     */
    private Long getSystemPublisherId() {
        return userRepository.findFirstAdmin()
                .map(User::getId)
                .orElseGet(() -> {
                    log.warn("未找到管理员用户，使用默认ID: 1");
                    return 1L;
                });
    }
}

