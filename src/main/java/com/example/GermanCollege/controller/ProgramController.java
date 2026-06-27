package com.example.GermanCollege.controller;

import com.example.GermanCollege.model.Program;
import com.example.GermanCollege.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramRepository programRepository;

    @GetMapping("/programs")
    public List<Program> programs() {

        return programRepository.findAll();
    }
}