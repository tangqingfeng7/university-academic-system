package com.university.academic.service.attendance.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.university.academic.entity.attendance.AttendanceConfig;
import com.university.academic.exception.ErrorCode;
import com.university.academic.exception.attendance.AttendanceException;
import com.university.academic.repository.attendance.AttendanceConfigRepository;
import com.university.academic.service.attendance.AttendanceConfigService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 考勤配置服务实现类
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceConfigServiceImpl implements AttendanceConfigService {

    private final AttendanceConfigRepository configRepository;

    /**
     * 配置缓存
     */
    private Cache<String, String> configCache;

    @PostConstruct
    public void init() {
        // 初始化配置缓存，1小时过期
        configCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(100)
                .build();

        log.info("考勤配置缓存初始化完成，过期时间: 1小时");

        // 初始化默认配置
        initializeDefaultConfigs();
    }

    @Override
    @Transactional(readOnly = true)
    public String getConfigValue(String configKey) {
        // 先从缓存获取
        String cached = configCache.getIfPresent(configKey);
        if (cached != null) {
            return cached;
        }

        // 从数据库获取
        AttendanceConfig config = configRepository.findByConfigKey(configKey).orElse(null);
        if (config != null) {
            configCache.put(configKey, config.getConfigValue());
            return config.getConfigValue();
        }

        return null;
    }

    @Override
    public Integer getIntConfig(String configKey, Integer defaultValue) {
        String value = getConfigValue(configKey);
        if (value == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("配置值解析失败: key={}, value={}", configKey, value);
            return defaultValue;
        }
    }

    @Override
    public Double getDoubleConfig(String configKey, Double defaultValue) {
        String value = getConfigValue(configKey);
        if (value == null) {
            return defaultValue;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.warn("配置值解析失败: key={}, value={}", configKey, value);
            return defaultValue;
        }
    }

    @Override
    public Boolean getBooleanConfig(String configKey, Boolean defaultValue) {
        String value = getConfigValue(configKey);
        if (value == null) {
            return defaultValue;
        }

        return Boolean.parseBoolean(value);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceConfig> getAllConfigs() {
        return configRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getAllConfigsMap() {
        return configRepository.findAll().stream()
                .collect(Collectors.toMap(
                        AttendanceConfig::getConfigKey,
                        AttendanceConfig::getConfigValue
                ));
    }

    @Override
    @Transactional
    public AttendanceConfig updateConfig(String configKey, String configValue) {
        log.info("更新配置: key={}, value={}", configKey, configValue);

        AttendanceConfig config = configRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new AttendanceException(ErrorCode.ATTENDANCE_CONFIG_NOT_FOUND));

        config.setConfigValue(configValue);
        config = configRepository.save(config);

        // 更新缓存
        configCache.put(configKey, configValue);

        log.info("配置更新成功: key={}", configKey);
        return config;
    }

    @Override
    @Transactional
    public void updateConfigs(Map<String, String> configs) {
        log.info("批量更新配置: count={}", configs.size());

        for (Map.Entry<String, String> entry : configs.entrySet()) {
            try {
                updateConfig(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                log.error("配置更新失败: key={}", entry.getKey(), e);
            }
        }
    }

    @Override
    @Transactional
    public AttendanceConfig createConfig(String configKey, String configValue,
                                        String configType, String description, Boolean isSystem) {
        log.info("创建配置: key={}", configKey);

        if (configRepository.existsByConfigKey(configKey)) {
            throw new AttendanceException(ErrorCode.DATA_ALREADY_EXISTS, "配置键已存在");
        }

        AttendanceConfig config = AttendanceConfig.builder()
                .configKey(configKey)
                .configValue(configValue)
                .configType(configType)
                .description(description)
                .isSystem(isSystem != null ? isSystem : false)
                .build();

        config = configRepository.save(config);

        // 更新缓存
        configCache.put(configKey, configValue);

        log.info("配置创建成功: key={}", configKey);
        return config;
    }

    @Override
    @Transactional
    public void deleteConfig(String configKey) {
        log.info("删除配置: key={}", configKey);

        AttendanceConfig config = configRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new AttendanceException(ErrorCode.ATTENDANCE_CONFIG_NOT_FOUND));

        if (config.getIsSystem()) {
            throw new AttendanceException(ErrorCode.INVALID_OPERATION, "系统配置不允许删除");
        }

        configRepository.delete(config);

        // 移除缓存
        configCache.invalidate(configKey);

        log.info("配置已删除: key={}", configKey);
    }

    @Override
    @Transactional
    public void initializeDefaultConfigs() {
        log.info("初始化默认考勤配置");

        Map<String, ConfigItem> defaultConfigs = new HashMap<>();

        // 签到时间配置
        defaultConfigs.put("attendance.qrcode.expire_minutes",
                new ConfigItem("5", "INTEGER", "二维码签到有效期（分钟）", true));
        defaultConfigs.put("attendance.early_checkin_minutes",
                new ConfigItem("5", "INTEGER", "允许提前签到时间（分钟）", true));
        defaultConfigs.put("attendance.late_threshold_minutes",
                new ConfigItem("5", "INTEGER", "迟到判定时间（分钟）", true));
        defaultConfigs.put("attendance.max_late_minutes",
                new ConfigItem("15", "INTEGER", "最晚签到时间（分钟）", true));

        // 定位签到配置
        defaultConfigs.put("attendance.location.geofence_radius",
                new ConfigItem("100", "INTEGER", "地理围栏半径（米）", true));

        // 预警配置
        defaultConfigs.put("attendance.warning.absent_threshold",
                new ConfigItem("3", "INTEGER", "旷课次数预警阈值", true));
        defaultConfigs.put("attendance.warning.absent_rate_threshold",
                new ConfigItem("0.33", "DOUBLE", "旷课比例预警阈值", true));
        defaultConfigs.put("attendance.warning.low_attendance_rate",
                new ConfigItem("0.70", "DOUBLE", "课程出勤率预警阈值", true));
        defaultConfigs.put("attendance.warning.teacher_no_attendance_days",
                new ConfigItem("7", "INTEGER", "教师未考勤提醒天数", true));

        // 功能开关
        defaultConfigs.put("attendance.method.manual.enabled",
                new ConfigItem("true", "BOOLEAN", "启用手动点名", true));
        defaultConfigs.put("attendance.method.qrcode.enabled",
                new ConfigItem("true", "BOOLEAN", "启用扫码签到", true));
        defaultConfigs.put("attendance.method.location.enabled",
                new ConfigItem("true", "BOOLEAN", "启用定位签到", true));

        // 缓存配置
        defaultConfigs.put("attendance.cache.expire_minutes",
                new ConfigItem("30", "INTEGER", "统计数据缓存过期时间（分钟）", true));

        // 初始化配置
        for (Map.Entry<String, ConfigItem> entry : defaultConfigs.entrySet()) {
            String key = entry.getKey();
            ConfigItem item = entry.getValue();

            if (!configRepository.existsByConfigKey(key)) {
                createConfig(key, item.value, item.type, item.description, item.isSystem);
                log.info("默认配置已创建: key={}, value={}", key, item.value);
            }
        }

        log.info("默认配置初始化完成");
    }

    @Override
    public void refreshCache() {
        log.info("刷新配置缓存");
        configCache.invalidateAll();
    }

    /**
     * 配置项辅助类
     */
    private static class ConfigItem {
        String value;
        String type;
        String description;
        Boolean isSystem;

        ConfigItem(String value, String type, String description, Boolean isSystem) {
            this.value = value;
            this.type = type;
            this.description = description;
            this.isSystem = isSystem;
        }
    }
}

