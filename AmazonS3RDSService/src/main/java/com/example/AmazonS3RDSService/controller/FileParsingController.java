package com.example.AmazonS3RDSService.controller;

import com.example.AmazonS3RDSService.dto.FileUploadRequest;
import com.example.AmazonS3RDSService.dto.ProcessResult;
import com.example.AmazonS3RDSService.service.FileProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/files")
@RestController
@RequiredArgsConstructor
public class FileParsingController {


    private final FileProcessingService processingService;

    @Operation(summary = "Validate and process an XML document")
    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProcessResult> process(@Valid @ModelAttribute FileUploadRequest fileUploadRequest) {
        ProcessResult result = processingService.process(fileUploadRequest);
        return ResponseEntity.status(result.replayed() ? HttpStatus.OK : HttpStatus.CREATED).body(result);
    }
}
