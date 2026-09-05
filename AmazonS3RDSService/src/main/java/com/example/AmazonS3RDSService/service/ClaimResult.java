package com.example.AmazonS3RDSService.service;

import com.example.AmazonS3RDSService.entity.FileProcessingEntity;

public record ClaimResult(ClaimDisposition disposition, FileProcessingEntity entity) {
}
