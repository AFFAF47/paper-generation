package com.example.exam.papergenerator.controller;

import com.example.exam.papergenerator.service.IngestionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/notes")
public class UploadController {

    private final IngestionService ingestionService;

    public UploadController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file,
                                         @RequestParam("subject") String subject,
                                         @RequestParam("class") String className,
                                         @RequestParam("chapter") String chapter) {

        ingestionService.uploadNotes(file, subject, className, chapter);
        return ResponseEntity.ok("Notes uploaded and vectorized successfully!");
    }
}