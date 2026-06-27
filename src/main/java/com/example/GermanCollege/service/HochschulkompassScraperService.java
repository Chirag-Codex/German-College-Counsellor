package com.example.GermanCollege.service;

import com.example.GermanCollege.dto.SourceProgramDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Slf4j
public class HochschulkompassScraperService implements ProgramSourceService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${scraper.hochschulkompass.search-url:https://www.hochschulkompass.de/en/study/search.html}")
    private String searchUrl;

    public HochschulkompassScraperService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public List<SourceProgramDto> fetchPrograms() throws Exception {
        log.info("Starting Hochschulkompass scraper...");
        List<SourceProgramDto> programs = new ArrayList<>();

        // Step 1: Get search results page
        Document searchPage = Jsoup.connect(searchUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(30000)
                .get();

        log.info("Fetched search page: {}", searchPage.title());

        // Step 2: Extract program URLs (you need to adjust selectors based on actual HTML)
        List<String> programUrls = extractProgramUrls(searchPage);
        log.info("Found {} program URLs to scrape", programUrls.size());

        // Step 3: Scrape each program page and extract data with AI
        for (int i = 0; i < Math.min(programUrls.size(), 10); i++) { // Limit to 10 for testing
            try {
                String programUrl = programUrls.get(i);
                log.info("Scraping program {}: {}", i+1, programUrl);

                Document programPage = Jsoup.connect(programUrl)
                        .userAgent("Mozilla/5.0")
                        .timeout(30000)
                        .get();

                SourceProgramDto dto = extractProgramDataWithAI(programPage, programUrl);
                if (dto != null) {
                    programs.add(dto);
                    log.info("Successfully extracted: {} - {}", dto.getUniversityName(), dto.getProgramName());
                }

                Thread.sleep(1000); // Be respectful to the server

            } catch (Exception e) {
                log.error("Failed to scrape program URL: {}", e.getMessage());
            }
        }

        log.info("Completed scraping. Total programs extracted: {}", programs.size());
        return programs;
    }

    private List<String> extractProgramUrls(Document page) {
        List<String> urls = new ArrayList<>();

        // TODO: Update these selectors based on actual Hochschulkompass HTML structure
        page.select("a[href*=/en/program/]").forEach(link -> {
            String href = link.attr("abs:href");
            if (!href.isEmpty()) {
                urls.add(href);
            }
        });

        // Fallback: Add sample search URL with parameters
        if (urls.isEmpty()) {
            log.warn("No program URLs found with default selector. Using search results page.");
            urls.add("https://www.hochschulkompass.de/en/study/search/results.html?subject=computer+science");
        }

        return urls;
    }

    private SourceProgramDto extractProgramDataWithAI(Document page, String sourceUrl) {
        try {
            // Extract relevant text from the page
            String pageText = extractRelevantText(page);

            // Gemini prompt for extraction
            String prompt = String.format("""
                Extract master's program information from the following German university webpage text.
                
                Return ONLY valid JSON in this exact format:
                {
                    "universityName": "full university name",
                    "city": "city name",
                    "programName": "exact program name",
                    "specialization": "specialization if any, else same as program name",
                    "courseLanguage": "English or German",
                    "minCgpa": 0.0,
                    "minIelts": 0.0,
                    "tuitionFee": 0.0,
                    "intake": "Winter/Summer/Both",
                    "apsRequired": false
                }
                
                If information is not found, use null for strings, 0.0 for numbers, false for boolean.
                
                Webpage text:
                %s
                
                Source URL: %s
                """,
                    truncateText(pageText, 15000),
                    sourceUrl
            );

            String aiResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.debug("AI Response: {}", aiResponse);

            // Extract JSON from response
            String jsonStr = extractJsonFromResponse(aiResponse);

            SourceProgramDto dto = objectMapper.readValue(jsonStr, SourceProgramDto.class);
            dto.setSource("Hochschulkompass");
            dto.setSourceUrl(sourceUrl);

            return dto;

        } catch (Exception e) {
            log.error("Error extracting data with AI: {}", e.getMessage());
            return null;
        }
    }

    private String extractRelevantText(Document page) {
        // Remove script and style elements
        page.select("script, style, nav, footer, header").remove();

        // Get main content areas
        StringBuilder text = new StringBuilder();

        // Try to find program description sections
        page.select("main, article, .program-details, .course-description, .module-description")
                .forEach(element -> {
                    text.append(element.text()).append("\n");
                });

        // If no specific sections found, get body text with limit
        if (text.length() == 0) {
            text.append(page.body().text());
        }

        return text.toString();
    }

    private String extractJsonFromResponse(String response) {
        // Find JSON object in the response
        java.util.regex.Pattern pattern = Pattern.compile("\\{[^{}]*(?:\\{[^{}]*\\}[^{}]*)*\\}",
                Pattern.DOTALL);
        java.util.regex.Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            return matcher.group();
        }

        throw new RuntimeException("No JSON found in AI response");
    }

    private String truncateText(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength);
    }
}