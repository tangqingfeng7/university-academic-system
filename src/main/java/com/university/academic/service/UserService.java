package com.university.academic.service;

import com.university.academic.entity.User;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务类
 * 提供用户CRUD、密码管理等功能
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户对象
     */
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 查询所有用户
     *
     * @return 用户列表
     */
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * 分页查询用户
     *
     * @param pageable 分页参数
     * @return 用户分页数据
     */
    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * 创建用户
     *
     * @param user 用户对象
     * @return 创建后的用户对象
     */
    @Transactional
    public User createUser(User user) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 设置默认值
        if (user.getEnabled() == null) {
            user.setEnabled(true);
        }
        if (user.getFirstLogin() == null) {
            user.setFirstLogin(true);
        }

        User savedUser = userRepository.save(user);
        log.info("创建用户成功: {}", savedUser.getUsername());
        return savedUser;
    }

    /**
     * 更新用户信息
     *
     * @param id   用户ID
     * @param user 更新的用户信息
     * @return 更新后的用户对象
     */
    @Transactional
    public User updateUser(Long id, User user) {
        User existingUser = findById(id);

        // 如果要修改用户名，检查新用户名是否已存在
        if (!existingUser.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(user.getUsername())) {
                throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
            }
            existingUser.setUsername(user.getUsername());
        }

        // 更新其他字段（密码除外）
        if (user.getRole() != null) {
            existingUser.setRole(user.getRole());
        }
        if (user.getEnabled() != null) {
            existingUser.setEnabled(user.getEnabled());
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("更新用户成功: {}", updatedUser.getUsername());
        return updatedUser;
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = findById(id);
        userRepository.delete(user);
        log.info("删除用户成功: {}", user.getUsername());
    }

    /**
     * 启用/禁用用户
     *
     * @param id      用户ID
     * @param enabled 启用状态
     */
    @Transactional
    public void setUserEnabled(Long id, boolean enabled) {
        User user = findById(id);
        user.setEnabled(enabled);
        userRepository.save(user);
        log.info("设置用户 {} 启用状态为: {}", user.getUsername(), enabled);
    }

    /**
     * 重置用户密码
     *
     * @param id          用户ID
     * @param newPassword 新密码
     */
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = findById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFirstLogin(true); // 重置后需要首次登录修改密码
        userRepository.save(user);
        log.info("重置用户 {} 密码成功", user.getUsername());
    }

    /**
     * 修改密码
     *
     * @param username    用户名
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = findByUsername(username);

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.OLD_PASSWORD_ERROR);
        }

        // 验证新密码不能与旧密码相同
        if (oldPassword.equals(newPassword)) {
            throw new BusinessException(ErrorCode.PASSWORD_SAME_AS_OLD);
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFirstLogin(false);
        userRepository.save(user);
        log.info("用户 {} 修改密码成功", username);
    }

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return true-存在，false-不存在
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 统计用户数量
     *
     * @return 用户总数
     */
    @Transactional(readOnly = true)
    public long count() {
        return userRepository.count();
    }

    /**
     * 根据角色统计用户数量
     *
     * @param role 角色
     * @return 该角色的用户数量
     */
    @Transactional(readOnly = true)
    public long countByRole(User.UserRole role) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == role)
                .count();
    }
}

