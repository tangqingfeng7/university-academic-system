package com.university.academic.service.attendance;

import com.university.academic.entity.attendance.AttendanceConfig;

import java.util.List;
import java.util.Map;

/**
 * 考勤配置服务接口
 * 提供考勤配置的管理功能
 *
 * @author Academic System Team
 */
public interface AttendanceConfigService {

    /**
     * 获取配置值
     *
     * @param configKey 配置键
     * @return 配置值
     */
    String getConfigValue(String configKey);

    /**
     * 获取整数型配置
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    Integer getIntConfig(String configKey, Integer defaultValue);

    /**
     * 获取浮点型配置
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    Double getDoubleConfig(String configKey, Double defaultValue);

    /**
     * 获取布尔型配置
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    Boolean getBooleanConfig(String configKey, Boolean defaultValue);

    /**
     * 获取所有配置
     *
     * @return 配置列表
     */
    List<AttendanceConfig> getAllConfigs();

    /**
     * 获取所有配置的Map形式
     *
     * @return 配置Map（key -> value）
     */
    Map<String, String> getAllConfigsMap();

    /**
     * 更新配置
     *
     * @param configKey   配置键
     * @param configValue 配置值
     * @return 配置对象
     */
    AttendanceConfig updateConfig(String configKey, String configValue);

    /**
     * 批量更新配置
     *
     * @param configs 配置Map
     */
    void updateConfigs(Map<String, String> configs);

    /**
     * 创建配置
     *
     * @param configKey   配置键
     * @param configValue 配置值
     * @param configType  配置类型
     * @param description 描述
     * @param isSystem    是否系统配置
     * @return 配置对象
     */
    AttendanceConfig createConfig(String configKey, String configValue, 
                                  String configType, String description, Boolean isSystem);

    /**
     * 删除配置（非系统配置）
     *
     * @param configKey 配置键
     */
    void deleteConfig(String configKey);

    /**
     * 初始化默认配置
     */
    void initializeDefaultConfigs();

    /**
     * 刷新配置缓存
     */
    void refreshCache();
}

