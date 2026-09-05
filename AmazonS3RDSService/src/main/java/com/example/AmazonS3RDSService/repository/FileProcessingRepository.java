package com.example.AmazonS3RDSService.repository;

import com.example.AmazonS3RDSService.entity.FileProcessingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

public interface FileProcessingRepository extends JpaRepository<FileProcessingEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select processing from FileProcessingEntity processing where processing.idempotencyKey = :key")
    Optional<FileProcessingEntity> findByIdempotencyKeyForUpdate(@Param("key") String idempotencyKey);
    Optional<FileProcessingEntity> findByRequestId(UUID requestId);
}
