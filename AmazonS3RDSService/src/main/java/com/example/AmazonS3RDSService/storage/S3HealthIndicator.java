package com.example.AmazonS3RDSService.storage;

import com.example.AmazonS3RDSService.config.S3Properties;
import org.springframework.boot.actuate.health.*;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

@Component
public class S3HealthIndicator implements HealthIndicator {
    private final S3Client client; private final S3Properties properties;
    public S3HealthIndicator(S3Client client, S3Properties properties) { this.client = client; this.properties = properties; }
    @Override public Health health() {
        try { client.headBucket(HeadBucketRequest.builder().bucket(properties.bucket()).build()); return Health.up().build(); }
        catch (RuntimeException failure) { return Health.down().withDetail("reason", "bucket unavailable").build(); }
    }
}
