package com.example.S3_RDS_CloudWatch.repository;

import com.example.S3_RDS_CloudWatch.model.FileUploadNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileUploadNotificationRepository extends JpaRepository<FileUploadNotification, Long> {
}
