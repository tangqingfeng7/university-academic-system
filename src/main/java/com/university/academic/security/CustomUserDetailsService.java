package com.university.academic.security;

import com.university.academic.entity.Student;
import com.university.academic.entity.Teacher;
import com.university.academic.entity.User;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.StudentRepository;
import com.university.academic.repository.TeacherRepository;
import com.university.academic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

/**
 * 自定义用户详情服务
 * 用于Spring Security从数据库加载用户信息
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("加载用户信息: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        if (!user.getEnabled()) {
            log.warn("用户已被禁用: {}", username);
        }

        return createUserDetails(user);
    }

    /**
     * 创建UserDetails对象
     *
     * @param user 用户实体
     * @return UserDetails对象
     */
    private UserDetails createUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(getAuthorities(user))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.getEnabled())
                .build();
    }

    /**
     * 获取用户权限
     *
     * @param user 用户实体
     * @return 权限集合
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        // 为角色添加ROLE_前缀（Spring Security要求）
        String role = "ROLE_" + user.getRole().name();
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    /**
     * 从Authentication中获取学生ID
     *
     * @param authentication 认证信息
     * @return 学生ID
     */
    @Transactional(readOnly = true)
    public Long getStudentIdFromAuth(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        
        return student.getId();
    }

    /**
     * 从Authentication中获取教师ID
     *
     * @param authentication 认证信息
     * @return 教师ID
     */
    @Transactional(readOnly = true)
    public Long getTeacherIdFromAuth(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Teacher teacher = teacherRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_NOT_FOUND));
        
        return teacher.getId();
    }

    /**
     * 从Authentication中获取用户ID
     *
     * @param authentication 认证信息
     * @return 用户ID
     */
    @Transactional(readOnly = true)
    public Long getUserIdFromAuth(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        return user.getId();
    }

    /**
     * 从Authentication中获取用户角色
     *
     * @param authentication 认证信息
     * @return 用户角色（不含ROLE_前缀）
     */
    @Transactional(readOnly = true)
    public String getRoleFromAuth(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        return user.getRole().name();
    }
}

