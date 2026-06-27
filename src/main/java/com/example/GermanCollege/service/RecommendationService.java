package com.example.GermanCollege.service;

import com.example.GermanCollege.dto.RecommendationDto;
import com.example.GermanCollege.dto.RecommendationResult;
import com.example.GermanCollege.model.Program;
import com.example.GermanCollege.model.StudentProfile;
import com.example.GermanCollege.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ProgramRepository programRepository;

    public List<RecommendationDto> recommend(
            StudentProfile profile) {

        if (profile == null) {
            return List.of();
        }

        Double cgpa =
                profile.getCgpa() != null
                        ? profile.getCgpa()
                        : 0.0;

        Double ielts =
                profile.getIeltsScore() != null
                        ? profile.getIeltsScore()
                        : 0.0;

        List<Program> programs =
                programRepository
                        .findByMinCgpaLessThanEqualAndMinIeltsLessThanEqual(
                                cgpa,
                                ielts
                        );

        return programs.stream()

                .map(program ->
                        new RecommendationResult(
                                program,
                                calculateScore(
                                        profile,
                                        program
                                )
                        )
                )

                .sorted(
                        Comparator.comparingInt(
                                RecommendationResult::getScore
                        ).reversed()
                )

                .limit(10)

                .map(result -> {

                    Program program =
                            result.getProgram();

                    return RecommendationDto.builder()

                            .universityName(
                                    program.getUniversity()
                                            .getName()
                            )

                            .city(
                                    program.getUniversity()
                                            .getCity()
                            )

                            .programName(
                                    program.getProgramName()
                            )

                            .specialization(
                                    program.getSpecialization()
                            )

                            .courseLanguage(
                                    program.getCourseLanguage()
                            )

                            .minCgpa(
                                    program.getMinCgpa()
                            )

                            .minIelts(
                                    program.getMinIelts()
                            )

                            .tuitionFee(
                                    program.getTuitionFee()
                            )

                            .intake(
                                    program.getIntake()
                            )

                            .matchScore(
                                    result.getScore()
                            )

                            .reason(
                                    buildReason(
                                            profile,
                                            program
                                    )
                            )

                            .build();
                })

                .toList();
    }

    private int calculateScore(
            StudentProfile profile,
            Program program) {

        int score = 0;

        if (isCourseMatch(
                profile.getPreferredCourse(),
                program.getProgramName())) {

            score += 40;
        }

        if (isCourseMatch(
                profile.getPreferredCourse(),
                program.getSpecialization())) {

            score += 30;
        }

        if (profile.getPreferredCity() != null
                && program.getUniversity().getCity() != null
                && program.getUniversity()
                .getCity()
                .equalsIgnoreCase(
                        profile.getPreferredCity())) {

            score += 20;
        }

        if (profile.getCgpa() != null
                && program.getMinCgpa() != null
                && profile.getCgpa()
                >= program.getMinCgpa()) {

            score += 5;
        }

        if (profile.getIeltsScore() != null
                && program.getMinIelts() != null
                && profile.getIeltsScore()
                >= program.getMinIelts()) {

            score += 5;
        }

        if (profile.getBudget() != null
                && program.getTuitionFee() != null
                && program.getTuitionFee()
                <= profile.getBudget()) {

            score += 10;
        }

        if (Boolean.TRUE.equals(program.getApsRequired())) {
            score += 5;
        }

        score = Math.min(score, 100);

        return score;
    }

    private boolean isCourseMatch(
            String preferredCourse,
            String targetText) {

        if (preferredCourse == null
                || targetText == null) {

            return false;
        }

        preferredCourse =
                preferredCourse.toLowerCase();

        targetText =
                targetText.toLowerCase();

        // DIRECT MATCH
        if (targetText.contains(preferredCourse)) {
            return true;
        }

        // COMPUTER SCIENCE / AI / DATA SCIENCE
        if (
                preferredCourse.contains("computer")
                        || preferredCourse.contains("cs")
                        || preferredCourse.contains("artificial intelligence")
                        || preferredCourse.contains("ai")
                        || preferredCourse.contains("data science")
        ) {

            return targetText.contains("computer")
                    || targetText.contains("data")
                    || targetText.contains("ai")
                    || targetText.contains("machine learning")
                    || targetText.contains("software")
                    || targetText.contains("artificial intelligence");
        }

        // BUSINESS DOMAIN
        if (preferredCourse.contains("business")) {

            return targetText.contains("management")
                    || targetText.contains("finance")
                    || targetText.contains("marketing")
                    || targetText.contains("business");
        }

        // MECHANICAL DOMAIN
        if (preferredCourse.contains("mechanical")) {

            return targetText.contains("mechanical")
                    || targetText.contains("automobile")
                    || targetText.contains("manufacturing");
        }

        // ELECTRICAL DOMAIN
        if (preferredCourse.contains("electrical")) {

            return targetText.contains("electrical")
                    || targetText.contains("electronics")
                    || targetText.contains("embedded");
        }

        return false;
    }

    private String buildReason(
            StudentProfile profile,
            Program program) {

        StringBuilder reason =
                new StringBuilder();

        if (profile.getCgpa() != null
                && program.getMinCgpa() != null
                && profile.getCgpa()
                >= program.getMinCgpa()) {

            reason.append(
                    "Your CGPA meets the program requirement. "
            );
        }

        if (profile.getIeltsScore() != null
                && program.getMinIelts() != null
                && profile.getIeltsScore()
                >= program.getMinIelts()) {

            reason.append(
                    "Your IELTS score satisfies admission criteria. "
            );
        }

        if (isCourseMatch(
                profile.getPreferredCourse(),
                program.getProgramName())) {

            reason.append(
                    "The program closely matches your preferred field of study. "
            );
        }

        if (profile.getPreferredCity() != null
                && program.getUniversity().getCity() != null
                && profile.getPreferredCity()
                .equalsIgnoreCase(
                        program.getUniversity()
                                .getCity())) {

            reason.append(
                    "The university is located in your preferred city. "
            );
        }

        if (profile.getBudget() != null
                && program.getTuitionFee() != null
                && program.getTuitionFee()
                <= profile.getBudget()) {

            reason.append(
                    "Tuition fees fit within your budget. "
            );
        }

        if (program.getCourseLanguage() != null) {

            reason.append(
                    "Course language: "
                            + program.getCourseLanguage()
                            + ". "
            );
        }

        if (program.getIntake() != null) {

            reason.append(
                    "Available intake: "
                            + program.getIntake()
                            + ". "
            );
        }

        if (reason.isEmpty()) {

            reason.append(
                    "This program aligns reasonably well with your academic profile and study preferences."
            );
        }

        return reason.toString().trim();
    }
}