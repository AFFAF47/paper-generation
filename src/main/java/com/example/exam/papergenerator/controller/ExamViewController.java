package com.example.exam.papergenerator.controller;

import com.example.exam.papergenerator.model.ExamRecord;
import com.example.exam.papergenerator.service.ExamGeneratorService;
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

    public ExamViewController(ExamGeneratorService examService) {
        this.examService = examService;
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
    public String generateExam(
            @RequestParam String subject,
            @RequestParam String className,
            @RequestParam String chapter,
            @RequestParam String pattern,
            Model model) {

        String result = examService.generateExam(subject, className, chapter, pattern);

        // Pass the AI result back to the HTML page
        model.addAttribute("examPaper", result);
        model.addAttribute("subject", subject);
        model.addAttribute("chapter", chapter);

        return "exam-dashboard";
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

    //check with model and how to put this on UI
    @GetMapping("/history/{id}")
    public String viewHistoryById(@RequestParam String id, Model model) {
        ExamRecord history = examService.getHistoryById(id);
        model.addAttribute("historyList", history);
        model.addAttribute("subject", history.getSubject());
        model.addAttribute("className", history.getClassName());
        return "exam-history";
    }

    // 1. Show a simple search page for history
    @GetMapping("/history-view")
    public String showHistorySearch() {
        return "history-search"; // A small page to pick subject/class
    }

    // 2. Fetch and show the results
    @GetMapping("/history/results")
    public String getHistoryResults(@RequestParam String subject,
                                    @RequestParam String className,
                                    @RequestParam String id,
                                    Model model) {
        List<ExamRecord> history = examService.getHistory(subject, className, id);
        model.addAttribute("historyList", history);
        model.addAttribute("subject", subject);
        model.addAttribute("className", className);
        model.addAttribute("id", id);
        return "exam-history";
    }

    @GetMapping("/upload")
    public String showUploadPage() {
        return "upload-notes"; // This must match your HTML filename
    }
}
