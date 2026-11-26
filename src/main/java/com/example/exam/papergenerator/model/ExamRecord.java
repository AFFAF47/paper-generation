package com.example.exam.papergenerator.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "exam_history")
public class ExamRecord {
    @Id
    private String id;
    private String topicId;
    private List<String> chapters;
    private String pattern;
    private String content; // The AI generated text
    private String status;
    private Instant createdAt = Instant.now();
}