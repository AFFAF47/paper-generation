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

    private final RedisTemplate<String, Object> redisTemplate; // Note: Changed to Object for DTO support

    private static final String STREAM_KEY = "exam_tasks";

    public void publishExamTask(String id, String subject, String className, String chapter, String pattern) {
        // Create the request with all 5 fields
        ExamRequest request = new ExamRequest(id, subject, className, chapter, pattern);

        ObjectRecord<String, ExamRequest> record = StreamRecords.newRecord()
                .ofObject(request)
                .withStreamKey(STREAM_KEY);

        this.redisTemplate.opsForStream().add(record);

        System.out.println("Task queued for Home Worker. ID: " + id);
    }
}
