package com.example.GermanCollege.service;

import com.example.GermanCollege.dto.SourceProgramDto;


import java.util.List;

public interface ProgramSourceService {

    List<SourceProgramDto> fetchPrograms() throws Exception;
}