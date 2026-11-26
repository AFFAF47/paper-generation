package com.example.exam.papergenerator.controller;

import com.example.exam.papergenerator.model.IngestionRequest;
import com.example.exam.papergenerator.model.KnowledgeTopic;
import com.example.exam.papergenerator.service.TopicIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicIngestionController {

    private final TopicIngestionService ingestionService;

    @PostMapping("/ingest")
    public ResponseEntity<?> ingestSource(@RequestBody IngestionRequest request) {
        if (request.getTopicId() == null || request.getTopicId().isBlank()) {
            return ResponseEntity.badRequest().body("topicId is required.");
        }
        try {
            KnowledgeTopic updatedTopic = ingestionService.ingestSource(request);
            return ResponseEntity.ok(Map.of(
                    "message", "Source ingested successfully",
                    "topicId", updatedTopic.getId(),
                    "status", updatedTopic.getStatus()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
