package com.example.GermanCollege.controller;

import com.example.GermanCollege.dto.SourceProgramDto;
import com.example.GermanCollege.model.Program;
import com.example.GermanCollege.model.University;
import com.example.GermanCollege.repository.ProgramRepository;
import com.example.GermanCollege.repository.UniversityRepository;
import com.example.GermanCollege.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FlowTestController {

    private final DaadScraperService daadScraperService;
    private final HochschulkompassScraperService hochschulkompassScraperService;
    private final UniversitySiteScraperService universitySiteScraperService;
    private final UniversityImportService universityImportService;
    private final UniversityRepository universityRepository;
    private final ProgramRepository programRepository;

    @GetMapping("/test-complete-flow")
    public Map<String, Object> testCompleteFlow() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("testStartTime", LocalDateTime.now().toString());

        try {
            log.info("STEP 1: Checking database before import");
            long uniCountBefore = universityRepository.count();
            long progCountBefore = programRepository.count();
            result.put("step1_beforeImport", Map.of(
                    "universities", uniCountBefore,
                    "programs", progCountBefore
            ));

            log.info("STEP 2: Fetching data from all sources");
            List<SourceProgramDto> allPrograms = new ArrayList<>();

            try {
                List<SourceProgramDto> daadPrograms = daadScraperService.fetchPrograms();
                allPrograms.addAll(daadPrograms);
                result.put("step2_daad", Map.of(
                        "status", "SUCCESS",
                        "count", daadPrograms.size(),
                        "sample", daadPrograms.isEmpty() ? "No data" : daadPrograms.get(0).getProgramName()
                ));
            } catch (Exception e) {
                result.put("step2_daad", Map.of("status", "ERROR", "message", e.getMessage()));
            }

            try {
                List<SourceProgramDto> hochschulPrograms = hochschulkompassScraperService.fetchPrograms();
                allPrograms.addAll(hochschulPrograms);
                result.put("step2_hochschulkompass", Map.of(
                        "status", "SUCCESS",
                        "count", hochschulPrograms.size()
                ));
            } catch (Exception e) {
                result.put("step2_hochschulkompass", Map.of("status", "ERROR", "message", e.getMessage()));
            }

            try {
                List<SourceProgramDto> uniPrograms = universitySiteScraperService.fetchPrograms();
                allPrograms.addAll(uniPrograms);
                result.put("step2_university", Map.of(
                        "status", "SUCCESS",
                        "count", uniPrograms.size(),
                        "sample", uniPrograms.isEmpty() ? "No data" : uniPrograms.get(0).getProgramName()
                ));
            } catch (Exception e) {
                result.put("step2_university", Map.of("status", "ERROR", "message", e.getMessage()));
            }

            result.put("step2_totalFetched", allPrograms.size());

            log.info("STEP 3: Generating CSV from fetched data");
            CsvGeneratorService csvService = new CsvGeneratorService();
            String csvPath = "src/main/resources/data/test_programs.csv";
            try {
                StringBuilder csvContent = new StringBuilder();
                csvContent.append("source,universityName,city,programName,specialization,courseLanguage,minCgpa,minIelts,tuitionFee,intake,apsRequired,sourceUrl\n");

                for (SourceProgramDto dto : allPrograms) {
                    csvContent.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                            dto.getSource(),
                            dto.getUniversityName(),
                            dto.getCity(),
                            dto.getProgramName(),
                            dto.getSpecialization(),
                            dto.getCourseLanguage(),
                            dto.getMinCgpa(),
                            dto.getMinIelts(),
                            dto.getTuitionFee(),
                            dto.getIntake(),
                            dto.getApsRequired(),
                            dto.getSourceUrl() != null ? dto.getSourceUrl() : ""
                    ));
                }

                java.nio.file.Files.write(java.nio.file.Paths.get(csvPath), csvContent.toString().getBytes());
                result.put("step3_csv", Map.of(
                        "status", "SUCCESS",
                        "path", csvPath,
                        "records", allPrograms.size()
                ));
            } catch (Exception e) {
                result.put("step3_csv", Map.of("status", "ERROR", "message", e.getMessage()));
            }

            // ========== STEP 4: Import to Database ==========
            log.info("STEP 4: Importing to database");
            try {
                universityImportService.importFromCsvFile(csvPath);
                result.put("step4_import", Map.of("status", "SUCCESS", "message", "Import completed"));
            } catch (Exception e) {
                result.put("step4_import", Map.of("status", "ERROR", "message", e.getMessage()));
            }


            log.info("STEP 5: Verifying database after import");
            long uniCountAfter = universityRepository.count();
            long progCountAfter = programRepository.count();
            result.put("step5_afterImport", Map.of(
                    "universities", uniCountAfter,
                    "programs", progCountAfter,
                    "newUniversities", uniCountAfter - uniCountBefore,
                    "newPrograms", progCountAfter - progCountBefore
            ));

            log.info("STEP 6: Fetching saved data from database");
            List<University> savedUniversities = universityRepository.findAll();
            List<Program> savedPrograms = programRepository.findAll();

            result.put("step6_savedData", Map.of(
                    "universities", savedUniversities.stream()
                            .map(u -> Map.of("id", u.getId(), "name", u.getName(), "city", u.getCity()))
                            .toList(),
                    "programs", savedPrograms.stream()
                            .limit(5)
                            .map(p -> Map.of(
                                    "id", p.getId(),
                                    "name", p.getProgramName(),
                                    "university", p.getUniversity().getName(),
                                    "language", p.getCourseLanguage(),
                                    "cgpa", p.getMinCgpa()
                            ))
                            .toList()
            ));

            boolean success = (progCountAfter - progCountBefore) > 0;
            result.put("finalVerdict", Map.of(
                    "success", success,
                    "message", success ? "✅ DATA IS BEING SAVED TO DATABASE!" : " No data saved to database",
                    "totalSavedPrograms", progCountAfter,
                    "totalSavedUniversities", uniCountAfter
            ));

        } catch (Exception e) {
            log.error("Test flow failed", e);
            result.put("error", e.getMessage());
            result.put("stackTrace", Arrays.toString(e.getStackTrace()));
        }

        result.put("testEndTime", LocalDateTime.now().toString());
        return result;
    }


    @GetMapping("/simple-import-test")
    public Map<String, Object> simpleImportTest() {
        Map<String, Object> result = new LinkedHashMap<>();

        try {

            long beforeCount = programRepository.count();
            result.put("programsBefore", beforeCount);


            String csvPath = "src/main/resources/data/programs.csv";
            universityImportService.importFromCsvFile(csvPath);

            long afterCount = programRepository.count();
            result.put("programsAfter", afterCount);
            result.put("newProgramsImported", afterCount - beforeCount);

            List<Program> programs = programRepository.findAll();
            result.put("savedPrograms", programs.stream()
                    .map(p -> Map.of(
                            "id", p.getId(),
                            "name", p.getProgramName(),
                            "university", p.getUniversity().getName()
                    ))
                    .toList()
            );

            result.put("success", afterCount > beforeCount);
            result.put("message", afterCount > beforeCount ?
                    "✅ SUCCESS! Data is being saved to MySQL!" :
                    "❌ FAILED! No data saved to MySQL");

        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("success", false);
        }

        return result;
    }
}