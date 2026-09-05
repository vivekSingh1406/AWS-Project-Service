package com.example.AmazonS3RDSService.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfiguration {
    @Bean OpenAPI processingOpenApi() {
        return new OpenAPI().info(new Info().title("Secure XML Processing API").version("v1")
                .description("Validates XML against XSD and business rules, stores artifacts, and notifies a downstream service."));
    }
}
