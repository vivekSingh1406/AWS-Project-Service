package com.example.AmazonS3RDSService.validation;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Validated
@ConfigurationProperties("app.file-validation")
public record FileValidationProperties(@NotNull DataSize maxSize,
                                       @NotEmpty Set<String> allowedContentTypes) {
}
