package com.example.AmazonS3RDSService.config;

import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.time.Duration;

@Validated
@ConfigurationProperties("app.downstream")
public record DownstreamProperties(@NotNull URI url,
                                   @NotNull Duration connectTimeout,
                                   @NotNull Duration readTimeout,
                                   @Min(1) @Max(5) int maxAttempts,
                                   @NotNull Duration backoff) {
    public DownstreamProperties {
        requireHttp(url);
        positive(connectTimeout, "connect timeout");
        positive(readTimeout, "read timeout");
        if (backoff != null && backoff.isNegative()) throw new IllegalArgumentException("backoff must not be negative");
    }
    private static void requireHttp(URI uri) {
        if (uri != null && !("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())))
            throw new IllegalArgumentException("downstream URL must use HTTP(S)");
    }
    private static void positive(Duration value, String name) {
        if (value != null && (value.isZero() || value.isNegative())) throw new IllegalArgumentException(name + " must be positive");
    }
}
