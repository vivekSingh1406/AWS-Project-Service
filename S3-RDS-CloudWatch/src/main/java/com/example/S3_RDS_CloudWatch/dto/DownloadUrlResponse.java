package com.example.S3_RDS_CloudWatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DownloadUrlResponse {
    private Long id;
    private String originalFileName;
    private String presignedUrl;
    private long expiresInSeconds;
}
