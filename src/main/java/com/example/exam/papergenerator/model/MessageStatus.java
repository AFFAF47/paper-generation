package com.example.exam.papergenerator.model;

import lombok.Getter;

@Getter
public enum MessageStatus {
    PENDING("Pending"),
    GENERATING("Generating"),
    DONE("Done"),
    FAILED("Failed");

    private final String value;

    MessageStatus(String value) {
        this.value = value;
    }
}
