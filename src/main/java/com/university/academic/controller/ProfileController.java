package com.university.academic.controller;

import com.university.academic.dto.StudentDTO;
import com.university.academic.dto.TeacherDTO;
import com.university.academic.entity.Student;
import com.university.academic.entity.Teacher;
import com.university.academic.entity.User;
import com.university.academic.repository.UserRepository;
import com.university.academic.security.CustomUserDetailsService;
import com.university.academic.service.StudentService;
import com.university.academic.service.TeacherService;
import com.university.academic.util.DtoConverter;
import com.university.academic.vo.Result;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 个人信息管理控制器
 * 提供查看和更新个人信息接口（所有角色）
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final StudentService studentService;
    private final TeacherService teacherService;
    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;
    private final DtoConverter dtoConverter;

    /**
     * 获取个人信息
     */
    @GetMapping
    public Result<?> getProfile(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        log.info("查询个人信息: username={}, role={}", username, user.getRole());

        switch (user.getRole()) {
            case STUDENT:
                Student student = studentService.findByUserIdWithDetails(user.getId());
                StudentDTO studentDTO = dtoConverter.toStudentDTO(student);
                return Result.success(studentDTO);
                
            case TEACHER:
                Teacher teacher = teacherService.findByUserIdWithDetails(user.getId());
                TeacherDTO teacherDTO = dtoConverter.toTeacherDTO(teacher);
                return Result.success(teacherDTO);
                
            default:
                // 管理员只返回基本用户信息
                return Result.success(Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "role", user.getRole().name()
                ));
        }
    }

    /**
     * 更新个人信息
     */
    @PutMapping
    public Result<?> updateProfile(@RequestBody Map<String, String> updates,
                                    Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        log.info("更新个人信息: username={}, role={}", username, user.getRole());

        switch (user.getRole()) {
            case STUDENT:
                Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
                Student student = studentService.findById(studentId);
                
                // 只允许更新联系方式
                if (updates.containsKey("phone")) {
                    student.setPhone(updates.get("phone"));
                }
                
                Student updatedStudent = studentService.updateStudent(studentId, student);
                StudentDTO studentDTO = dtoConverter.toStudentDTO(updatedStudent);
                return Result.success("个人信息更新成功", studentDTO);
                
            case TEACHER:
                Long teacherId = userDetailsService.getTeacherIdFromAuth(authentication);
                Teacher teacher = teacherService.findById(teacherId);
                
                // 只允许更新联系方式
                if (updates.containsKey("phone")) {
                    teacher.setPhone(updates.get("phone"));
                }
                if (updates.containsKey("email")) {
                    teacher.setEmail(updates.get("email"));
                }
                
                Teacher updatedTeacher = teacherService.updateTeacher(teacherId, teacher);
                TeacherDTO teacherDTO = dtoConverter.toTeacherDTO(updatedTeacher);
                return Result.success("个人信息更新成功", teacherDTO);
                
            default:
                throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}

