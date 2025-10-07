package com.university.academic.repository;

import com.university.academic.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 系统配置数据访问接口
 * 提供系统配置数据的CRUD操作
 *
 * @author Academic System Team
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {

    /**
     * 根据配置键查询配置
     *
     * @param configKey 配置键
     * @return 配置对象
     */
    Optional<SystemConfig> findByConfigKey(String configKey);

    /**
     * 检查配置键是否存在
     *
     * @param configKey 配置键
     * @return true-存在，false-不存在
     */
    boolean existsByConfigKey(String configKey);
}

