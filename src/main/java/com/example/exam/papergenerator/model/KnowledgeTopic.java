package com.example.exam.papergenerator.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "knowledge_topic")
public class KnowledgeTopic {

    @Id
    private String id;
    private String name;
    private VectorStatus status; // Vector Status: Indexing, Ready, Failed
    private int totalChunks;
    private Instant updatedAt;

    @Builder.Default
    private List<TopicSource> sources = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicSource {
        private String url;   // web url or s3 objectkey
        private String chapter;  // Name of the chapter or module
        private SourceType type;   // PDF, URL
        private String fileName;
        private TopicStatus status;  // "Pending, Processing, Done, Failed"
        private int chunkCount;
        private String error;
    }

    public enum TopicStatus {
        PENDING,
        PROCESSING,
        DONE,
        FAILED
    }

    public enum VectorStatus {
        INDEXING,
        READY,
        FAILED
    }
}
