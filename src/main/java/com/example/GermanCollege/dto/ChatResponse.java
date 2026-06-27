package com.example.GermanCollege.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private String conversationId;

    private String reply;

    private boolean profileComplete;

    private double completionPercentage;

    private StudentProfileDto profile;

    private List<RecommendationDto> recommendations;

    private String nextMissingField;
}