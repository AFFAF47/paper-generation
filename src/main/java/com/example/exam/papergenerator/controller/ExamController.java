package com.example.exam.papergenerator.controller;

import com.example.exam.papergenerator.model.KnowledgeTopic;
import com.example.exam.papergenerator.respository.KnowledgeTopicRepository;
import com.example.exam.papergenerator.service.ExamGeneratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamGeneratorService examService;
    private final KnowledgeTopicRepository topicRepository;

    public ExamController(ExamGeneratorService examService, KnowledgeTopicRepository topicRepository) {
        this.examService = examService;
        this.topicRepository = topicRepository;
    }

    @GetMapping("/generate")
    public ResponseEntity<String> generate(
            @RequestParam String topicId,
            @RequestParam(required = false) List<String> selectedChapters,
            @RequestParam String pattern,
            Model model) {

        KnowledgeTopic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic Not Found: " + topicId));

        List<String> normalizeChapters = (selectedChapters != null)
                ? selectedChapters.stream().map(c -> c.trim().toLowerCase()).toList() : List.of();

        String generatedPaper = examService.generateExam(topicId, normalizeChapters, pattern);

        model.addAttribute("topics", topicRepository.findAll());
        model.addAttribute("topicName", topic.getName());
        model.addAttribute("examPaper", generatedPaper);
        return ResponseEntity.ok(generatedPaper);
    }
}
