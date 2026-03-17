package com.example.exam.papergenerator.respository;

import com.example.exam.papergenerator.model.ExamRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends MongoRepository<ExamRecord, String> {
    // This will let us show history for a specific class
    List<ExamRecord> findBySubjectAndClassNameOrderByCreatedAtDesc(String subject, String className);
}
