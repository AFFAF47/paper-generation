package com.example.exam.papergenerator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamRequest {
    private String id;        // MongoDB Document ID
    private String prompt;    // The actual instructions for the AI
    private String userEmail; // To notify the user when done
}
