/*
 * config/OpenApiConfig.java
 * OpenAPI 3.0 configuration bean for Swagger UI, Security schemes, and interactive documentation.
 * Connects to: Springdoc OpenAPI, security/JwtUtils.java, controllers/EmployeeController.java
 * Created: 2026-08-08
 */
package com.employee.directory.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class defining OpenAPI 3.0 specification metadata and JWT Bearer security schemes.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures global OpenAPI metadata and JWT SecurityScheme for Swagger UI visualization.
     * 
     * @return Configured OpenAPI bean instance.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "BearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Employee Directory API")
                        .version("1.4.0")
                        .description("Production-grade RESTful Employee Directory service featuring full CRUD capabilities, JPA persistence, multi-field pagination, sorting, department analytics, bulk CSV import/export, and Spring Security JWT authentication with RBAC roles.")
                        .contact(new Contact()
                                .name("Breaking The Bot Team")
                                .url("https://github.com/breakingthebot/employee-directory-api-build71")
                                .email("dev@breakingthebot.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT Bearer token obtained from POST /api/v1/auth/login")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ));
    }
}
