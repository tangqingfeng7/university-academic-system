package com.university.academic.service;

import com.university.academic.entity.UserNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 用户专属通知Service接口
 *
 * @author Academic System Team
 */
public interface UserNotificationService {

    /**
     * 发送通知给特定用户
     *
     * @param userId        接收用户ID
     * @param title         标题
     * @param content       内容
     * @param type          类型
     * @param referenceType 关联业务类型
     * @param referenceId   关联业务ID
     * @return 通知对象
     */
    UserNotification sendToUser(Long userId, String title, String content, 
                                String type, String referenceType, Long referenceId);

    /**
     * 批量发送通知给多个用户
     *
     * @param userIds       接收用户ID列表
     * @param title         标题
     * @param content       内容
     * @param type          类型
     * @param referenceType 关联业务类型
     * @param referenceId   关联业务ID
     * @return 发送的通知数量
     */
    int sendToUsers(List<Long> userIds, String title, String content, 
                   String type, String referenceType, Long referenceId);

    /**
     * 分页查询用户的通知列表
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 通知分页列表
     */
    Page<UserNotification> getUserNotifications(Long userId, Pageable pageable);

    /**
     * 查询用户未读通知
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 未读通知分页列表
     */
    Page<UserNotification> getUnreadNotifications(Long userId, Pageable pageable);

    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     * @param userId         用户ID
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 标记所有通知为已读
     *
     * @param userId 用户ID
     */
    void markAllAsRead(Long userId);

    /**
     * 统计未读通知数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    long countUnread(Long userId);
}

