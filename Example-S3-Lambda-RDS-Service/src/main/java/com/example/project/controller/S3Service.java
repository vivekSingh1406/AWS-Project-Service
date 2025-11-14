package com.example.project.controller;


import com.example.project.model.UserData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;
import java.util.UUID;


@Service
public class S3Service {

    @Autowired
    public S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public String uploadToS3(UserData data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(data);

            String fileName = "user-" + UUID.randomUUID() + ".json";

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType("application/json")
                    .build();

            s3Client.putObject(request, RequestBody.fromString(json, StandardCharsets.UTF_8));
            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("S3 upload failed", e);
        }
    }
}