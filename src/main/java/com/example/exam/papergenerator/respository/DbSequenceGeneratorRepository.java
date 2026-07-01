package com.example.exam.papergenerator.respository;

import com.example.exam.papergenerator.model.DbSequenceModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DbSequenceGeneratorRepository extends MongoRepository<DbSequenceModel, String> {
}
