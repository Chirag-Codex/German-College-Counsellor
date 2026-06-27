package com.example.GermanCollege.model;

import jakarta.persistence.GeneratedValue;
import lombok.Data;
import org.springframework.aot.generate.Generated;

@Data
public class StudentProfile {


    private int id;

    private Double tenthMarks;

    private Double twelfthMarks;

    private Double cgpa;

    private Double ieltsScore;

    private String preferredCourse;

    private String preferredCity;

    private String germanLevel;

    private Double budget;
}