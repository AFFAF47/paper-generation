package com.example.exam.papergenerator.model;

import lombok.Data;

@Data
public class IngestionRequest {
    private String topicId;
    private String topicName;
    private String chapter;   //eg: "LightReflection or SystemDesign Module 1
    private SourceType sourceType;  // eg: URL or PDF
    private String url;
    private String objectKey;
    private String fileName;
}
