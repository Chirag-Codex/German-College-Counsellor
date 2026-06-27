package com.example.GermanCollege.service;

import com.example.GermanCollege.dto.SourceProgramDto;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataNormalizationService {

    public List<SourceProgramDto> normalize(
            List<SourceProgramDto> data
    ) {

        Map<String, SourceProgramDto> unique =
                new HashMap<>();

        for (SourceProgramDto dto : data) {

            String key =
                    dto.getUniversityName()
                            + "-"
                            + dto.getProgramName();

            unique.putIfAbsent(
                    key,
                    dto
            );
        }

        return unique.values()
                .stream()
                .collect(Collectors.toList());
    }
}