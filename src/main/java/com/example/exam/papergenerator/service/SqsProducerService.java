package com.example.exam.papergenerator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class SqsProducerService {

    private static final Logger log = LoggerFactory.getLogger(SqsProducerService.class);

    private final ObjectMapper objectMapper;
    private final String queueName;
    private final SqsClient sqsClient;

    public SqsProducerService(
            ObjectMapper objectMapper,
            @Value("${app.sqs.exam-generation-queue-name:exam-generation-queue}") String queueName,
            SqsClient sqsClient) {
        this.objectMapper = objectMapper;
        this.queueName = queueName;
        this.sqsClient = sqsClient;
    }

    @SneakyThrows
    public void publishExamTask(String examId, String topicId, List<String> chapters, String pattern) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("examId", examId);
            payload.put("topicId", topicId);
            payload.put("chapters", chapters != null ? chapters : List.of());
            payload.put("pattern", pattern);

            String messageBody = objectMapper.writeValueAsString(payload);

            sqsClient.sendMessage(SendMessageRequest.builder()
                            .queueUrl(queueName)
                            .messageBody(messageBody)
                            .build());

            log.info("Published exam generation task to SQS: examId={}, topicId={}, chapters={}",
                    examId, topicId, chapters);

            log.info("✅ Task Published successfully to SQS for examId: {}, topicId: {}", examId, topicId);
        } catch (Exception e) {
            log.error("Failed to publish exam task for examId={}", examId, e);
            throw new RuntimeException("Failed to queue exam generation task: " + e.getMessage(), e);
        }
    }
}