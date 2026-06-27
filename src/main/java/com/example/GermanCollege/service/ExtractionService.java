package com.example.GermanCollege.service;

import com.example.GermanCollege.dto.ProfileExtraction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ExtractionService {

    private final ChatClient chatClient;

    public ExtractionService(ChatClient.Builder builder) {

        this.chatClient = builder.build();
    }

    public ProfileExtraction extractProfile(String message) {

        return chatClient.prompt()
                .system("""
                        Extract student information from the user message.

                        Return only available fields.

                        If a field is missing, leave it null.
                        """)
                .user(message)
                .call()
                .entity(ProfileExtraction.class);
    }
}