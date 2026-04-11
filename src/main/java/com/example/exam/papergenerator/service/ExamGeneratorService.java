package com.example.exam.papergenerator.service;

import com.example.exam.papergenerator.model.ExamRecord;
import com.example.exam.papergenerator.model.MessageStatus;
import com.example.exam.papergenerator.respository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamGeneratorService {

    private final RedisProducerService redisProducerService;
    private final VectorStore vectorStore;
    private final ExamRepository examRepository; // Injecting MongoDB repository

    public String generateExam(String subject, String className, String chapter, String pattern) {

        String subjectLower = subject.toLowerCase();
        String classLower = className.toLowerCase();
        // 1. Fetch relevant notes from Pinecone
        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(chapter)
                        .topK(5)
                        .filterExpression(String.format("subject == '%s' && class == '%s'", subjectLower, classLower))
                        .build()
        );

        String context = similarDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        // 2. Updated "Strict Format" Prompt
        String userPrompt = String.format("""
                You are an expert school teacher. Use the provided context to generate an exam.
                
                CONTEXT:
                %s
                
                REQUEST:
                Subject: %s | Class: %s | Chapter: %s
                Pattern: %s
                
                STRICT FORMATTING RULES:
                1. SECTION 1: THE QUESTION PAPER
                   - List all questions (MCQs, Short/Long Answers).
                   - DO NOT include answers in this section.
                2. SECTION 2: THE ANSWER KEY
                   - Place this at the very end after a '--- END OF PAPER ---' marker.
                   - Provide all correct options and marking points here only.
                """, context, subject, className, chapter, pattern);

        // 4. SAVE TO MONGODB ATLAS
        ExamRecord record = new ExamRecord();
        try {
            record.setSubject(subject);
            record.setClassName(className);
            record.setChapter(chapter);
            record.setPattern(pattern);
            record.setStatus(MessageStatus.PENDING);
            record.setContent("Generating...");
            examRepository.save(record); // This pushes it to your Atlas cluster
        } catch (Exception e) {
            System.err.println("Database Save Failed: " + e.getMessage());
            // We still return even if DB fails so the user gets their paper
        }

        // 4. ASYNC HANDOFF: Push to Redis Stream
        try {
            // We pass the Doc ID so the worker knows which record to update later
            //TODO: Fix this email Id
            redisProducerService.publishExamTask(record.getId(), userPrompt, "your-email@example.com");
        } catch (Exception e) {
            return "ERROR: Failed to queue task. Please check Redis connection.";
        }

        return "SUCCESS: Your paper is being generated! Refresh the history in a minute.";
    }

    public List<ExamRecord> getHistory(String subject, String className) {
        return examRepository.findBySubjectAndClassNameOrderByCreatedAtDesc(subject, className);
    }
}
