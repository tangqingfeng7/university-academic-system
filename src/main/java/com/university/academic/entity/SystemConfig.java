package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 系统配置实体类
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "system_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfig extends BaseEntity {

    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    @Column(name = "config_value", nullable = false, length = 500)
    private String configValue;

    @Column(length = 200)
    private String description;
}

