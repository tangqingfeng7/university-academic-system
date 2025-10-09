package com.university.academic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 大学教务管理系统主应用类
 *
 * @author Academic System Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class AcademicSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcademicSystemApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("大学教务管理系统启动成功！");
        System.out.println("========================================\n");
    }
}

