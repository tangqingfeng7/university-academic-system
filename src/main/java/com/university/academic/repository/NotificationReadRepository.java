package com.university.academic.repository;

import com.university.academic.entity.NotificationRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 通知阅读记录数据访问接口
 *
 * @author Academic System Team
 */
@Repository
public interface NotificationReadRepository extends JpaRepository<NotificationRead, Long> {

    /**
     * 查询用户是否已读某条通知
     *
     * @param notificationId 通知ID
     * @param userId         用户ID
     * @return 阅读记录
     */
    Optional<NotificationRead> findByNotificationIdAndUserId(Long notificationId, Long userId);

    /**
     * 检查用户是否已读某条通知
     *
     * @param notificationId 通知ID
     * @param userId         用户ID
     * @return true-已读，false-未读
     */
    boolean existsByNotificationIdAndUserId(Long notificationId, Long userId);

    /**
     * 查询用户已读的通知ID列表
     *
     * @param userId 用户ID
     * @return 通知ID列表
     */
    @Query("SELECT nr.notification.id FROM NotificationRead nr WHERE nr.user.id = :userId")
    List<Long> findReadNotificationIdsByUserId(@Param("userId") Long userId);

    /**
     * 统计用户未读通知数量（包含自己发布的通知）
     *
     * @param userId     用户ID
     * @param targetRole 目标角色
     * @return 未读通知数量
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE " +
            "n.active = true AND " +
            "(n.targetRole = :targetRole OR n.targetRole = 'ALL' OR n.publisher.id = :userId) AND " +
            "NOT EXISTS (SELECT 1 FROM NotificationRead nr WHERE " +
            "nr.notification.id = n.id AND nr.user.id = :userId)")
    long countUnreadByUserId(@Param("userId") Long userId, 
                            @Param("targetRole") String targetRole);
}

