package com.university.academic.service;

import com.university.academic.dto.CreateNotificationRequest;
import com.university.academic.dto.NotificationDTO;
import com.university.academic.entity.Notification;
import com.university.academic.entity.NotificationRead;
import com.university.academic.entity.User;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.NotificationReadRepository;
import com.university.academic.repository.NotificationRepository;
import com.university.academic.repository.UserRepository;
import com.university.academic.util.DtoConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 通知公告服务类
 * 提供通知发布、查询、已读标记等功能
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationReadRepository readRepository;
    private final UserRepository userRepository;
    private final DtoConverter dtoConverter;

    /**
     * 发布通知
     *
     * @param request     通知内容
     * @param publisherId 发布者ID
     * @return 通知对象
     */
    @Transactional
    public Notification publishNotification(CreateNotificationRequest request, Long publisherId) {
        log.info("发布通知: title={}, publisherId={}", request.getTitle(), publisherId);

        // 验证发布者存在
        User publisher = userRepository.findById(publisherId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 验证通知类型
        Notification.NotificationType type;
        try {
            type = Notification.NotificationType.valueOf(request.getType());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 创建通知
        Notification notification = Notification.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .type(type)
                .publisher(publisher)
                .targetRole(request.getTargetRole() != null ? request.getTargetRole() : "ALL")
                .active(true)
                .build();

        notification = notificationRepository.save(notification);
        log.info("通知发布成功: notificationId={}", notification.getId());

        return notification;
    }

    /**
     * 查询通知列表（分页）
     *
     * @param pageable 分页参数
     * @return 通知分页数据
     */
    @Transactional(readOnly = true)
    public Page<NotificationDTO> findAll(Pageable pageable) {
        Page<Notification> page = notificationRepository
                .findByActiveTrueOrderByPublishTimeDesc(pageable);
        
        return page.map(dtoConverter::toNotificationDTO);
    }

    /**
     * 根据角色查询通知列表（包含自己发布的通知）
     *
     * @param targetRole 目标角色
     * @param userId     用户ID（用于判断已读状态和查询自己发布的通知）
     * @param pageable   分页参数
     * @return 通知分页数据
     */
    @Transactional(readOnly = true)
    public Page<NotificationDTO> findByTargetRole(String targetRole, Long userId, 
                                                   Pageable pageable) {
        log.info("查询通知列表: targetRole={}, userId={}", targetRole, userId);

        // 使用新的查询方法，包含自己发布的通知
        Page<Notification> page = notificationRepository
                .findByTargetRoleOrPublisher(targetRole, userId, pageable);

        // 获取用户已读的通知ID列表
        Set<Long> readIds = readRepository.findReadNotificationIdsByUserId(userId)
                .stream().collect(Collectors.toSet());

        // 转换为DTO并设置已读状态
        List<NotificationDTO> dtoList = page.getContent().stream()
                .map(notification -> {
                    NotificationDTO dto = dtoConverter.toNotificationDTO(notification);
                    dto.setRead(readIds.contains(notification.getId()));
                    return dto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

    /**
     * 根据ID查询通知详情
     *
     * @param id 通知ID
     * @return 通知对象
     */
    @Transactional(readOnly = true)
    public Notification findById(Long id) {
        return notificationRepository.findByIdWithPublisher(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
    }

    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     * @param userId         用户ID
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        log.info("标记通知已读: notificationId={}, userId={}", notificationId, userId);

        // 验证通知存在（使用预加载版本）
        Notification notification = notificationRepository.findByIdWithPublisher(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // 检查是否已读
        if (readRepository.existsByNotificationIdAndUserId(notificationId, userId)) {
            throw new BusinessException(ErrorCode.NOTIFICATION_ALREADY_READ);
        }

        // 验证用户存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 创建已读记录
        NotificationRead readRecord = NotificationRead.builder()
                .notification(notification)
                .user(user)
                .build();

        readRepository.save(readRecord);
        log.info("标记已读成功");
    }

    /**
     * 获取未读通知数量
     *
     * @param userId     用户ID
     * @param targetRole 目标角色
     * @return 未读通知数量
     */
    @Transactional(readOnly = true)
    public long countUnread(Long userId, String targetRole) {
        return readRepository.countUnreadByUserId(userId, targetRole);
    }

    /**
     * 获取最新通知列表（包含自己发布的通知）
     *
     * @param targetRole 目标角色
     * @param userId     用户ID
     * @param limit      数量限制
     * @return 通知列表
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> findLatest(String targetRole, Long userId, int limit) {
        // 使用新的查询方法，包含自己发布的通知
        List<Notification> notifications = notificationRepository
                .findLatestByTargetRoleOrPublisher(targetRole, userId, Pageable.ofSize(limit));

        // 获取用户已读的通知ID列表
        Set<Long> readIds = readRepository.findReadNotificationIdsByUserId(userId)
                .stream().collect(Collectors.toSet());

        return notifications.stream()
                .map(notification -> {
                    NotificationDTO dto = dtoConverter.toNotificationDTO(notification);
                    dto.setRead(readIds.contains(notification.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * 停用通知
     *
     * @param id 通知ID
     */
    @Transactional
    public void deactivate(Long id) {
        log.info("停用通知: id={}", id);

        Notification notification = notificationRepository.findByIdWithPublisher(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        notification.setActive(false);
        notificationRepository.save(notification);

        log.info("通知已停用");
    }
}

