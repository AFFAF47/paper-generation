package com.example.exam.papergenerator.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "exam_history")
public class ExamRecord {
    @Id
    private String id;
    private String subject;
    private String className;
    private List<String> chapters;
    private String pattern;
    private MessageStatus status;
    private String content; // The AI generated text
    private LocalDateTime createdAt = LocalDateTime.now();
}