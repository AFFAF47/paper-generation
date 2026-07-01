package com.example.exam.papergenerator.service;

import com.example.exam.papergenerator.model.ExamRecord;
import com.example.exam.papergenerator.respository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamGeneratorService {

    private final RedisProducerService redisProducerService;
    private final DbSequenceGeneratorService dbSequenceGeneratorService;
    private final MongoTemplate mongoTemplate;
    private final VectorStore vectorStore;
    private final ExamRepository examRepository; // Injecting MongoDB repository

    public String generateExam(String subject, String className, String chapter, String pattern) {
        // 1. Just save a shell record in MongoDB so the user sees "Pending"
        ExamRecord record = new ExamRecord();
        record.setId(dbSequenceGeneratorService.GenerateSequence());
        record.setSubject(subject);
        record.setClassName(className);
        record.setChapter(chapter);
        record.setPattern(pattern);
        record.setContent("GENERATING...");
        ExamRecord savedRecord = examRepository.save(record);

        // 2. Put the raw request into Redis
        // We send 'chapter' so the worker can do the Pinecone search locally
        redisProducerService.publishExamTask(savedRecord.getId(), subject, className, chapter, pattern);

        return "SUCCESS: Your request is queued with Id: " + savedRecord.getId() + " . Check the history in a moment!";
    }

    public List<ExamRecord> getHistory(String subject, String className, String id) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        // 1. Dynamically add criteria if parameters are present
        if (subject != null && !subject.isEmpty()) {
            criteriaList.add(Criteria.where("subject").is(subject));
        }
        if (className != null && !className.isEmpty()) {
            criteriaList.add(Criteria.where("className").is(className));
        }
        if (id != null && !id.isEmpty()) {
            criteriaList.add(Criteria.where("id").is(id)); // or "_id" depending on your mapping
        }

        // Guard clause: Prevent pulling the whole database if everything is empty
        if (criteriaList.isEmpty()) {
            throw new IllegalArgumentException("At least one search parameter must be provided");
        }

        // 2. Join all criteria together using AND logic
        query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));

        // 3. Add your sorting
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));

        return mongoTemplate.find(query, ExamRecord.class);
    }

    public ExamRecord getHistoryById(String id) {
        return examRepository.findById(id).orElseThrow( () -> new RuntimeException("History Not Found"));
    }
}
