package com.university.academic.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger配置类
 * 提供API文档自动生成功能
 *
 * @author Academic System Team
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("大学教务管理系统 API文档")
                        .description("基于Spring Boot + Vue 3的现代化教务管理平台\n\n" +
                                "## 主要功能模块\n" +
                                "- 用户管理：学生、教师、管理员信息管理\n" +
                                "- 课程管理：课程、开课计划、选课管理\n" +
                                "- 成绩管理：成绩录入、查询、统计分析\n" +
                                "- 考试管理：考试计划、考场安排、监考管理\n" +
                                "- 请假管理：请假申请、审批流程\n" +
                                "- 通知管理：系统通知、公告发布\n\n" +
                                "## API权限说明\n" +
                                "- 管理员端：/api/admin/** 需要 ADMIN 角色\n" +
                                "- 教师端：/api/teacher/** 需要 TEACHER 角色\n" +
                                "- 学生端：/api/student/** 需要 STUDENT 角色\n" +
                                "- 认证接口：/api/auth/** 无需认证")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Academic System Team")
                                .email("support@example.com")
                                .url("https://github.com/your-repo"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                                .description("JWT认证令牌")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}

