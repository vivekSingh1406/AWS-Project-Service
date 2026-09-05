package com.example.AmazonS3RDSService.config;

import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.time.Duration;

@Validated
@ConfigurationProperties("app.s3")
public record S3Properties(@NotBlank String bucket,
                           @NotBlank String region,
                           @NotBlank String accessKey,
                           @NotBlank String secretKey,
                           URI endpoint,
                           boolean pathStyle,
                           @NotNull Duration apiCallTimeout,
                           @NotNull Duration apiAttemptTimeout,
                           @Min(0) @Max(10) int maxRetries) {
    public S3Properties {
        if (apiCallTimeout != null && (apiCallTimeout.isZero() || apiCallTimeout.isNegative()))
            throw new IllegalArgumentException("API call timeout must be positive");
        if (apiAttemptTimeout != null && (apiAttemptTimeout.isZero() || apiAttemptTimeout.isNegative()))
            throw new IllegalArgumentException("API attempt timeout must be positive");
    }
}
