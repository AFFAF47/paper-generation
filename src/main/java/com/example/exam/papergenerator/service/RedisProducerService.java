package com.example.exam.papergenerator.service;

import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RedisProducerService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Use Constructor Injection instead of @Autowired on the field
    public RedisProducerService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publishExamTask(String id, String subject, String className, String chapter, String pattern) {
        // Now this.redisTemplate will never be null
        Map<String, String> taskMap = new HashMap<>();
        taskMap.put("id", id);
        taskMap.put("subject", subject);
        taskMap.put("className", className);
        taskMap.put("chapter", chapter);
        taskMap.put("pattern", pattern);

        this.redisTemplate.opsForStream().add(StreamRecords.newRecord()
                .ofMap(taskMap)
                .withStreamKey("exam_tasks"));

        System.out.println("✅ Task Published successfully!");
    }
}
