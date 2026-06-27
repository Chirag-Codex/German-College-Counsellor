package com.example.GermanCollege.service;

import com.example.GermanCollege.dto.ProfileExtraction;
import com.example.GermanCollege.model.StudentProfile;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProfileService {

    private final Map<String, StudentProfile> profiles = new HashMap<>();

    public StudentProfile getProfile(String conversationId) {

        return profiles.computeIfAbsent(
                conversationId,
                id -> new StudentProfile()
        );
    }

    public void updateProfile(
            String conversationId,
            ProfileExtraction extraction) {

        StudentProfile profile = getProfile(conversationId);

        if (extraction.getCgpa() != null) {
            profile.setCgpa(extraction.getCgpa());
        }

        if (extraction.getTenthMarks() != null) {
            profile.setTenthMarks(extraction.getTenthMarks());
        }

        if (extraction.getTwelfthMarks() != null) {
            profile.setTwelfthMarks(extraction.getTwelfthMarks());
        }

        if (extraction.getIeltsScore() != null) {
            profile.setIeltsScore(extraction.getIeltsScore());
        }

        if (extraction.getPreferredCourse() != null) {
            profile.setPreferredCourse(extraction.getPreferredCourse());
        }

        if (extraction.getPreferredCity() != null) {
            profile.setPreferredCity(extraction.getPreferredCity());
        }

        if (extraction.getBudget() != null) {
            profile.setBudget(extraction.getBudget());
        }

        if (extraction.getGermanLevel() != null) {
            profile.setGermanLevel(extraction.getGermanLevel());
        }
    }
}