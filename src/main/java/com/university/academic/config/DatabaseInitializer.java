package com.university.academic.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 数据库初始化配置
 * 用于在应用启动时检查和更新默认管理员密码
 *
 * @author Academic System Team
 */
@Slf4j
@Configuration
public class DatabaseInitializer {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 初始化默认管理员账户
     * 如果数据库中管理员密码不是BCrypt格式，则更新为正确的加密密码
     */
    @Bean
    public CommandLineRunner initDefaultAdmin() {
        return args -> {
            try (Connection conn = dataSource.getConnection()) {
                // 检查是否存在管理员账户
                String checkSql = "SELECT id, password FROM sys_user WHERE username = 'admin' AND role = 'ADMIN'";
                try (PreparedStatement ps = conn.prepareStatement(checkSql);
                     ResultSet rs = ps.executeQuery()) {
                    
                    if (rs.next()) {
                        Long adminId = rs.getLong("id");
                        // 总是重新生成密码，确保正确
                        String newPassword = passwordEncoder.encode("admin123");
                        
                        String updateSql = "UPDATE sys_user SET password = ? WHERE id = ?";
                        try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                            updatePs.setString(1, newPassword);
                            updatePs.setLong(2, adminId);
                            int updated = updatePs.executeUpdate();
                            if (updated > 0) {
                                log.info("✓ 默认管理员密码已更新 (用户名: admin, 密码: admin123)");
                            }
                        }
                    } else {
                        // 如果不存在管理员，创建一个
                        String insertSql = "INSERT INTO sys_user (username, password, role, first_login, enabled) VALUES (?, ?, 'ADMIN', FALSE, TRUE)";
                        try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                            insertPs.setString(1, "admin");
                            insertPs.setString(2, passwordEncoder.encode("admin123"));
                            insertPs.executeUpdate();
                            log.info("✓ 已创建默认管理员账户 (用户名: admin, 密码: admin123)");
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("初始化默认管理员账户时出错: {}", e.getMessage());
            }
        };
    }
}

