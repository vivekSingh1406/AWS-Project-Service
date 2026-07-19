package com.example.S3_RDS_CloudWatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private Long id;
    private String originalFileName;
    private Long fileSizeBytes;
    private LocalDateTime uploadedAt;
    private String message;
}
