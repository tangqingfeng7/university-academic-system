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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 教师通知控制器
 * 提供教师发布课程通知功能
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherNotificationController {

    private final NotificationService notificationService;
    private final CustomUserDetailsService userDetailsService;
    private final DtoConverter dtoConverter;

    /**
     * 发布课程通知
     */
    @PostMapping
    public Result<NotificationDTO> publishNotification(
            @Valid @RequestBody CreateNotificationRequest request,
            Authentication authentication) {
        Long userId = userDetailsService.getUserIdFromAuth(authentication);
        log.info("教师发布通知: userId={}, title={}", userId, request.getTitle());

        // 教师发布的通知默认类型为COURSE，目标角色为STUDENT
        if (request.getType() == null) {
            request.setType("COURSE");
        }
        if (request.getTargetRole() == null) {
            request.setTargetRole("STUDENT");
        }

        Notification notification = notificationService.publishNotification(request, userId);
        NotificationDTO dto = dtoConverter.toNotificationDTO(notification);

        return Result.success("通知发布成功", dto);
    }
}

