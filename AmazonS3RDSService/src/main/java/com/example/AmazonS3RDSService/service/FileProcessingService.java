package com.example.AmazonS3RDSService.service;

import com.example.AmazonS3RDSService.dto.FileUploadRequest;
import com.example.AmazonS3RDSService.dto.ProcessResult;

public interface FileProcessingService {

    ProcessResult process(FileUploadRequest fileUploadRequest);
}
