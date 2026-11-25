package com.example.exam.papergenerator.respository;

import com.example.exam.papergenerator.model.KnowledgeTopic;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeTopicRepository extends MongoRepository<KnowledgeTopic, String> {

}
