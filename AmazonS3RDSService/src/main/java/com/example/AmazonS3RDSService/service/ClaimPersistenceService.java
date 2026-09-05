package com.example.AmazonS3RDSService.service;

import com.example.AmazonS3RDSService.entity.*;
import com.example.AmazonS3RDSService.repository.FileProcessingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.util.Optional;

@Service
public class ClaimPersistenceService {
    private final FileProcessingRepository repository;
    public ClaimPersistenceService(FileProcessingRepository repository) { this.repository = repository; }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<ClaimResult> acquireExisting(String key, String hash) {
        return repository.findByIdempotencyKeyForUpdate(key).map(entity -> {
            if (!entity.getContentHash().equals(hash)) return new ClaimResult(ClaimDisposition.HASH_CONFLICT, entity);
            if (entity.getStatus() == ProcessingStatus.COMPLETED) return new ClaimResult(ClaimDisposition.COMPLETED, entity);
            if (entity.getStatus() == ProcessingStatus.FAILED) {
                entity.resume();
                repository.save(entity);
                return new ClaimResult(ClaimDisposition.RESUMABLE, entity);
            }
            return new ClaimResult(ClaimDisposition.IN_PROGRESS, entity);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FileProcessingEntity create(String key, String hash, String filename) {
        return repository.saveAndFlush(FileProcessingEntity.received(key, hash, filename));
    }
}
