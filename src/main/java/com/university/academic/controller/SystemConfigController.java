package com.university.academic.controller;

import com.university.academic.dto.SystemConfigDTO;
import com.university.academic.dto.UpdateSystemConfigRequest;
import com.university.academic.entity.SystemConfig;
import com.university.academic.service.SystemConfigService;
import com.university.academic.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 * 提供系统配置的查询和管理接口
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/configs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SystemConfigController {

    private final SystemConfigService configService;

    /**
     * 获取所有配置
     */
    @GetMapping
    public Result<List<SystemConfigDTO>> getAllConfigs() {
        log.info("查询所有系统配置");

        List<SystemConfigDTO> configs = configService.findAll();
        return Result.success(configs);
    }

    /**
     * 根据键获取配置
     */
    @GetMapping("/{key}")
    public Result<SystemConfigDTO> getConfig(@PathVariable String key) {
        log.info("查询系统配置: key={}", key);

        SystemConfig config = configService.findByKey(key);
        SystemConfigDTO dto = SystemConfigDTO.builder()
                .id(config.getId())
                .configKey(config.getConfigKey())
                .configValue(config.getConfigValue())
                .description(config.getDescription())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();

        return Result.success(dto);
    }

    /**
     * 更新配置
     */
    @PutMapping
    public Result<SystemConfigDTO> updateConfig(
            @Valid @RequestBody UpdateSystemConfigRequest request) {
        log.info("更新系统配置: key={}", request.getConfigKey());

        SystemConfig config = configService.updateConfig(request);
        SystemConfigDTO dto = SystemConfigDTO.builder()
                .id(config.getId())
                .configKey(config.getConfigKey())
                .configValue(config.getConfigValue())
                .description(config.getDescription())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();

        return Result.success("配置更新成功", dto);
    }

    /**
     * 批量更新配置
     */
    @PutMapping("/batch")
    public Result<String> batchUpdateConfigs(
            @Valid @RequestBody List<UpdateSystemConfigRequest> requests) {
        log.info("批量更新系统配置: 数量={}", requests.size());

        configService.batchUpdateConfigs(requests);
        return Result.success("批量更新成功");
    }

    /**
     * 创建配置
     */
    @PostMapping
    public Result<SystemConfigDTO> createConfig(
            @Valid @RequestBody UpdateSystemConfigRequest request) {
        log.info("创建系统配置: key={}", request.getConfigKey());

        SystemConfig config = configService.createConfig(request);
        SystemConfigDTO dto = SystemConfigDTO.builder()
                .id(config.getId())
                .configKey(config.getConfigKey())
                .configValue(config.getConfigValue())
                .description(config.getDescription())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();

        return Result.success("配置创建成功", dto);
    }

    /**
     * 删除配置
     */
    @DeleteMapping("/{key}")
    public Result<String> deleteConfig(@PathVariable String key) {
        log.info("删除系统配置: key={}", key);

        configService.deleteConfig(key);
        return Result.success("配置删除成功");
    }

    /**
     * 刷新配置缓存
     */
    @PostMapping("/refresh")
    public Result<String> refreshCache() {
        log.info("刷新系统配置缓存");

        configService.refreshCache();
        return Result.success("缓存刷新成功");
    }

    /**
     * 获取配置值（公共接口，用于前端获取常用配置）
     */
    @GetMapping("/public/{key}")
    @PreAuthorize("permitAll()")
    public Result<Map<String, String>> getPublicConfig(@PathVariable String key) {
        log.info("获取公共配置: key={}", key);

        String value = configService.getConfigValue(key);
        
        Map<String, String> result = new HashMap<>();
        result.put("key", key);
        result.put("value", value != null ? value : "");

        return Result.success(result);
    }

    /**
     * 获取常用配置（用于前端展示）
     */
    @GetMapping("/common")
    public Result<Map<String, String>> getCommonConfigs() {
        log.info("查询常用系统配置");

        Map<String, String> configs = new HashMap<>();
        configs.put(SystemConfigService.MAX_CREDITS_PER_SEMESTER, 
                configService.getConfigValue(SystemConfigService.MAX_CREDITS_PER_SEMESTER, "30"));
        configs.put(SystemConfigService.MAX_COURSES_PER_SEMESTER, 
                configService.getConfigValue(SystemConfigService.MAX_COURSES_PER_SEMESTER, "8"));
        configs.put(SystemConfigService.DROP_DEADLINE_DAYS, 
                configService.getConfigValue(SystemConfigService.DROP_DEADLINE_DAYS, "14"));
        configs.put(SystemConfigService.SYSTEM_NAME, 
                configService.getConfigValue(SystemConfigService.SYSTEM_NAME, "大学教务管理系统"));
        configs.put(SystemConfigService.SYSTEM_MAINTENANCE_MODE, 
                configService.getConfigValue(SystemConfigService.SYSTEM_MAINTENANCE_MODE, "false"));

        return Result.success(configs);
    }
}

