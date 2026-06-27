package com.example.GermanCollege.service;

import com.example.GermanCollege.dto.SourceProgramDto;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class CsvGeneratorService {

    public String generateCsv(List<SourceProgramDto> programs) throws Exception {

        String filePath = "src/main/resources/data/programs.csv";

        Files.createDirectories(
                Path.of("src/main/resources/data")
        );

        CsvMapper mapper = new CsvMapper();

        CsvSchema schema = mapper
                .schemaFor(SourceProgramDto.class)
                .withHeader();

        mapper.writer(schema)
                .writeValue(
                        new File(filePath),
                        programs
                );

        return filePath;
    }
}