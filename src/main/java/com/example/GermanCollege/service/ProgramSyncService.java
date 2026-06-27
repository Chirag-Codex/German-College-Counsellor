package com.example.GermanCollege.service;


import com.example.GermanCollege.dto.SourceProgramDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgramSyncService {

    private final UniversityApiService apiService;

    private final CsvGeneratorService csvGeneratorService;

    private final UniversityImportService importService;

    public void syncPrograms()
            throws Exception {

        List<SourceProgramDto> programs =
                apiService.fetchPrograms();

        String csvFile =
                csvGeneratorService.generateCsv(
                        programs
                );

        importService.importFromCsvFile(
                csvFile
        );
    }
}