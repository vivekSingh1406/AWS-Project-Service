package com.example.S3_RDS_CloudWatch.service;


import com.example.S3_RDS_CloudWatch.dto.DownloadUrlResponse;
import com.example.S3_RDS_CloudWatch.dto.FileUploadResponse;
import com.example.S3_RDS_CloudWatch.exception.FileNotFoundException;
import com.example.S3_RDS_CloudWatch.model.FileMetadata;
import com.example.S3_RDS_CloudWatch.repository.FileMetadataRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final FileMetadataRepository repository;
    private final MeterRegistry meterRegistry;

    @Value("${cloud.s3.bucket-name}")
    private String bucketName;

    @Value("${app.presigned-url.expiry-minutes:15}")
    private long presignedUrlExpiryMinutes;

    public FileService(S3Client s3Client,
                        S3Presigner s3Presigner,
                        FileMetadataRepository repository,
                        MeterRegistry meterRegistry) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.repository = repository;
        this.meterRegistry = meterRegistry;
    }

    /**
     * 1. Streams the file to S3
     * 2. Persists metadata (S3 key, size, content-type) to RDS
     * 3. Emits a CloudWatch custom metric + structured log line
     */
    public FileUploadResponse uploadFile(MultipartFile file) {
        String s3Key = buildS3Key(file.getOriginalFilename());

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            FileMetadata metadata = FileMetadata.builder()
                    .originalFileName(file.getOriginalFilename())
                    .s3Key(s3Key)
                    .s3Bucket(bucketName)
                    .contentType(file.getContentType())
                    .fileSizeBytes(file.getSize())
                    .uploadedAt(LocalDateTime.now())
                    .downloadCount(0L)
                    .build();

            FileMetadata saved = repository.save(metadata);

            meterRegistry.counter("file.upload.success").increment();
            meterRegistry.summary("file.upload.size.bytes").record(file.getSize());

            log.info("File uploaded successfully. id={}, s3Key={}, size={} bytes",
                    saved.getId(), s3Key, file.getSize());

            return FileUploadResponse.builder()
                    .id(saved.getId())
                    .originalFileName(saved.getOriginalFileName())
                    .fileSizeBytes(saved.getFileSizeBytes())
                    .uploadedAt(saved.getUploadedAt())
                    .message("Upload successful")
                    .build();

        } catch (IOException e) {
            meterRegistry.counter("file.upload.failure").increment();
            log.error("Failed to read multipart file stream for upload", e);
            throw new RuntimeException("Failed to read file for upload", e);
        }
    }

    /**
     * Generates a time-limited pre-signed S3 URL so clients can download
     * directly from S3 without the file ever passing through this API.
     */
    public DownloadUrlResponse generateDownloadUrl(Long id) {
        FileMetadata metadata = repository.findById(id)
                .orElseThrow(() -> new FileNotFoundException(id));

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(metadata.getS3Bucket())
                .key(metadata.getS3Key())
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignedUrlExpiryMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

        metadata.setDownloadCount(metadata.getDownloadCount() + 1);
        repository.save(metadata);

        meterRegistry.counter("file.download.presigned_url_generated").increment();
        log.info("Presigned download URL generated. id={}, expiresInMinutes={}", id, presignedUrlExpiryMinutes);

        return DownloadUrlResponse.builder()
                .id(metadata.getId())
                .originalFileName(metadata.getOriginalFileName())
                .presignedUrl(presignedRequest.url().toString())
                .expiresInSeconds(presignedUrlExpiryMinutes * 60)
                .build();
    }

    public List<FileMetadata> listFiles() {
        return repository.findAll().stream().collect(Collectors.toList());
    }

    public void deleteFile(Long id) {
        FileMetadata metadata = repository.findById(id)
                .orElseThrow(() -> new FileNotFoundException(id));

        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(metadata.getS3Bucket())
                .key(metadata.getS3Key())
                .build());

        repository.delete(metadata);

        meterRegistry.counter("file.delete.success").increment();
        log.info("File deleted. id={}, s3Key={}", id, metadata.getS3Key());
    }

    private String buildS3Key(String originalFileName) {
        String safeName = originalFileName == null ? "unnamed" : originalFileName.replaceAll("\\s+", "_");
        return "uploads/" + UUID.randomUUID() + "-" + safeName;
    }
}
