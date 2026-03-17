package com.example.exam.papergenerator.controller;

import com.example.exam.papergenerator.model.ExamRecord;
import com.example.exam.papergenerator.respository.ExamRepository;
import com.example.exam.papergenerator.service.PdfGeneratorService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pdf")
public class PdfDownloadController {

    private final ExamRepository examRepository;
    private final PdfGeneratorService pdfService;

    public PdfDownloadController(ExamRepository examRepository, PdfGeneratorService pdfService) {
        this.examRepository = examRepository;
        this.pdfService = pdfService;
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String id) {
        ExamRecord record = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        byte[] pdfBytes = pdfService.generateExamPdf(record);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Exam_" + record.getChapter() + ".pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
