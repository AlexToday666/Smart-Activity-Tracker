package com.example.ActivityTracker.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Smart Activity Tracker API",
                version = "v1",
                description = "REST API для приёма событий и аналитики"
        )
)
public class OpenApiConfig {
}
