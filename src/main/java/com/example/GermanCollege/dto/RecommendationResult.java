package com.example.GermanCollege.dto;

import com.example.GermanCollege.model.Program;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecommendationResult {

    private Program program;

    private int score;
}