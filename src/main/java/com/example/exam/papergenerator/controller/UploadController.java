package com.example.exam.papergenerator.controller;

import com.example.exam.papergenerator.service.IngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class UploadController {

    private final IngestionService ingestionService;
    private final S3Presigner s3Presigner;

    @Value("${app.bucket-name:exam-paper-generator-bucket-558260070804-ap-south-1-an}")
    String bucketName;

    @GetMapping("/presigned-url")
    public ResponseEntity<Map<String, String>> getPresignedUrl(@RequestParam("fileName") String fileName) {
        // 1. Generate a unique key so files with identical names never overwrite each other
        String objectKey = "uploads/" + UUID.randomUUID() + "-" + fileName;

        // 2. Request a Presigned PUT URL valid for 10 minutes
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType("application/pdf")
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        String uploadUrl = presignedRequest.url().toString();

        // 3. Return both the upload URL (for direct PUT) and the objectKey (for processing later)
        Map<String, String> response = new HashMap<>();
        response.put("uploadUrl", uploadUrl);
        response.put("objectKey", objectKey);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("objectKey") String objectKey,
                                         @RequestParam("subject") String subject,
                                         @RequestParam("className") String className,
                                         @RequestParam("chapter") String chapter) {

        ingestionService.uploadNotes(objectKey, subject, className, chapter);
        return ResponseEntity.ok("Notes uploaded and vectorized successfully!");
    }
}