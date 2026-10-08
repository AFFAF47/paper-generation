package com.example.exam.papergenerator.service;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SqsProducerService {

    private static final Logger log = LoggerFactory.getLogger(SqsProducerService.class);

    private final SqsTemplate sqsTemplate;
    private final String queueName;

    public SqsProducerService(
            SqsTemplate sqsTemplate,
            @Value("${app.sqs.queue-name:exam-generation-queue}") String queueName) {
        this.sqsTemplate = sqsTemplate;
        this.queueName = queueName;
    }

    public void publishExamTask(String id, String subject, String className, String chapter, String pattern) {
        Map<String, String> taskMap = new HashMap<>();
        taskMap.put("id", id);
        taskMap.put("subject", subject);
        taskMap.put("className", className);
        taskMap.put("chapter", chapter);
        taskMap.put("pattern", pattern);

        // Sends message to SQS queue with automatic JSON serialization
        this.sqsTemplate.send(to -> to
                .queue(this.queueName)
                .payload(taskMap)
        );

        log.info("✅ Task Published successfully to SQS for ID: {}", id);
    }
}