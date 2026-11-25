package com.example.exam.papergenerator.controller;

import com.example.exam.papergenerator.model.ExamRecord;
import com.example.exam.papergenerator.model.KnowledgeTopic;
import com.example.exam.papergenerator.respository.ExamRepository;
import com.example.exam.papergenerator.respository.KnowledgeTopicRepository;
import com.example.exam.papergenerator.service.ExamGeneratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/exams")
public class ExamViewController {

    private final ExamGeneratorService examService;
    private final KnowledgeTopicRepository topicRepository;
    private final ExamRepository examRepository;

    public ExamViewController(ExamGeneratorService examService,
                              KnowledgeTopicRepository topicRepository,
                              ExamRepository examRepository) {
        this.examService = examService;
        this.topicRepository = topicRepository;
        this.examRepository = examRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/exams"; // Automatically sends the user to the generator
    }

    // This loads the initial empty page
    @GetMapping
    public String showExamPage() {
        return "exam-dashboard";
    }

    // This handles the form submission
    @PostMapping("/generate")
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

    @GetMapping("/history")
    public String viewHistory(@RequestParam String subject,
                              @RequestParam String className,
                              @RequestParam String id,
                              Model model) {
        List<ExamRecord> history = examService.getHistory(subject, className, id);
        model.addAttribute("historyList", history);
        model.addAttribute("subject", subject);
        return "exam-history"; // We will create this HTML next
    }

    //TODO: check if this needs to be addressed
    //check with model and how to put this on UI
    @GetMapping("/history/{id}")
    public String viewHistoryById(@RequestParam String id, Model model) {
        ExamRecord history = examService.getHistoryById(id);
        model.addAttribute("historyList", history);
        return "exam-history";
    }

    // 1. Show a simple search page for history
    @GetMapping("/history-view")
    public String showHistorySearch() {
        return "history-search"; // A small page to pick subject/class
    }

    // 2. Fetch and show the results
    @GetMapping("/exams/history/results")
    public String viewHistoryResults(@RequestParam(required = false) String topicId,
                                     @RequestParam(required = false) String id,
                                     Model model) {
        List<ExamRecord> historyList;

        if (id != null && !id.isBlank()) {
            historyList = examRepository.findById(id.trim())
                    .map(List::of)
                    .orElse(List.of());
        } else if (topicId != null && !topicId.isBlank()) {
            historyList = examRepository.findByTopicIdOrderByCreatedAtDesc(topicId.trim().toLowerCase());
            topicRepository.findById(topicId.trim().toLowerCase())
                    .ifPresent(t -> model.addAttribute("topicName", t.getName()));
        } else {
            historyList = examRepository.findAllByOrderByCreatedAtDesc();
        }

        model.addAttribute("historyList", historyList);
        return "exam-history";
    }

    @GetMapping("/upload")
    public String showUploadPage() {
        return "upload-notes"; // This must match your HTML filename
    }
}
