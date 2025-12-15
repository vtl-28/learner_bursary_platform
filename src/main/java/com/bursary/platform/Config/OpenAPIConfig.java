package com.bursary.platform.Config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Learner Management System API",
                version = "1.0.0",
                description = "RESTful API for learner registration, authentication, and profile management",
                contact = @Contact(
                        name = "Kwetu Development Team",
                        email = "dev@kwetu.com"
                ),
                license = @License(
                        name = "Proprietary",
                        url = "https://kwetu.com/license"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Local Development Server"
                ),
                @Server(
                        url = "https://api.kwetu.com",
                        description = "Production Server"
                )
        }
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenAPIConfig {
}

