package com.example.S3_RDS_CloudWatch.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Audit record for the email requested when a file is uploaded.
 */
@Entity
@Table(name = "file_upload_notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String recipientEmail;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    @Column(nullable = false)
    private String deliveryStatus;

    @Column(length = 1000)
    private String failureReason;
}
