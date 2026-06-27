package com.example.GermanCollege.service;

import com.example.GermanCollege.dto.SourceProgramDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UniversityApiService {

    public List<SourceProgramDto> fetchPrograms() {

        List<SourceProgramDto> programs =
                new ArrayList<>();

        SourceProgramDto dto =
                new SourceProgramDto();

        dto.setSource("API");
        dto.setUniversityName("TU Munich");
        dto.setCity("Munich");
        dto.setProgramName("Computer Science");
        dto.setSpecialization("Artificial Intelligence");
        dto.setCourseLanguage("English");
        dto.setMinCgpa(7.5);
        dto.setMinIelts(6.5);
        dto.setTuitionFee(0.0);
        dto.setIntake("Winter");
        dto.setApsRequired(true);

        programs.add(dto);

        return programs;
    }
}