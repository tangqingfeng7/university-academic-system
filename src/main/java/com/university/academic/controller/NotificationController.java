package com.university.academic.controller;

import com.university.academic.dto.NotificationDTO;
import com.university.academic.security.CustomUserDetailsService;
import com.university.academic.service.NotificationService;
import com.university.academic.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知查询控制器
 * 提供通知查询和已读标记功能（所有角色通用）
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final CustomUserDetailsService userDetailsService;

    /**
     * 获取我的通知列表（根据角色自动筛选）
     */
    @GetMapping
    public Result<Page<NotificationDTO>> getMyNotifications(
            @PageableDefault(size = 20, sort = "publishTime", direction = Sort.Direction.DESC) 
            Pageable pageable,
            Authentication authentication) {
        Long userId = userDetailsService.getUserIdFromAuth(authentication);
        String role = userDetailsService.getRoleFromAuth(authentication);
        
        log.info("查询通知列表: userId={}, role={}", userId, role);

        Page<NotificationDTO> page = notificationService
                .findByTargetRole(role, userId, pageable);
        return Result.success(page);
    }

    /**
     * 获取通知详情
     */
    @GetMapping("/{id}")
    public Result<NotificationDTO> getNotification(@PathVariable Long id,
                                                   Authentication authentication) {
        Long userId = userDetailsService.getUserIdFromAuth(authentication);
        log.info("查询通知详情: id={}, userId={}", id, userId);

        var notification = notificationService.findById(id);
        NotificationDTO dto = notificationService
                .findByTargetRole(userDetailsService.getRoleFromAuth(authentication), 
                        userId, Pageable.ofSize(1))
                .getContent().stream()
                .filter(n -> n.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (dto == null) {
            // 如果在分页结果中找不到，直接转换
            dto = NotificationDTO.builder()
                    .id(notification.getId())
                    .title(notification.getTitle())
                    .content(notification.getContent())
                    .type(notification.getType().name())
                    .typeDescription(notification.getType().getDescription())
                    .publisherId(notification.getPublisher().getId())
                    .publisherName(notification.getPublisher().getUsername())
                    .targetRole(notification.getTargetRole())
                    .publishTime(notification.getPublishTime())
                    .active(notification.getActive())
                    .createdAt(notification.getCreatedAt())
                    .read(false)
                    .build();
        }

        return Result.success(dto);
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    public Result<String> markAsRead(@PathVariable Long id,
                                    Authentication authentication) {
        Long userId = userDetailsService.getUserIdFromAuth(authentication);
        log.info("标记通知已读: id={}, userId={}", id, userId);

        notificationService.markAsRead(id, userId);
        return Result.success("标记已读成功");
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread/count")
    public Result<Map<String, Object>> getUnreadCount(Authentication authentication) {
        Long userId = userDetailsService.getUserIdFromAuth(authentication);
        String role = userDetailsService.getRoleFromAuth(authentication);
        
        log.info("查询未读通知数量: userId={}, role={}", userId, role);

        long count = notificationService.countUnread(userId, role);
        
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        
        return Result.success(result);
    }

    /**
     * 获取最新通知列表（首页展示用）
     */
    @GetMapping("/latest")
    public Result<List<NotificationDTO>> getLatestNotifications(
            @RequestParam(defaultValue = "5") int limit,
            Authentication authentication) {
        Long userId = userDetailsService.getUserIdFromAuth(authentication);
        String role = userDetailsService.getRoleFromAuth(authentication);
        
        log.info("查询最新通知: userId={}, role={}, limit={}", userId, role, limit);

        List<NotificationDTO> notifications = notificationService
                .findLatest(role, userId, limit);
        return Result.success(notifications);
    }
}

