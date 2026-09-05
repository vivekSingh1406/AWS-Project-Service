package com.example.AmazonS3RDSService.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.xml")
public record XmlProcessingProperties(@Min(1) int maxDepth,
                                      @Min(1) int maxElements,
                                      @Min(1) long maxTotalCharacters,
                                      @Min(1) int maxElementCharacters) {
}
