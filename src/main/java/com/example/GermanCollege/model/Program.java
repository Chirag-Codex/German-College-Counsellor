package com.example.GermanCollege.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String programName;

    private String specialization;

    private String courseLanguage;

    private Double minCgpa;

    private Double minIelts;

    private Double tuitionFee;

    private String intake;

    private Boolean apsRequired;

    private String source;

    private String sourceUrl;

    private LocalDateTime lastUpdated;

    @ManyToOne
    @JoinColumn(name = "university_id")
    private University university;
}