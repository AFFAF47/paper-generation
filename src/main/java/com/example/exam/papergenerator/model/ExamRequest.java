package com.example.exam.papergenerator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamRequest {
    private String id;         // MongoDB Document ID
    private String subject;    // Needed for Pinecone Filter
    private String className;  // Needed for Pinecone Filter
    private String chapter;    // Needed for Vector Search
    private String pattern;    // Needed for AI Prompt
}
