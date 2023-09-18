package org.pikovets.GeeksSocialNetworkAPI.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Geeks Social Network",
                        email = "contact@gsn.com",
                        url = "https://geeks-social-network.com"
                ),
                description = "This is the API documentation for the Geek Social Network application." +
                        " It uses JWT tokens for security." +
                        " You must provide a valid JWT token for some functions to run successfully, otherwise a 403 Forbidden error will be generated." +
                        " For more information on how JWT tokens work, please visit the following website: [jwt.io/introduction](https://jwt.io/introduction).",
                title = "OpenAPI specification - Geeks Social Network",
                version = "1.0"
        ),
        servers = {
                @Server(
                        description = "Local ENV",
                        url = "http://localhost:8080"
                )
        },
        security = {
                @SecurityRequirement(
                        name = "bearerAuth"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT auth description",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenAPIConfig {
}