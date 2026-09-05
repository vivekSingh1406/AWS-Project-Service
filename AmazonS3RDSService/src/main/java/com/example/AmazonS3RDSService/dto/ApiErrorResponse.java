package com.example.AmazonS3RDSService.dto;

import com.example.AmazonS3RDSService.exception.ValidationDetail;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ApiErrorResponse(Instant timestamp, String correlationId, UUID requestId, int status,
                               String code, String message, List<ValidationDetail> details) {}
