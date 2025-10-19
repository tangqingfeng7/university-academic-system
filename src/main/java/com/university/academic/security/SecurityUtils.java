package com.university.academic.security;

import com.university.academic.entity.Student;
import com.university.academic.entity.Teacher;
import com.university.academic.entity.User;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.StudentRepository;
import com.university.academic.repository.TeacherRepository;
import com.university.academic.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Security工具类
 *
 * @author university
 * @since 2024-01-01
 */
@Slf4j
@Component
public class SecurityUtils {

    private static UserRepository userRepository;
    private static TeacherRepository teacherRepository;
    private static StudentRepository studentRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        SecurityUtils.userRepository = userRepository;
    }

    @Autowired
    public void setTeacherRepository(TeacherRepository teacherRepository) {
        SecurityUtils.teacherRepository = teacherRepository;
    }

    @Autowired
    public void setStudentRepository(StudentRepository studentRepository) {
        SecurityUtils.studentRepository = studentRepository;
    }

    private SecurityUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        String username = getCurrentUsername();
        if (username == null) {
            return null;
        }

        // 根据用户名查询用户ID
        if (userRepository != null) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                return userOpt.get().getId();
            } else {
                log.warn("未找到用户: username={}", username);
            }
        }

        return null;
    }

    /**
     * 获取当前登录教师ID
     *
     * @return 教师ID
     */
    public static Long getCurrentTeacherId() {
        String username = getCurrentUsername();
        if (username == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 根据用户名查询用户
        if (userRepository != null && teacherRepository != null) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // 查询教师信息
                Optional<Teacher> teacherOpt = teacherRepository.findByUserId(user.getId());
                if (teacherOpt.isPresent()) {
                    return teacherOpt.get().getId();
                } else {
                    log.warn("用户不是教师: username={}, userId={}", username, user.getId());
                    throw new BusinessException(ErrorCode.TEACHER_NOT_FOUND);
                }
            } else {
                log.warn("未找到用户: username={}", username);
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            }
        }

        throw new BusinessException(ErrorCode.SYSTEM_ERROR);
    }

    /**
     * 获取当前登录学生ID
     *
     * @return 学生ID
     */
    public static Long getCurrentStudentId() {
        String username = getCurrentUsername();
        if (username == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 根据用户名查询用户
        if (userRepository != null && studentRepository != null) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // 查询学生信息
                Optional<Student> studentOpt = studentRepository.findByUserId(user.getId());
                if (studentOpt.isPresent()) {
                    return studentOpt.get().getId();
                } else {
                    log.warn("用户不是学生: username={}, userId={}", username, user.getId());
                    throw new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
                }
            } else {
                log.warn("未找到用户: username={}", username);
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            }
        }

        throw new BusinessException(ErrorCode.SYSTEM_ERROR);
    }

    /**
     * 获取当前登录用户名
     *
     * @return 用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }

        return null;
    }

    /**
     * 获取当前认证对象
     *
     * @return Authentication
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 检查当前用户是否已认证
     *
     * @return 是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}

