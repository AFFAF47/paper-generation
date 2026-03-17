package com.example.exam.papergenerator.service;

import com.example.exam.papergenerator.model.ExamRecord;
import com.example.exam.papergenerator.respository.ExamRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamGeneratorService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ExamRepository examRepository; // Injecting MongoDB repository

    public ExamGeneratorService(ChatClient.Builder builder, VectorStore vectorStore, ExamRepository examRepository) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
        this.examRepository = examRepository;
    }

    public String generateExam(String subject, String className, String chapter, String pattern) {

        // 1. Fetch relevant notes from Pinecone
        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(chapter)
                        .topK(5)
                        .filterExpression("subject == '" + subject + "' && class == '" + className + "'")
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
        String aiResponse = chatClient.prompt()
                .user(userPrompt)
                .call()
                .content();

        // 4. SAVE TO MONGODB ATLAS
        ExamRecord record = new ExamRecord();
        record.setSubject(subject);
        record.setClassName(className);
        record.setChapter(chapter);
        record.setPattern(pattern);
        record.setContent(aiResponse);

        examRepository.save(record); // This pushes it to your Atlas cluster

        return aiResponse;
    }

    public List<ExamRecord> getHistory(String subject, String className) {
        return examRepository.findBySubjectAndClassNameOrderByCreatedAtDesc(subject, className);
    }
}
