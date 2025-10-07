package com.university.academic.controller;

import com.university.academic.dto.ChangePasswordRequest;
import com.university.academic.dto.LoginRequest;
import com.university.academic.dto.LoginResponse;
import com.university.academic.dto.RefreshTokenRequest;
import com.university.academic.entity.User;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.UserRepository;
import com.university.academic.util.JwtUtil;
import com.university.academic.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理用户登录、登出、令牌刷新、修改密码等操作
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录请求: {}", request.getUsername());

        // 查找用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("用户 {} 登录失败: 密码错误", request.getUsername());
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        // 检查账户状态
        if (!user.getEnabled()) {
            log.warn("用户 {} 登录失败: 账户已禁用", request.getUsername());
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        // 生成令牌
        String accessToken = jwtUtil.generateToken(
                user.getUsername(),
                user.getId(),
                user.getRole().name()
        );
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        // 构建响应
        LoginResponse response = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000) // 转换为秒
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .firstLogin(user.getFirstLogin())
                .build();

        log.info("用户 {} 登录成功", request.getUsername());
        return Result.success(response);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String username = authentication.getName();
            log.info("用户 {} 登出", username);
        }
        
        // 清除SecurityContext
        SecurityContextHolder.clearContext();
        
        return Result.success("登出成功");
    }

    /**
     * 刷新令牌
     */
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        try {
            // 验证刷新令牌
            if (!jwtUtil.validateToken(refreshToken)) {
                throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
            }

            // 从令牌中获取用户名
            String username = jwtUtil.getUsernameFromToken(refreshToken);

            // 查找用户
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            // 检查账户状态
            if (!user.getEnabled()) {
                throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
            }

            // 生成新的访问令牌
            String newAccessToken = jwtUtil.generateToken(
                    user.getUsername(),
                    user.getId(),
                    user.getRole().name()
            );

            // 构建响应
            LoginResponse response = LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken) // 刷新令牌保持不变
                    .tokenType("Bearer")
                    .expiresIn(jwtExpiration / 1000)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .role(user.getRole().name())
                    .firstLogin(user.getFirstLogin())
                    .build();

            log.info("用户 {} 刷新令牌成功", username);
            return Result.success(response);

        } catch (Exception e) {
            log.error("刷新令牌失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String username = authentication.getName();
        log.info("用户 {} 请求修改密码", username);

        // 查找用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            log.warn("用户 {} 修改密码失败: 旧密码错误", username);
            throw new BusinessException(ErrorCode.OLD_PASSWORD_ERROR);
        }

        // 验证新密码和确认密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 验证新密码不能与旧密码相同
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_SAME_AS_OLD);
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        
        // 如果是首次登录，更新首次登录标记
        if (user.getFirstLogin()) {
            user.setFirstLogin(false);
        }
        
        userRepository.save(user);

        log.info("用户 {} 修改密码成功", username);
        return Result.success("密码修改成功");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<LoginResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        LoginResponse response = LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .firstLogin(user.getFirstLogin())
                .build();

        return Result.success(response);
    }
}

