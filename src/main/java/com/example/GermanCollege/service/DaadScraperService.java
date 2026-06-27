package com.example.GermanCollege.service;

import com.example.GermanCollege.dto.SourceProgramDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Slf4j
public class DaadScraperService implements ProgramSourceService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DaadScraperService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public List<SourceProgramDto> fetchPrograms() throws Exception {
        log.info("Starting DAAD scraper...");
        List<SourceProgramDto> programs = new ArrayList<>();

        String searchUrl = "https://www2.daad.de/deutschland/studienangebote/international-programmes/en/result/?&degree[]=2&lang[]=1&page=1";

        Document searchPage = Jsoup.connect(searchUrl)
                .userAgent("Mozilla/5.0")
                .timeout(30000)
                .get();

        log.info("Fetched DAAD search page");

        List<String> programUrls = extractDaadProgramUrls(searchPage);
        log.info("Found {} DAAD programs", programUrls.size());

        for (int i = 0; i < Math.min(programUrls.size(), 20); i++) {
            try {
                String programUrl = programUrls.get(i);
                log.info("Scraping DAAD program {}: {}", i+1, programUrl);

                Document programPage = Jsoup.connect(programUrl)
                        .userAgent("Mozilla/5.0")
                        .timeout(30000)
                        .get();

                SourceProgramDto dto = extractDaadProgramWithAI(programPage, programUrl);
                if (dto != null) {
                    programs.add(dto);
                    log.info("Extracted: {} - {}", dto.getUniversityName(), dto.getProgramName());
                }

                Thread.sleep(1500); // Be respectful to DAAD

            } catch (Exception e) {
                log.error("Failed to scrape DAAD program: {}", e.getMessage());
            }
        }

        log.info("DAAD scraping completed. Total: {}", programs.size());
        return programs;
    }

    private List<String> extractDaadProgramUrls(Document page) {
        List<String> urls = new ArrayList<>();

        // DAAD specific selectors
        page.select("a[href*=/deutschland/studienangebote/international-programmes/en/detail/]")
                .forEach(link -> {
                    String href = link.attr("abs:href");
                    if (!href.isEmpty() && !urls.contains(href)) {
                        urls.add(href);
                    }
                });

        return urls;
    }

    private SourceProgramDto extractDaadProgramWithAI(Document page, String sourceUrl) {
        try {
            String programText = extractDaadText(page);

            String prompt = String.format("""
                Extract master's program information from this DAAD program page.
                
                Return JSON:
                {
                    "universityName": "university offering the program",
                    "city": "location city",
                    "programName": "full program name",
                    "specialization": "focus area if specified",
                    "courseLanguage": "language of instruction",
                    "minCgpa": 0.0,
                    "minIelts": 0.0,
                    "tuitionFee": 0.0,
                    "intake": "semester intake",
                    "apsRequired": false
                }
                
                Program text:
                %s
                """,
                    truncateText(programText, 10000)
            );

            String response = chatClient.prompt().user(prompt).call().content();
            String jsonStr = extractJsonFromResponse(response);

            SourceProgramDto dto = objectMapper.readValue(jsonStr, SourceProgramDto.class);
            dto.setSource("DAAD");
            dto.setSourceUrl(sourceUrl);

            return dto;

        } catch (Exception e) {
            log.error("DAAD AI extraction failed: {}", e.getMessage());
            return null;
        }
    }

    private String extractDaadText(Document page) {
        StringBuilder text = new StringBuilder();

        page.select(".detail-view-content, .programme-details, .description")
                .forEach(element -> text.append(element.text()).append("\n"));

        if (text.length() == 0) {
            text.append(page.body().text());
        }

        return text.toString();
    }

    private String extractJsonFromResponse(String response) {
        Pattern pattern = Pattern.compile("\\{[^{}]*(?:\\{[^{}]*\\}[^{}]*)*\\}", Pattern.DOTALL);
        java.util.regex.Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            return matcher.group();
        }

        throw new RuntimeException("No JSON found in response");
    }

    private String truncateText(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }
}