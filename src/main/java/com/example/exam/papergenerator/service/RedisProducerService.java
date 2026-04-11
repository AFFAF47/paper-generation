package com.example.exam.papergenerator.service;

import com.example.exam.papergenerator.model.ExamRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RedisProducerService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String STREAM_KEY = "exam_tasks";

    public void publishExamTask(String documentId, String prompt, String email) {
        ExamRequest request = new ExamRequest(documentId, prompt, email);

        // Create a Redis Stream Record
        ObjectRecord<String, ExamRequest> record = StreamRecords.newRecord()
                .ofObject(request)
                .withStreamKey(STREAM_KEY);

        // Push to Upstash Redis
        this.redisTemplate.opsForStream().add(record);

        System.out.println("Task published to Redis Stream: " + documentId);
    }
}
