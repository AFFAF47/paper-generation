package com.example.exam.papergenerator.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document("db_sequence")
public class DbSequenceModel {

    private String id;
    private String sequence;
}
