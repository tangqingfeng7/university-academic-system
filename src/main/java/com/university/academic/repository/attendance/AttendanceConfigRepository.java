package com.university.academic.repository.attendance;

import com.university.academic.entity.attendance.AttendanceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 考勤配置数据访问接口
 *
 * @author Academic System Team
 */
@Repository
public interface AttendanceConfigRepository extends JpaRepository<AttendanceConfig, Long> {

    /**
     * 根据配置键查询配置
     *
     * @param configKey 配置键
     * @return 配置对象
     */
    Optional<AttendanceConfig> findByConfigKey(String configKey);

    /**
     * 查询所有系统配置
     *
     * @return 配置列表
     */
    List<AttendanceConfig> findByIsSystemTrue();

    /**
     * 查询所有非系统配置
     *
     * @return 配置列表
     */
    List<AttendanceConfig> findByIsSystemFalse();

    /**
     * 检查配置键是否存在
     *
     * @param configKey 配置键
     * @return true-存在，false-不存在
     */
    boolean existsByConfigKey(String configKey);
}

