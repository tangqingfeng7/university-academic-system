package com.university.ems.controller;

import com.university.academic.entity.Teacher;
import com.university.academic.entity.User;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.TeacherRepository;
import com.university.academic.repository.UserRepository;
import com.university.academic.vo.Result;
import com.university.ems.dto.SetPreferenceRequest;
import com.university.ems.dto.TeacherPreferenceDTO;
import com.university.ems.dto.UpdatePreferenceRequest;
import com.university.ems.service.TeacherPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 教师端排课优化Controller
 * 提供教师偏好设置和查询功能
 */
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('TEACHER')")
public class TeacherSchedulingController {
    
    private final TeacherPreferenceService preferenceService;
    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    
    /**
     * 设置排课偏好
     */
    @PostMapping("/preferences")
    public Result<TeacherPreferenceDTO> setPreference(
            @Valid @RequestBody SetPreferenceRequest request,
            Authentication authentication) {
        Long teacherId = extractTeacherId(authentication);
        log.info("教师设置排课偏好: teacherId={}", teacherId);
        request.setTeacherId(teacherId); // 设置教师ID到请求中
        TeacherPreferenceDTO preference = preferenceService.setPreference(request);
        return Result.success(preference);
    }
    
    /**
     * 更新排课偏好
     */
    @PutMapping("/preferences")
    public Result<TeacherPreferenceDTO> updatePreference(
            @Valid @RequestBody UpdatePreferenceRequest request,
            Authentication authentication) {
        Long teacherId = extractTeacherId(authentication);
        log.info("教师更新排课偏好: teacherId={}", teacherId);
        TeacherPreferenceDTO preference = preferenceService.updatePreference(teacherId, request);
        return Result.success(preference);
    }
    
    /**
     * 查询我的排课偏好
     */
    @GetMapping("/preferences")
    public Result<TeacherPreferenceDTO> getMyPreference(Authentication authentication) {
        Long teacherId = extractTeacherId(authentication);
        log.info("查询教师排课偏好: teacherId={}", teacherId);
        TeacherPreferenceDTO preference = preferenceService.getPreferenceByTeacherId(teacherId);
        return Result.success(preference);
    }
    
    /**
     * 删除排课偏好
     */
    @DeleteMapping("/preferences")
    public Result<Void> deletePreference(Authentication authentication) {
        Long teacherId = extractTeacherId(authentication);
        log.info("删除教师排课偏好: teacherId={}", teacherId);
        preferenceService.deletePreference(teacherId);
        return Result.success(null);
    }
    
    /**
     * 从Authentication中提取教师ID
     */
    private Long extractTeacherId(Authentication authentication) {
        String username = authentication.getName();
        
        // 根据用户名查询用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        // 根据用户ID查询教师
        Teacher teacher = teacherRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_NOT_FOUND));
        
        return teacher.getId();
    }
}

