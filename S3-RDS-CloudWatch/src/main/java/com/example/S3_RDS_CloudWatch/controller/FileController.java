package com.example.S3_RDS_CloudWatch.controller;


import com.example.S3_RDS_CloudWatch.dto.DownloadUrlResponse;
import com.example.S3_RDS_CloudWatch.dto.FileUploadResponse;
import com.example.S3_RDS_CloudWatch.model.FileMetadata;
import com.example.S3_RDS_CloudWatch.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * POST /api/files/upload
     * multipart/form-data with a single "file" part.
     */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("Received upload request: name={}, size={} bytes", file.getOriginalFilename(), file.getSize());
        FileUploadResponse response = fileService.uploadFile(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/files
     * Lists all stored file metadata records.
     */
    @GetMapping
    public ResponseEntity<List<FileMetadata>> listFiles() {
        return ResponseEntity.ok(fileService.listFiles());
    }

    /**
     * GET /api/files/{id}/download
     * Returns a pre-signed S3 URL the client can use to download directly from S3.
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<DownloadUrlResponse> getDownloadUrl(@PathVariable Long id) {
        DownloadUrlResponse response = fileService.generateDownloadUrl(id);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/files/{id}
     * Removes both the S3 object and its RDS metadata record.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) {
        fileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
}
