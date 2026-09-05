package com.example.AmazonS3RDSService.dto;

import com.example.AmazonS3RDSService.entity.FileProcessingEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProcessResult(UUID requestId, String companyId, String companyName, LocalDate date,
                            String status, String xmlFileName, String xmlKey,
                            String jsonFileName, String jsonKey,
                            boolean replayed, Instant createdAt, Instant updatedAt) {
    public static ProcessResult from(FileProcessingEntity entity, boolean replayed) {
        return new ProcessResult(entity.getRequestId(), entity.getCompanyId(), entity.getCompanyName(),
                entity.getSubmittedDate(), entity.getStatus().name(), entity.getXmlFileName(),
                entity.getXmlS3Key(), entity.getJsonFileName(), entity.getJsonS3Key(), replayed,
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
