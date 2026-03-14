package com.example.exam.papergenerator.controller;

import com.example.exam.papergenerator.service.ExamGeneratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            @RequestParam String chapter,
            @RequestParam String pattern) {

        String paper = examService.generateExam(subject, className, chapter, pattern);
        return ResponseEntity.ok(paper);
    }
}
