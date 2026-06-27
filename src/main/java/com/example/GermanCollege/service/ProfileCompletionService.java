package com.example.GermanCollege.service;

import com.example.GermanCollege.model.StudentProfile;
import org.springframework.stereotype.Service;

@Service
public class ProfileCompletionService {

    public double calculateCompletion(StudentProfile profile) {

        int totalFields = 8;
        int filledFields = 0;

        if (profile.getTenthMarks() != null)
            filledFields++;

        if (profile.getTwelfthMarks() != null)
            filledFields++;

        if (profile.getCgpa() != null)
            filledFields++;

        if (profile.getIeltsScore() != null)
            filledFields++;

        if (profile.getPreferredCourse() != null
                && !profile.getPreferredCourse().isBlank())
            filledFields++;

        if (profile.getBudget() != null)
            filledFields++;

        if (profile.getPreferredCity() != null
                && !profile.getPreferredCity().isBlank())
            filledFields++;

        if (profile.getGermanLevel() != null
                && !profile.getGermanLevel().isBlank())
            filledFields++;

        return (filledFields * 100.0) / totalFields;
    }

    public boolean isProfileComplete(StudentProfile profile) {

        return profile.getCgpa() != null
                && profile.getIeltsScore() != null
                && profile.getPreferredCourse() != null
                && !profile.getPreferredCourse().isBlank()
                && profile.getBudget() != null;
    }

    public String getNextMissingField(StudentProfile profile) {

        if (profile.getPreferredCourse() == null
                || profile.getPreferredCourse().isBlank()) {
            return "preferredCourse";
        }

        if (profile.getCgpa() == null) {
            return "cgpa";
        }

        if (profile.getIeltsScore() == null) {
            return "ielts";
        }

        if (profile.getBudget() == null) {
            return "budget";
        }

        if (profile.getPreferredCity() == null
                || profile.getPreferredCity().isBlank()) {
            return "preferredCity";
        }

        if (profile.getGermanLevel() == null
                || profile.getGermanLevel().isBlank()) {
            return "germanLevel";
        }

        return null;
    }
}