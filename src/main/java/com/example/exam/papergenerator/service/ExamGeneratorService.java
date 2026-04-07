package com.example.exam.papergenerator.service;

import com.example.exam.papergenerator.model.ExamRecord;
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
public class ExamGeneratorService {

    @Value("${spring.ai.ollama.base-url}")
    private String ollamaUrl;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ExamRepository examRepository; // Injecting MongoDB repository

    public ExamGeneratorService(ChatClient.Builder builder, VectorStore vectorStore, ExamRepository examRepository) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
        this.examRepository = examRepository;
    }

    public String generateExam(String subject, String className, String chapter, String pattern) {

        String subjectLower = subject.toLowerCase();
        String classLower = className.toLowerCase();
        // 1. Fetch relevant notes from Pinecone

        // NEW: Check if the Home PC is actually reachable
        if (!isModelOnline()) {
            return "ERROR: The AI Model is currently offline. Please ensure the home computer is running and connected to Tailscale.";
        }

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

        // 3. Call AI
        String aiResponse;
        try {
            aiResponse = chatClient.prompt()
                    .user(userPrompt)
                    .call()
                    .content();
        } catch (Exception e) {
            // If the connection drops MID-GENERATION, catch it here
            System.err.println("AI Call Failed: " + e.getMessage());
            return "ERROR: Connection to the Home PC was lost during generation. Please try again.";
        }

        // 4. SAVE TO MONGODB ATLAS
        try {
            ExamRecord record = new ExamRecord();
            record.setSubject(subject);
            record.setClassName(className);
            record.setChapter(chapter);
            record.setPattern(pattern);
            record.setContent(aiResponse);
            examRepository.save(record); // This pushes it to your Atlas cluster
        } catch (Exception e) {
            System.err.println("Database Save Failed: " + e.getMessage());
            // We still return the aiResponse even if DB fails so the user gets their paper
        }

        return aiResponse;
    }

    // Heartbeat Helper Method
    private boolean isModelOnline() {
        try {
            // We use a simple socket check or a quick call to Ollama's tags endpoint
            // This is much faster than waiting for a full generation to fail
            java.net.URL url = new java.net.URL(ollamaUrl + "/api/tags");
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(2000); // 2 seconds limit
            connection.connect();
            return (connection.getResponseCode() == 200);
        } catch (Exception e) {
            return false;
        }
    }

    public List<ExamRecord> getHistory(String subject, String className) {
        return examRepository.findBySubjectAndClassNameOrderByCreatedAtDesc(subject, className);
    }
}
