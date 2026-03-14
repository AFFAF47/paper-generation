package com.example.exam.papergenerator.service;

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

    public ExamGeneratorService(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
    }

    public String generateExam(String subject, String className, String chapter, String pattern) {

        // 1. Create the Filter for our "Silo"
        // This ensures we only get notes for the specific class/subject
        String namespace = (subject + "-class" + className).toLowerCase();

        // 2. Fetch relevant notes from Pinecone
        // Replace the old SearchRequest line with this:
        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(chapter)
                        .topK(5) // Use the builder method
                        // This tells Pinecone to ONLY look in the specific class silo
                        .filterExpression("subject == '" + subject + "' && class == '" + className + "'")
                        .build()
        );

        String context = similarDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        // 3. Build the "Teacher" Prompt
        String userPrompt = String.format("""
                You are an expert school teacher. Use the following context from the class notes to generate an exam.
                
                CONTEXT FROM NOTES:
                %s
                
                EXAM PATTERN REQUEST:
                Subject: %s
                Class: %s
                Chapter: %s
                Pattern: %s
                
                INSTRUCTIONS:
                - Generate questions ONLY from the provided context.
                - Follow the pattern strictly.
                - Include a 'Memorandum' or 'Answer Key' at the end.
                """, context, subject, className, chapter, pattern);

        // 4. Call your Windows PC (Ollama)
        return chatClient.prompt()
                .user(userPrompt)
                .call()
                .content();
    }
}
