package com.university.academic.controller;

import com.university.academic.dto.*;
import com.university.academic.entity.User;
import com.university.academic.service.UserService;
import com.university.academic.util.DtoConverter;
import com.university.academic.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户管理控制器
 * 提供用户CRUD接口（仅管理员可访问）
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;
    private final DtoConverter dtoConverter;

    /**
     * 分页查询用户列表
     */
    @GetMapping
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {

        log.info("查询用户列表: page={}, size={}, sortBy={}, direction={}", page, size, sortBy, direction);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<User> userPage = userService.findAll(pageable);
        List<UserDTO> userDTOList = userPage.getContent().stream()
                .map(dtoConverter::toUserDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", userDTOList);
        response.put("totalElements", userPage.getTotalElements());
        response.put("totalPages", userPage.getTotalPages());
        response.put("currentPage", userPage.getNumber());
        response.put("pageSize", userPage.getSize());

        return Result.success(response);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public Result<UserDTO> getUserById(@PathVariable Long id) {
        log.info("查询用户详情: id={}", id);

        User user = userService.findById(id);
        UserDTO userDTO = dtoConverter.toUserDTO(user);

        return Result.success(userDTO);
    }

    /**
     * 创建用户
     */
    @PostMapping
    public Result<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("创建用户: username={}, role={}", request.getUsername(), request.getRole());

        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .role(User.UserRole.valueOf(request.getRole()))
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .firstLogin(true)
                .build();

        User createdUser = userService.createUser(user);
        UserDTO userDTO = dtoConverter.toUserDTO(createdUser);

        return Result.success("用户创建成功", userDTO);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public Result<UserDTO> updateUser(@PathVariable Long id,
                                      @Valid @RequestBody UpdateUserRequest request) {
        log.info("更新用户: id={}", id);

        User user = User.builder()
                .username(request.getUsername())
                .role(request.getRole() != null ? User.UserRole.valueOf(request.getRole()) : null)
                .enabled(request.getEnabled())
                .build();

        User updatedUser = userService.updateUser(id, user);
        UserDTO userDTO = dtoConverter.toUserDTO(updatedUser);

        return Result.success("用户更新成功", userDTO);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        log.info("删除用户: id={}", id);

        userService.deleteUser(id);

        return Result.success("用户删除成功");
    }

    /**
     * 启用/禁用用户
     */
    @PutMapping("/{id}/enabled")
    public Result<String> setUserEnabled(@PathVariable Long id,
                                         @RequestParam boolean enabled) {
        log.info("设置用户启用状态: id={}, enabled={}", id, enabled);

        userService.setUserEnabled(id, enabled);

        return Result.success(enabled ? "用户已启用" : "用户已禁用");
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/{id}/password/reset")
    public Result<String> resetPassword(@PathVariable Long id,
                                        @Valid @RequestBody ResetPasswordRequest request) {
        log.info("重置用户密码: id={}", id);

        userService.resetPassword(id, request.getNewPassword());

        return Result.success("密码重置成功，用户下次登录需修改密码");
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    public Result<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return Result.success(response);
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getUserStatistics() {
        log.info("查询用户统计信息");

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalUsers", userService.count());
        statistics.put("adminCount", userService.countByRole(User.UserRole.ADMIN));
        statistics.put("teacherCount", userService.countByRole(User.UserRole.TEACHER));
        statistics.put("studentCount", userService.countByRole(User.UserRole.STUDENT));

        return Result.success(statistics);
    }
}

