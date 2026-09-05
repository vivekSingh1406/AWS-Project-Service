package com.example.AmazonS3RDSService.storage;

import com.example.AmazonS3RDSService.config.S3Properties;
import com.example.AmazonS3RDSService.exception.ProcessingException;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Map;

@Component
public class S3ObjectStorage implements ObjectStorage {
    private final S3Client client;
    private final S3Properties properties;

    public S3ObjectStorage(S3Client client, S3Properties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public void put(String key, byte[] content, String contentType, Map<String, String> metadata) {
        try {
            PutObjectRequest request = PutObjectRequest.builder().bucket(properties.bucket()).key(key)
                    .contentType(contentType).contentLength((long) content.length).metadata(Map.copyOf(metadata)).build();
            client.putObject(request, RequestBody.fromBytes(content));
        } catch (SdkException exception) {
            throw new ProcessingException("S3_UPLOAD_FAILED", "Artifact storage is temporarily unavailable", exception);
        }
    }
}
