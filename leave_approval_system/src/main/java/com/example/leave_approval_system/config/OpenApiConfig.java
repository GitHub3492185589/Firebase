package com.example.leave_approval_system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger OpenAPI 配置类
 * 用于配置 API 文档信息
 */
@Configuration
public class OpenApiConfig {

    /**
     * 配置 OpenAPI 信息
     * @return OpenAPI 配置实例
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // 添加安全方案配置
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                // 配置安全方案组件
                .components(new Components()
                        .addSecuritySchemes("JWT", new SecurityScheme()
                                .name("JWT")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .description("请在此输入JWT令牌，格式为：Bearer {token}")))
                // 配置 API 基本信息
                .info(new Info()
                        .title("请假审批系统 API 文档")
                        .description("请假审批系统后端 API 接口文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("contact@example.com")
                                .url("https://example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}