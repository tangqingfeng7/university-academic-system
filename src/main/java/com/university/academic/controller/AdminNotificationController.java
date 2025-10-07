package com.university.academic.controller;

import com.university.academic.dto.CreateNotificationRequest;
import com.university.academic.dto.NotificationDTO;
import com.university.academic.entity.Notification;
import com.university.academic.security.CustomUserDetailsService;
import com.university.academic.service.NotificationService;
import com.university.academic.util.DtoConverter;
import com.university.academic.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员通知控制器
 * 提供管理员发布系统通知功能
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationController {

    private final NotificationService notificationService;
    private final CustomUserDetailsService userDetailsService;
    private final DtoConverter dtoConverter;

    /**
     * 发布系统通知
     */
    @PostMapping
    public Result<NotificationDTO> publishNotification(
            @Valid @RequestBody CreateNotificationRequest request,
            Authentication authentication) {
        Long userId = userDetailsService.getUserIdFromAuth(authentication);
        log.info("管理员发布通知: userId={}, title={}", userId, request.getTitle());

        Notification notification = notificationService.publishNotification(request, userId);
        NotificationDTO dto = dtoConverter.toNotificationDTO(notification);

        return Result.success("通知发布成功", dto);
    }

    /**
     * 查询所有通知列表
     */
    @GetMapping
    public Result<Page<NotificationDTO>> getAllNotifications(
            @PageableDefault(size = 20, sort = "publishTime", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        log.info("管理员查询通知列表");

        Page<NotificationDTO> page = notificationService.findAll(pageable);
        return Result.success(page);
    }

    /**
     * 停用通知
     */
    @PutMapping("/{id}/deactivate")
    public Result<String> deactivateNotification(@PathVariable Long id) {
        log.info("停用通知: id={}", id);

        notificationService.deactivate(id);
        return Result.success("通知已停用");
    }
}

