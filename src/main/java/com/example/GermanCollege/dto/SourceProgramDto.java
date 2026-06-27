package com.example.GermanCollege.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({
        "source",
        "universityName",
        "city",
        "programName",
        "specialization",
        "courseLanguage",
        "minCgpa",
        "minIelts",
        "tuitionFee",
        "intake",
        "apsRequired",
        "sourceUrl"
})
public class SourceProgramDto {

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