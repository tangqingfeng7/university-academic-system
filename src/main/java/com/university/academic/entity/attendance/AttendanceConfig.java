package com.university.academic.entity.attendance;

import com.university.academic.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 考勤配置实体类
 * 存储系统级考勤配置参数
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "attendance_config", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_config_key", columnNames = "config_key")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceConfig extends BaseEntity {

    /**
     * 配置键
     */
    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    /**
     * 配置值
     */
    @Column(name = "config_value", nullable = false, length = 500)
    private String configValue;

    /**
     * 配置类型（INTEGER/DOUBLE/BOOLEAN/STRING）
     */
    @Column(name = "config_type", nullable = false, length = 20)
    private String configType;

    /**
     * 配置描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 是否系统配置（系统配置不允许删除）
     */
    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private Boolean isSystem = false;
}

