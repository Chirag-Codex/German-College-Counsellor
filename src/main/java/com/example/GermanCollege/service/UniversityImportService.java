package com.example.GermanCollege.service;

import com.example.GermanCollege.dto.ProgramImportDto;
import com.example.GermanCollege.model.Program;
import com.example.GermanCollege.model.University;
import com.example.GermanCollege.repository.ProgramRepository;
import com.example.GermanCollege.repository.UniversityRepository;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UniversityImportService {

    private final UniversityRepository universityRepository;
    private final ProgramRepository programRepository;

    @Transactional
    public void importFromCsvFile(String csvPath) throws Exception {
        log.info("Starting import from CSV: {}", csvPath);

        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader();

        try (InputStream inputStream = new FileInputStream(csvPath)) {
            MappingIterator<ProgramImportDto> iterator = mapper.readerFor(ProgramImportDto.class)
                    .with(schema)
                    .readValues(inputStream);

            List<ProgramImportDto> records = iterator.readAll();
            log.info("Found {} records in CSV", records.size());

            int importedCount = 0;

            for (ProgramImportDto dto : records) {
                try {
                    log.info("Processing: {} - {}", dto.getUniversityName(), dto.getProgramName());

                    // Save or get university
                    University university = universityRepository.findByName(dto.getUniversityName())
                            .orElseGet(() -> {
                                log.info("Creating new university: {}", dto.getUniversityName());
                                University u = new University();
                                u.setName(dto.getUniversityName());
                                u.setCity(dto.getCity());
                                return universityRepository.save(u);
                            });

                    log.info("University ID: {}", university.getId());

                    // Create and save program
                    Program program = new Program();
                    program.setSource(dto.getSource());
                    program.setProgramName(dto.getProgramName());
                    program.setSpecialization(dto.getSpecialization());
                    program.setCourseLanguage(dto.getCourseLanguage());
                    program.setMinCgpa(dto.getMinCgpa());
                    program.setMinIelts(dto.getMinIelts());
                    program.setTuitionFee(dto.getTuitionFee() != null ? dto.getTuitionFee() : 0.0);
                    program.setIntake(dto.getIntake());
                    program.setApsRequired(dto.getApsRequired() != null && dto.getApsRequired());
                    program.setSourceUrl(dto.getSourceUrl());
                    program.setLastUpdated(LocalDateTime.now());
                    program.setUniversity(university);

                    Program saved = programRepository.save(program);
                    log.info("✓✓✓ SUCCESS: Saved program with ID: {} - {} ✓✓✓",
                            saved.getId(),
                            saved.getProgramName());

                    importedCount++;


                    programRepository.flush();

                } catch (Exception e) {
                    log.error("✗ FAILED to import: {} - Error: {}", dto.getProgramName(), e.getMessage(), e);
                }
            }

            log.info("========================================");
            log.info("IMPORT SUMMARY: Successfully imported {} out of {} programs",
                    importedCount, records.size());
            log.info("========================================");
        }
    }
}