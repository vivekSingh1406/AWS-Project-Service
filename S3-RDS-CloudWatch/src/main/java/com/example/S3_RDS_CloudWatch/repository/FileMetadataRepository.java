package com.example.S3_RDS_CloudWatch.repository;

import com.example.S3_RDS_CloudWatch.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
}
