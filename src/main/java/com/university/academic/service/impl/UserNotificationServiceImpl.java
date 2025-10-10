package com.university.academic.service.impl;

import com.university.academic.entity.User;
import com.university.academic.entity.UserNotification;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.UserNotificationRepository;
import com.university.academic.repository.UserRepository;
import com.university.academic.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户专属通知Service实现类
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserNotificationServiceImpl implements UserNotificationService {

    private final UserNotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserNotification sendToUser(Long userId, String title, String content,
                                      String type, String referenceType, Long referenceId) {
        log.info("发送专属通知给用户: userId={}, title={}", userId, title);

        // 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 创建通知
        UserNotification notification = UserNotification.builder()
                .user(user)
                .title(title)
                .content(content)
                .type(type)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .isRead(false)
                .sentTime(LocalDateTime.now())
                .build();

        UserNotification saved = notificationRepository.save(notification);
        log.info("通知发送成功: notificationId={}, userId={}", saved.getId(), userId);

        return saved;
    }

    @Override
    @Transactional
    public int sendToUsers(List<Long> userIds, String title, String content,
                          String type, String referenceType, Long referenceId) {
        log.info("批量发送通知: userCount={}, title={}", userIds.size(), title);

        int sentCount = 0;
        for (Long userId : userIds) {
            try {
                sendToUser(userId, title, content, type, referenceType, referenceId);
                sentCount++;
            } catch (Exception e) {
                log.error("发送通知失败: userId={}", userId, e);
            }
        }

        log.info("批量通知发送完成: 成功={}, 总数={}", sentCount, userIds.size());
        return sentCount;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserNotification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserNotification> getUnreadNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findUnreadByUserId(userId, pageable);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        UserNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // 验证通知归属
        if (!notification.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadTime(LocalDateTime.now());
            notificationRepository.save(notification);
            log.info("通知已标记为已读: notificationId={}, userId={}", notificationId, userId);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        Page<UserNotification> unreadPage = notificationRepository
                .findUnreadByUserId(userId, Pageable.unpaged());

        int count = 0;
        LocalDateTime now = LocalDateTime.now();
        for (UserNotification notification : unreadPage.getContent()) {
            notification.setIsRead(true);
            notification.setReadTime(now);
            notificationRepository.save(notification);
            count++;
        }

        log.info("批量标记通知为已读: userId={}, count={}", userId, count);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }
}

