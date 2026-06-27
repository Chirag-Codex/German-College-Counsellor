package com.example.GermanCollege.service;

import com.example.GermanCollege.model.Program;
import com.example.GermanCollege.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralQueryService {

    private final ProgramRepository programRepository;

    private static final Set<String> STOP_WORDS = Set.of(
            "what", "is", "are", "the", "for", "of", "in", "at",
            "to", "a", "an", "does", "do", "how", "much", "many",
            "which", "where", "when", "tell", "me", "about", "and",
            "university", "universities", "college", "require", "need",
            "with", "from", "have", "has", "can", "will", "its", "their",
            "this", "that", "these", "those", "get", "into", "eligible"
    );

    public String buildContextForQuery(String message) {

        List<String> keywords = extractKeywords(message);
        log.info("Extracted keywords from query: {}", keywords);

        if (keywords.isEmpty()) return "";

        List<Program> results = keywords.stream()
                .flatMap(kw -> {
                    List<Program> found = programRepository.searchByKeyword(kw);
                    log.info("Keyword '{}' matched {} programs", kw, found.size());
                    return found.stream();
                })
                .distinct()
                .limit(15)
                .collect(Collectors.toList());

        log.info("Total programs found for context: {}", results.size());

        if (results.isEmpty()) return "";

        return formatPrograms(results);
    }

    public String buildUniversityContext(String universityName) {

        List<Program> results =
                programRepository.findByUniversityNameContaining(universityName);

        log.info("University '{}' matched {} programs", universityName, results.size());

        if (results.isEmpty()) return "";

        return formatPrograms(results);
    }


    private String formatPrograms(List<Program> programs) {
        StringBuilder ctx = new StringBuilder();

        for (Program p : programs) {
            ctx.append(String.format(
                    "• University: %s | City: %s\n"
                            + "  Program: %s | Specialization: %s\n"
                            + "  Min CGPA: %.1f | Min IELTS: %.1f"
                            + " | Tuition: €%.0f/yr | Language: %s | Intake: %s\n\n",
                    p.getUniversity() != null ? p.getUniversity().getName() : "Unknown",
                    p.getUniversity() != null ? p.getUniversity().getCity() : "Unknown",
                    p.getProgramName() != null ? p.getProgramName() : "N/A",
                    p.getSpecialization() != null ? p.getSpecialization() : "N/A",
                    p.getMinCgpa()     != null ? p.getMinCgpa()     : 0.0,
                    p.getMinIelts()    != null ? p.getMinIelts()    : 0.0,
                    p.getTuitionFee()  != null ? p.getTuitionFee()  : 0.0,
                    p.getCourseLanguage() != null ? p.getCourseLanguage() : "N/A",
                    p.getIntake()      != null ? p.getIntake()      : "N/A"
            ));
        }

        return ctx.toString().trim();
    }

    private List<String> extractKeywords(String message) {
        return Arrays.stream(message.toLowerCase().split("[\\s,?.!]+"))
                .filter(w -> w.length() > 3)
                .filter(w -> !STOP_WORDS.contains(w))
                .distinct()
                .collect(Collectors.toList());
    }
}