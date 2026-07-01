package com.example.exam.papergenerator.service;

import com.example.exam.papergenerator.model.DbSequenceModel;
import com.example.exam.papergenerator.respository.DbSequenceGeneratorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DbSequenceGeneratorService {

    private final DbSequenceGeneratorRepository dbSequenceGeneratorRepository;
    private static final String SEQUENCE = "exam_sequence";

    public String GenerateSequence() {
        DbSequenceModel dbSequenceModel = dbSequenceGeneratorRepository.findById(SEQUENCE).orElseThrow(
                () -> new RuntimeException("Sequence not found"));

        String current = dbSequenceModel.getSequence();
        long currentSequence = Long.parseLong(current.split("_")[1]);
        currentSequence++;
        String updatedSequence = current.split("_")[0] + "_" + currentSequence;
        dbSequenceModel.setSequence(updatedSequence);
        dbSequenceGeneratorRepository.save(dbSequenceModel);
        return dbSequenceModel.getSequence();
    }
}
