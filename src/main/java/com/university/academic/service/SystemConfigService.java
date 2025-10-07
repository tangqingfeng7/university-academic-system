package com.university.academic.service;

import com.university.academic.dto.SystemConfigDTO;
import com.university.academic.dto.UpdateSystemConfigRequest;
import com.university.academic.entity.SystemConfig;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 系统配置服务类
 * 提供系统配置的查询、更新功能，并实现缓存机制
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository configRepository;

    /**
     * 配置缓存（configKey -> configValue）
     */
    private final Map<String, String> configCache = new ConcurrentHashMap<>();

    /**
     * 初始化时加载所有配置到缓存
     */
    @PostConstruct
    public void init() {
        log.info("初始化系统配置缓存");
        refreshCache();
        log.info("系统配置缓存初始化完成，共加载{}条配置", configCache.size());
    }

    /**
     * 刷新缓存
     */
    @Transactional(readOnly = true)
    public void refreshCache() {
        List<SystemConfig> configs = configRepository.findAll();
        configCache.clear();
        configs.forEach(config -> 
            configCache.put(config.getConfigKey(), config.getConfigValue()));
        log.info("系统配置缓存已刷新");
    }

    /**
     * 获取配置值（从缓存）
     *
     * @param key 配置键
     * @return 配置值，如果不存在返回null
     */
    public String getConfigValue(String key) {
        return configCache.get(key);
    }

    /**
     * 获取配置值（从缓存），如果不存在返回默认值
     *
     * @param key          配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public String getConfigValue(String key, String defaultValue) {
        return configCache.getOrDefault(key, defaultValue);
    }

    /**
     * 获取整型配置值
     *
     * @param key          配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public Integer getIntValue(String key, Integer defaultValue) {
        String value = configCache.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.error("配置值格式错误: key={}, value={}", key, value, e);
            return defaultValue;
        }
    }

    /**
     * 获取布尔型配置值
     *
     * @param key          配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public Boolean getBooleanValue(String key, Boolean defaultValue) {
        String value = configCache.get(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * 查询所有配置
     *
     * @return 配置列表
     */
    @Transactional(readOnly = true)
    public List<SystemConfigDTO> findAll() {
        List<SystemConfig> configs = configRepository.findAll();
        return configs.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据键查询配置
     *
     * @param key 配置键
     * @return 配置对象
     */
    @Transactional(readOnly = true)
    public SystemConfig findByKey(String key) {
        return configRepository.findByConfigKey(key)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFIG_NOT_FOUND));
    }

    /**
     * 更新配置
     *
     * @param request 更新请求
     * @return 更新后的配置
     */
    @Transactional
    public SystemConfig updateConfig(UpdateSystemConfigRequest request) {
        log.info("更新系统配置: key={}", request.getConfigKey());

        SystemConfig config = configRepository.findByConfigKey(request.getConfigKey())
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFIG_NOT_FOUND));

        config.setConfigValue(request.getConfigValue());
        if (request.getDescription() != null) {
            config.setDescription(request.getDescription());
        }

        config = configRepository.save(config);

        // 更新缓存
        configCache.put(config.getConfigKey(), config.getConfigValue());
        log.info("系统配置已更新并刷新缓存: key={}", request.getConfigKey());

        return config;
    }

    /**
     * 批量更新配置
     *
     * @param requests 更新请求列表
     */
    @Transactional
    public void batchUpdateConfigs(List<UpdateSystemConfigRequest> requests) {
        log.info("批量更新系统配置: 数量={}", requests.size());

        for (UpdateSystemConfigRequest request : requests) {
            SystemConfig config = configRepository.findByConfigKey(request.getConfigKey())
                    .orElse(null);
            
            if (config != null) {
                config.setConfigValue(request.getConfigValue());
                if (request.getDescription() != null) {
                    config.setDescription(request.getDescription());
                }
                configRepository.save(config);
                
                // 更新缓存
                configCache.put(config.getConfigKey(), config.getConfigValue());
            } else {
                log.warn("配置不存在: key={}", request.getConfigKey());
            }
        }

        log.info("批量更新系统配置完成");
    }

    /**
     * 创建配置
     *
     * @param request 配置请求
     * @return 创建的配置
     */
    @Transactional
    public SystemConfig createConfig(UpdateSystemConfigRequest request) {
        log.info("创建系统配置: key={}", request.getConfigKey());

        // 检查配置键是否已存在
        if (configRepository.existsByConfigKey(request.getConfigKey())) {
            throw new BusinessException(ErrorCode.CONFIG_KEY_ALREADY_EXISTS);
        }

        SystemConfig config = SystemConfig.builder()
                .configKey(request.getConfigKey())
                .configValue(request.getConfigValue())
                .description(request.getDescription())
                .build();

        config = configRepository.save(config);

        // 添加到缓存
        configCache.put(config.getConfigKey(), config.getConfigValue());
        log.info("系统配置已创建: key={}", request.getConfigKey());

        return config;
    }

    /**
     * 删除配置
     *
     * @param key 配置键
     */
    @Transactional
    public void deleteConfig(String key) {
        log.info("删除系统配置: key={}", key);

        SystemConfig config = configRepository.findByConfigKey(key)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFIG_NOT_FOUND));

        configRepository.delete(config);

        // 从缓存中移除
        configCache.remove(key);
        log.info("系统配置已删除: key={}", key);
    }

    /**
     * 转换为DTO
     */
    private SystemConfigDTO toDTO(SystemConfig config) {
        return SystemConfigDTO.builder()
                .id(config.getId())
                .configKey(config.getConfigKey())
                .configValue(config.getConfigValue())
                .description(config.getDescription())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }

    // ========== 预定义配置键常量 ==========

    /** 每学期最大学分 */
    public static final String MAX_CREDITS_PER_SEMESTER = "max.credits.per.semester";

    /** 每学期最多课程数 */
    public static final String MAX_COURSES_PER_SEMESTER = "max.courses.per.semester";

    /** 退课截止天数 */
    public static final String DROP_DEADLINE_DAYS = "drop.deadline.days";

    /** 系统维护模式 */
    public static final String SYSTEM_MAINTENANCE_MODE = "system.maintenance.mode";

    /** 系统名称 */
    public static final String SYSTEM_NAME = "system.name";
}

