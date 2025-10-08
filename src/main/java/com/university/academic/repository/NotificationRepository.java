package com.university.academic.repository;

import com.university.academic.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 通知公告数据访问接口
 * 提供通知数据的CRUD操作
 *
 * @author Academic System Team
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 根据ID查询通知（预加载发布者信息）
     *
     * @param id 通知ID
     * @return 通知对象
     */
    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.publisher WHERE n.id = :id")
    Optional<Notification> findByIdWithPublisher(@Param("id") Long id);

    /**
     * 查询有效的通知（按发布时间倒序）
     *
     * @param pageable 分页参数
     * @return 通知分页数据
     */
    Page<Notification> findByActiveTrueOrderByPublishTimeDesc(Pageable pageable);

    /**
     * 根据目标角色查询通知
     *
     * @param targetRole 目标角色
     * @param pageable   分页参数
     * @return 通知分页数据
     */
    @Query("SELECT n FROM Notification n WHERE " +
            "n.active = true AND " +
            "(n.targetRole = :targetRole OR n.targetRole = 'ALL') " +
            "ORDER BY n.publishTime DESC")
    Page<Notification> findByTargetRole(@Param("targetRole") String targetRole, 
                                        Pageable pageable);

    /**
     * 根据目标角色或发布者查询通知（包含自己发布的通知）
     *
     * @param targetRole 目标角色
     * @param userId     用户ID
     * @param pageable   分页参数
     * @return 通知分页数据
     */
    @Query("SELECT n FROM Notification n WHERE " +
            "n.active = true AND " +
            "(n.targetRole = :targetRole OR n.targetRole = 'ALL' OR n.publisher.id = :userId) " +
            "ORDER BY n.publishTime DESC")
    Page<Notification> findByTargetRoleOrPublisher(@Param("targetRole") String targetRole,
                                                    @Param("userId") Long userId,
                                                    Pageable pageable);

    /**
     * 根据类型查询通知
     *
     * @param type     通知类型
     * @param pageable 分页参数
     * @return 通知分页数据
     */
    Page<Notification> findByTypeAndActiveTrueOrderByPublishTimeDesc(
            Notification.NotificationType type, Pageable pageable);

    /**
     * 查询指定角色的最新通知
     *
     * @param targetRole 目标角色
     * @param limit      数量限制
     * @return 通知列表
     */
    @Query(value = "SELECT n FROM Notification n WHERE " +
            "n.active = true AND " +
            "(n.targetRole = :targetRole OR n.targetRole = 'ALL') " +
            "ORDER BY n.publishTime DESC")
    List<Notification> findLatestByTargetRole(@Param("targetRole") String targetRole, 
                                              Pageable pageable);

    /**
     * 查询指定角色或发布者的最新通知（包含自己发布的）
     *
     * @param targetRole 目标角色
     * @param userId     用户ID
     * @param pageable   分页参数
     * @return 通知列表
     */
    @Query(value = "SELECT n FROM Notification n WHERE " +
            "n.active = true AND " +
            "(n.targetRole = :targetRole OR n.targetRole = 'ALL' OR n.publisher.id = :userId) " +
            "ORDER BY n.publishTime DESC")
    List<Notification> findLatestByTargetRoleOrPublisher(@Param("targetRole") String targetRole,
                                                         @Param("userId") Long userId,
                                                         Pageable pageable);

    /**
     * 统计有效通知数量
     *
     * @return 通知总数
     */
    long countByActiveTrue();
}

