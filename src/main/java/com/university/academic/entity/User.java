package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 用户实体类
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "sys_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;

    @Builder.Default
    @Column(name = "first_login", nullable = false)
    private Boolean firstLogin = true;

    /**
     * 用户角色枚举
     */
    public enum UserRole {
        ADMIN("管理员"),
        TEACHER("教师"),
        STUDENT("学生");

        private final String description;

        UserRole(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}

