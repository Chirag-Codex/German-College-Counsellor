package com.example.GermanCollege.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecommendationDto {

    private String universityName;

    private String city;

    private String programName;

    private String specialization;

    private String courseLanguage;

    private Double minCgpa;

    private Double minIelts;

    private Double tuitionFee;

    private String intake;

    private Integer matchScore;

    private String reason;
}