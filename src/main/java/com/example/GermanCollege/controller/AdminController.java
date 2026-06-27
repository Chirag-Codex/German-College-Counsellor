package com.example.GermanCollege.controller;

import com.example.GermanCollege.service.UniversityImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final UniversityImportService universityImportService;

    @PostMapping("/import")
    public String importData() throws Exception {

        universityImportService.importFromCsvFile(
                "src/main/resources/data/programs.csv"
        );

        return "Import completed";
    }
}