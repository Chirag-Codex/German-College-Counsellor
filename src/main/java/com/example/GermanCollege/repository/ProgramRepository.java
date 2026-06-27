package com.example.GermanCollege.repository;

import com.example.GermanCollege.model.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    List<Program> findByMinCgpaLessThanEqualAndMinIeltsLessThanEqual(
            Double cgpa, Double ielts);

    Optional<Program> findByProgramNameAndUniversity_Name(
            String programName, String universityName);

    @Query("SELECT COUNT(p) > 0 FROM Program p " +
            "WHERE p.university.name = :universityName " +
            "AND p.programName = :programName")
    boolean existsByUniversityNameAndProgramName(
            @Param("universityName") String universityName,
            @Param("programName") String programName);

    // ── NEW: fuzzy search across all relevant fields ──────────────────────────
    @Query("""
        SELECT p FROM Program p
        WHERE LOWER(p.university.name)  LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(p.university.city)  LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(p.programName)      LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(p.specialization)   LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(p.courseLanguage)   LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    List<Program> searchByKeyword(@Param("keyword") String keyword);

    // ── NEW: fetch all programs for a specific university by name ─────────────
    @Query("""
        SELECT p FROM Program p
        WHERE LOWER(p.university.name) LIKE LOWER(CONCAT('%', :universityName, '%'))
    """)
    List<Program> findByUniversityNameContaining(
            @Param("universityName") String universityName);
}