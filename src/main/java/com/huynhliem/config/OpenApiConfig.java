package com.huynhliem.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI openAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Booking System API")
                                                .version("1.0.0")
                                                .description("""
                                                                ## Booking System - REST API Documentation

                                                                Hệ thống đặt vé với xác thực JWT.

                                                                ### Cách sử dụng
                                                                1. Đăng ký tài khoản qua POST /auth/signup
                                                                2. Đăng nhập qua POST /auth/login → nhận accessToken
                                                                3. Click **Authorize** → nhập accessToken vào ô Bearer
                                                                4. Gọi các API yêu cầu xác thực

                                                                ### Response format
                                                                Mọi response đều có cùng cấu trúc:
                                                                ```json
                                                                {
                                                                  "code": 200,
                                                                  "message": "...",
                                                                  "data": {},
                                                                  "timestamp": "2026-08-03T15:00:00Z"
                                                                }
                                                                ```
                                                                """)
                                                .contact(new Contact()
                                                                .name("Huynh Liem")
                                                                .email("liemhuynh0789@gmail.com"))
                                                .license(new License()
                                                                .name("Apache 2.0")
                                                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                                .servers(List.of(
                                                new Server().url("http://localhost:8080")
                                                                .description("Local Development Server")))
                                .components(new Components()
                                                .addSecuritySchemes("bearer-key",
                                                                new SecurityScheme()
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")
                                                                                .description("Nhập accessToken nhận được sau khi đăng nhập")))
                                .security(List.of(
                                                new SecurityRequirement().addList("bearer-key")));
        }

        @Bean
        public GroupedOpenApi fullApi() {
                return GroupedOpenApi.builder()
                                .group("Full API")
                                .pathsToMatch("/**")
                                .build();
        }

        @Bean
        public GroupedOpenApi authApi() {
                return GroupedOpenApi.builder()
                                .group("Authentication")
                                .pathsToMatch("/auth/**")
                                .build();
        }

        @Bean
        public GroupedOpenApi userApi() {
                return GroupedOpenApi.builder()
                                .group("User Management")
                                .pathsToMatch("/user/**")
                                .build();
        }

        @Bean
        public GroupedOpenApi concertApi() {
                return GroupedOpenApi.builder()
                                .group("Concert")
                                .pathsToMatch("/concert/**")
                                .build();
        }

        @Bean
        public GroupedOpenApi ticketTypeApi() {
                return GroupedOpenApi.builder()
                                .group("Ticket Type")
                                .pathsToMatch("/ticket-type/**")
                                .build();
        }
}