package com.example.exam.papergenerator.service;

import com.example.exam.papergenerator.model.ExamRecord;
import com.example.exam.papergenerator.respository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamGeneratorService {

    private final RedisProducerService redisProducerService;
    private final VectorStore vectorStore;
    private final ExamRepository examRepository; // Injecting MongoDB repository

    public String generateExam(String subject, String className, String chapter, String pattern) {
        // 1. Just save a shell record in MongoDB so the user sees "Pending"
        ExamRecord record = new ExamRecord();
        record.setSubject(subject);
        record.setClassName(className);
        record.setChapter(chapter);
        record.setPattern(pattern);
        record.setContent("GENERATING...");
        ExamRecord savedRecord = examRepository.save(record);

        // 2. Put the raw request into Redis
        // We send 'chapter' so the worker can do the Pinecone search locally
        redisProducerService.publishExamTask(savedRecord.getId(), subject, className, chapter, pattern);

        return "SUCCESS: Your request is queued. Check the history in a moment!";
    }

    public List<ExamRecord> getHistory(String subject, String className) {
        return examRepository.findBySubjectAndClassNameOrderByCreatedAtDesc(subject, className);
    }
}
