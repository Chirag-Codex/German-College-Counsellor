package com.example.GermanCollege.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProgramImportDto {

    private String source;
    private String universityName;
    private String city;
    private String programName;
    private String specialization;
    private String courseLanguage;
    private Double minCgpa;
    private Double minIelts;
    private Double tuitionFee;
    private String intake;
    private Boolean apsRequired;
    private String sourceUrl;
}