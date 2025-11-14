package com.example.project.controller;



import com.example.project.model.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/data")
public class UploadController {

    @Autowired
    public S3Service s3Service;

    @PostMapping
    public ResponseEntity<String> upload(@RequestBody UserData userData) {
        String fileKey = s3Service.uploadToS3(userData);
        return ResponseEntity.ok("Uploaded to S3 with key: " + fileKey);
    }
}
