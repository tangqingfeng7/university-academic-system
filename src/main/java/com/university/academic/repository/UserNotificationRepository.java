package com.university.academic.repository;

import com.university.academic.entity.UserNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户专属通知Repository接口
 *
 * @author Academic System Team
 */
@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    /**
     * 分页查询用户的通知列表
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 通知分页列表
     */
    @Query("SELECT un FROM UserNotification un WHERE un.user.id = :userId ORDER BY un.sentTime DESC")
    Page<UserNotification> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 查询用户未读通知列表
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 未读通知分页列表
     */
    @Query("SELECT un FROM UserNotification un WHERE un.user.id = :userId AND un.isRead = false ORDER BY un.sentTime DESC")
    Page<UserNotification> findUnreadByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 统计用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    @Query("SELECT COUNT(un) FROM UserNotification un WHERE un.user.id = :userId AND un.isRead = false")
    long countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 根据关联业务查询通知
     *
     * @param referenceType 关联业务类型
     * @param referenceId   关联业务ID
     * @return 通知列表
     */
    @Query("SELECT un FROM UserNotification un WHERE un.referenceType = :referenceType AND un.referenceId = :referenceId")
    List<UserNotification> findByReference(@Param("referenceType") String referenceType, 
                                           @Param("referenceId") Long referenceId);
}

