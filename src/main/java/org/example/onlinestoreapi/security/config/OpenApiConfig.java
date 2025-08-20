package org.example.onlinestoreapi.security.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * OpenAPi documentation for the security system
 */
@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Roli",
                        email = "nagyroli0513@gmail.com"
                ),
                description = "OpenApi documentation for Spring Security",
                title = "OpenApi specification - Roli",
                version = "1.0",
                termsOfService = "Terms of service"
        ),
        servers = {
                @Server(
                        description = "Local ENV",
                        url = "http://localhost:8080"
                ),
                @Server(
                        description = "PROD ENV",
                        url = "https://aliboucoding.com/course"
                )
        },
        security = {
                @SecurityRequirement(
                        name = "cookieAuth"
                )
        }
)
@SecurityScheme(
        name = "cookieAuth",
        description = "JWT auth stored in HttpOnly cookie",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.COOKIE,
        paramName = "access_token, refresh_token"
)
public class OpenApiConfig {
}
