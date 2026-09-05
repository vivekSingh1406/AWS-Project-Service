package com.example.AmazonS3RDSService.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;


@Getter
@Setter
@Entity
@Table(name = "file_processing", uniqueConstraints = {
        @UniqueConstraint(name = "uk_file_processing_request_id", columnNames = "request_id"),
        @UniqueConstraint(name = "uk_file_processing_idempotency_key", columnNames = "idempotency_key")
})
public class FileProcessingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false, updatable = false)
    private UUID requestId;

    @Column(name = "idempotency_key", length = 128, updatable = false)
    private String idempotencyKey;

    @Column(name = "content_hash", length = 64, updatable = false)
    private String contentHash;

    @Column(name = "company_id", nullable = false, length = 100, updatable = false)
    private String companyId;

    @Column(name = "company_name", nullable = false, length = 200, updatable = false)
    private String companyName;

    @Column(name = "submitted_date", nullable = false, updatable = false)
    private LocalDate submittedDate;

    @Column(name = "xml_file_name", nullable = false, length = 255)
    private String xmlFileName;

    @Column(name = "xml_s3_key", length = 512)
    private String xmlS3Key;

    @Column(name = "json_file_name", length = 255)
    private String jsonFileName;

    @Column(name = "json_s3_key", length = 512)
    private String jsonS3Key;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ProcessingStatus status;

    @Column(name = "error_code", length = 64)
    private String errorCode;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected FileProcessingEntity() {
    }

    public static FileProcessingEntity received(String key, String hash, String filename) {
        FileProcessingEntity entity = new FileProcessingEntity();
        entity.requestId = UUID.randomUUID();
        entity.idempotencyKey = key;
        entity.contentHash = hash;
        entity.xmlFileName = filename;
        entity.status = ProcessingStatus.RECEIVED;
        entity.createdAt = Instant.now();
        entity.updatedAt = entity.createdAt;
        return entity;
    }

    public static FileProcessingEntity received(String companyId, String companyName,
                                                LocalDate submittedDate, String filename) {
        FileProcessingEntity entity = new FileProcessingEntity();
        entity.requestId = UUID.randomUUID();
        entity.companyId = companyId;
        entity.companyName = companyName;
        entity.submittedDate = submittedDate;
        entity.xmlFileName = filename;
        entity.status = ProcessingStatus.RECEIVED;
        entity.createdAt = Instant.now();
        entity.updatedAt = entity.createdAt;
        return entity;
    }

    public void markXmlStored(String key) {
        xmlS3Key = key;
        status = ProcessingStatus.XML_STORED;
        clearError();
    }

    public void markJsonStored(String filename, String key) {
        jsonFileName = filename;
        jsonS3Key = key;
        status = ProcessingStatus.JSON_STORED;
        clearError();
    }

    public void markCompleted() {
        status = ProcessingStatus.COMPLETED;
        clearError();
    }

    public void resume() {
        status = ProcessingStatus.RECEIVED;
        clearError();
    }

    public void markFailed(String code, String message) {
        status = ProcessingStatus.FAILED;
        errorCode = code;
        errorMessage = message;
        touch();
    }

    private void clearError() {
        errorCode = null;
        errorMessage = null;
        touch();
    }
    private void touch() {
        updatedAt = Instant.now();
    }
}
