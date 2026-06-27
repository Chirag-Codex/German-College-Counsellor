package com.example.GermanCollege.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IntentClassifier {

    public enum Intent {
        GENERAL_QUERY,
        PROFILE_COLLECTION,
        FOLLOW_UP
    }

    private static final List<String> PROFILE_SIGNALS = List.of(
            "my cgpa", "my ielts", "my marks", "my score", "my budget",
            "i scored", "i have", "i got", "i want to study", "i prefer",
            "find me", "recommend", "suggest universities", "help me find",
            "my 10th", "my 12th", "my bachelor", "my gpa"
    );

    private static final List<String> FOLLOWUP_SIGNALS = List.of(
            "first one", "second one", "third one", "option 1", "option 2",
            "option 3", "that university", "tell me more", "more about",
            "compare", "which is better", "deadline for", "apply to",
            "the one you", "mentioned university", "above university"
    );

    private static final List<String> GENERAL_SIGNALS = List.of(
            "what is", "what are", "eligibility", "requirement", "require",
            "minimum cgpa", "minimum ielts", "tuition fee", "fee structure",
            "intake", "deadline", "about tu", "about lmu", "about rwth",
            "about kit", "about tum", "which universities", "tell me about",
            "how much", "language requirement", "german requirement",
            "aps certificate", "blocked account", "visa", "scholarship"
    );

    public Intent classify(String message, boolean hasRecommendations) {
        String lower = message.toLowerCase().trim();

        if (hasRecommendations) {
            for (String signal : FOLLOWUP_SIGNALS) {
                if (lower.contains(signal)) return Intent.FOLLOW_UP;
            }
        }

        for (String signal : PROFILE_SIGNALS) {
            if (lower.contains(signal)) return Intent.PROFILE_COLLECTION;
        }

        for (String signal : GENERAL_SIGNALS) {
            if (lower.contains(signal)) return Intent.GENERAL_QUERY;
        }

        return Intent.PROFILE_COLLECTION;
    }
}