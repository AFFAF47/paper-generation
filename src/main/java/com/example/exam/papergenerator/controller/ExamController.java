package com.example.exam.papergenerator.controller;

import com.example.exam.papergenerator.service.ExamGeneratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamGeneratorService examService;

    public ExamController(ExamGeneratorService examService) {
        this.examService = examService;
    }

    @GetMapping("/generate")
    public ResponseEntity<String> generate(
            @RequestParam String subject,
            @RequestParam String className,
            @RequestParam String pattern,
            @RequestBody List<String> chapters) {

        String paper = examService.generateExam(subject, className, chapters, pattern);
        return ResponseEntity.ok(paper);
    }
}
