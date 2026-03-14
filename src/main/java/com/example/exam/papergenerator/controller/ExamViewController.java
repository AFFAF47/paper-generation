package com.example.exam.papergenerator.controller;

import com.example.exam.papergenerator.service.ExamGeneratorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/exams")
public class ExamViewController {

    private final ExamGeneratorService examService;

    public ExamViewController(ExamGeneratorService examService) {
        this.examService = examService;
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
}
