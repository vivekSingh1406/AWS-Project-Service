package com.example.AmazonS3RDSService.service;

import com.example.AmazonS3RDSService.entity.FileProcessingEntity;
import com.example.AmazonS3RDSService.entity.ProcessingStatus;
import com.example.AmazonS3RDSService.repository.FileProcessingRepository;
import com.example.AmazonS3RDSService.exception.ProcessingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.time.LocalDate;

@Service
public class ProcessingStateService {
    private final FileProcessingRepository repository;
    private final ClaimPersistenceService claims;

    public ProcessingStateService(FileProcessingRepository repository, ClaimPersistenceService claims) {
        this.repository = repository;
        this.claims = claims;
    }

    public ClaimResult claim(String key, String hash, String filename) {
        var existing = claims.acquireExisting(key, hash);
        if (existing.isPresent()) return existing.get();
        try {
            return new ClaimResult(ClaimDisposition.NEW, claims.create(key, hash, filename));
        } catch (DataIntegrityViolationException race) {
            return claims.acquireExisting(key, hash)
                    .orElseThrow(() -> race);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FileProcessingEntity create(String companyId, String companyName, LocalDate submittedDate,
                                       String filename) {
        return repository.saveAndFlush(
                FileProcessingEntity.received(companyId, companyName, submittedDate, filename));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FileProcessingEntity markXmlStored(UUID requestId, String key) {
        FileProcessingEntity entity = required(requestId);
        entity.markXmlStored(key);
        return repository.save(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FileProcessingEntity markJsonStored(UUID requestId, String filename, String key) {
        FileProcessingEntity entity = required(requestId);
        entity.markJsonStored(filename, key);
        return repository.save(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FileProcessingEntity markCompleted(UUID requestId) {
        FileProcessingEntity entity = required(requestId);
        entity.markCompleted();
        return repository.save(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FileProcessingEntity markFailed(UUID requestId, String code, String message) {
        FileProcessingEntity entity = required(requestId);
        entity.markFailed(sanitize(code, 64), sanitize(message, 500));
        return repository.save(entity);
    }

    private FileProcessingEntity required(UUID requestId) {
        return repository.findByRequestId(requestId)
                .orElseThrow(() -> new ProcessingException("NOT_FOUND", "Processing request was not found").withRequestId(requestId));
    }

    private String sanitize(String value, int maxLength) {
        if (value == null) return null;
        String safe = value.replaceAll("[\\r\\n\\t<>]", " ").replaceAll("\\s+", " ").trim();
        return safe.substring(0, Math.min(maxLength, safe.length()));
    }
}
