package com.university.academic.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成器 - 用于生成BCrypt密码哈希
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String admin123 = encoder.encode("admin123");
        String teacher123 = encoder.encode("teacher123");
        String student123 = encoder.encode("student123");
        
        System.out.println("=== BCrypt 密码哈希 ===");
        System.out.println("admin123: " + admin123);
        System.out.println("teacher123: " + teacher123);
        System.out.println("student123: " + student123);
        System.out.println();
        System.out.println("=== SQL 更新语句 ===");
        System.out.println("UPDATE sys_user SET password = '" + admin123 + "' WHERE username = 'admin';");
        System.out.println("UPDATE sys_user SET password = '" + teacher123 + "' WHERE role = 'TEACHER';");
        System.out.println("UPDATE sys_user SET password = '" + student123 + "' WHERE role = 'STUDENT';");
    }
}

