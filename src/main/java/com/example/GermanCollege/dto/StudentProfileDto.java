package com.example.GermanCollege.dto;

import lombok.Data;

@Data
public class StudentProfileDto {

    private Double tenthMarks;

    private Double twelfthMarks;

    private Double cgpa;

    private Double ielts;

    private String preferredCourse;

    private Double budget;

    private String preferredCity;

    private String germanLevel;
}