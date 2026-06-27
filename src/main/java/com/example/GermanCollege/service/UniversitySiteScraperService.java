package com.example.GermanCollege.service;

import com.example.GermanCollege.dto.SourceProgramDto;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UniversitySiteScraperService
        implements ProgramSourceService {

    @Override
    public List<SourceProgramDto> fetchPrograms() {

        List<SourceProgramDto> result =
                new ArrayList<>();

        SourceProgramDto dto =
                new SourceProgramDto();

        dto.setSource("University");
        dto.setUniversityName("KIT");
        dto.setCity("Karlsruhe");
        dto.setProgramName("Data Science");
        dto.setSpecialization("Machine Learning");
        dto.setCourseLanguage("English");
        dto.setMinCgpa(7.2);
        dto.setMinIelts(6.5);
        dto.setTuitionFee(1500.0);
        dto.setIntake("Winter");
        dto.setApsRequired(true);

        result.add(dto);

        return result;
    }
}