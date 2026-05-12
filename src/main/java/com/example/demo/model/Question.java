package com.example.demo.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    private String text;

    // Exactly 4 answer options
    private List<String> options;

    // 0-based index of the correct option (0=A, 1=B, 2=C, 3=D)
    private int correctIndex;

    // How many seconds players have to answer
    private int timeLimitSeconds;
}