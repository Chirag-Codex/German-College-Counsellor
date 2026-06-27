package com.example.GermanCollege.service;

import com.example.GermanCollege.dto.SourceProgramDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataPipelineService {

    private final DaadScraperService daadScraperService;

    private final HochschulkompassScraperService hochschulkompassScraperService;

    private final UniversitySiteScraperService universitySiteScraperService;

    private final DataNormalizationService normalizationService;

    private final CsvGeneratorService csvGeneratorService;

    private final UniversityImportService universityImportService;

    public String runPipeline()
            throws Exception {

        List<SourceProgramDto> allPrograms =
                new ArrayList<>();

        allPrograms.addAll(
                daadScraperService.fetchPrograms()
        );

        allPrograms.addAll(
                hochschulkompassScraperService.fetchPrograms()
        );

        allPrograms.addAll(
                universitySiteScraperService.fetchPrograms()
        );

        List<SourceProgramDto> normalized =
                normalizationService.normalize(
                        allPrograms
                );

        String csvFile =
                csvGeneratorService.generateCsv(
                        normalized
                );

        universityImportService.importFromCsvFile(
                csvFile
        );

        return "Imported "
                + normalized.size()
                + " programs";
    }
}