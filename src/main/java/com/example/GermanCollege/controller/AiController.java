package com.example.GermanCollege.controller;

import com.example.GermanCollege.dto.ChatResponse;
import com.example.GermanCollege.dto.ProfileExtraction;
import com.example.GermanCollege.dto.RecommendationDto;
import com.example.GermanCollege.dto.StudentProfileDto;
import com.example.GermanCollege.model.StudentProfile;
import com.example.GermanCollege.service.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Stream;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:5173")
public class AiController {

    private final ChatClient chatClient;
    private final ExtractionService extractionService;
    private final ProfileService profileService;
    private final RecommendationService recommendationService;
    private final ProfileCompletionService profileCompletionService;
    private final IntentClassifier intentClassifier;
    private final GeneralQueryService generalQueryService;

    private final Map<String, List<RecommendationDto>> lastRecommendations =
            new ConcurrentHashMap<>();

    @Autowired
    public AiController(
            ChatClient.Builder builder,
            ChatMemory chatMemory,
            ExtractionService extractionService,
            ProfileService profileService,
            RecommendationService recommendationService,
            ProfileCompletionService profileCompletionService,
            IntentClassifier intentClassifier,
            GeneralQueryService generalQueryService
    ) {
        this.extractionService = extractionService;
        this.profileService = profileService;
        this.recommendationService = recommendationService;
        this.profileCompletionService = profileCompletionService;
        this.intentClassifier = intentClassifier;
        this.generalQueryService = generalQueryService;

        this.chatClient = builder
                .defaultSystem("""
                        Your name is Harry
                        You are GermanyMate AI, an expert German University Admission Counselor.
                        
                        YOUR PERSONA:
                        You are warm, professional, and concise. You help Indian students 
                        find the right German university for their goals.
                        
                        YOUR THREE MODES — switch based on context:
                        
                        ── MODE 1: GENERAL INFORMATION ──
                        When the user asks a factual question (eligibility, fees, intake, 
                        city info, language requirements) WITHOUT sharing personal data,
                        answer directly from the context you receive. Be factual and concise.
                        Do NOT ask for their profile unless they want personalised recommendations.
                        
                        ── MODE 2: PROFILE COLLECTION ──
                        When the user wants personalised recommendations, collect these fields
                        one by one through natural conversation:
                          • 10th marks  • 12th marks  • Bachelor CGPA  • IELTS score
                          • Preferred course  • Specialization  • Budget  • Preferred city
                          • German language level  • APS status
                        
                        Rules:
                        - Never re-ask for information already provided.
                        - Ask for only ONE missing field at a time.
                        - Acknowledge what they shared before asking the next question.
                        - When profile is complete, say exactly:
                          "Great. I have enough information to find suitable universities."
                        
                        ── MODE 3: FOLLOW-UP & DEEP DIVE ──
                        When the user asks about specific universities from a recommendation list,
                        use the recommendation context provided to answer accurately.
                        Cover: admission requirements, application process, campus life, 
                        research opportunities, deadlines, scholarships, and city life.
                        Be specific and helpful — this is where you add real value.
                        
                        IMPORTANT:
                        - Focus ONLY on studying in Germany.
                        - Keep answers short and conversational unless deep-dive is needed.
                        - Be encouraging and supportive.
                        If the data is not in the database tell the latest information available to you
                        and don't give the duplicate or same recommendations more than one time only give them one time and check it
                        after getting information from the database
                        """)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    @PostMapping("/ask")
    public ChatResponse ask(
            @RequestParam (required = false) String conversationId,
            @RequestBody String message
    ) {
        final String sessionId = (conversationId != null && !conversationId.isBlank())
                ? conversationId
                : UUID.randomUUID().toString();

        List<RecommendationDto> storedRecs =
                lastRecommendations.getOrDefault(conversationId, List.of());

        IntentClassifier.Intent intent =
                intentClassifier.classify(message, !storedRecs.isEmpty());

        return switch (intent) {
            case GENERAL_QUERY -> handleGeneralQuery(conversationId, message);
            case FOLLOW_UP     -> handleFollowUp(conversationId, message, storedRecs);
            default            -> handleProfileCollection(conversationId, message);
        };
    }



    private ChatResponse handleGeneralQuery(
            String conversationId,
            String message
    ) {

        String dbContext = generalQueryService.buildContextForQuery(message);

        String universityContext = extractUniversityName(message)
                .map(generalQueryService::buildUniversityContext)
                .orElse("");

        String combinedContext = Stream.of(dbContext, universityContext)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining("\n\n"));

        String prompt = combinedContext.isBlank()
                ? message
                : message + "\n\n[Database context — answer using ONLY this data]:\n"
                + combinedContext;

        String reply = chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(prompt)
                .call()
                .content();

        StudentProfile profile = profileService.getProfile(conversationId);

        return ChatResponse.builder()
                .conversationId(conversationId)
                .reply(reply)
                .profileComplete(false)
                .completionPercentage(
                        profileCompletionService.calculateCompletion(profile))
                .profile(convert(profile))
                .recommendations(null)
                .nextMissingField(null)
                .build();
    }

    private Optional<String> extractUniversityName(String message) {
        String lower = message.toLowerCase();
        List<String> universityKeywords = List.of(
                "tu munich", "tum", "tu berlin", "rwth", "lmu", "kit",
                "heidelberg", "mannheim", "frankfurt", "hamburg", "cologne",
                "Stuttgart", "dresden", "darmstadt", "karlsruhe", "bonn",
                "freiburg", "erlangen", "hannover", "münster", "gottingen"
        );

        return universityKeywords.stream()
                .filter(lower::contains)
                .findFirst();
    }

    private ChatResponse handleFollowUp(
            String conversationId,
            String message,
            List<RecommendationDto> storedRecs
    ) {
        String recsContext = formatRecommendationsAsContext(storedRecs);

        String prompt = """
                The user was shown these university recommendations earlier:
                
                %s
                
                Now they are asking: %s
                
                Answer specifically using the recommendation context above.
                Be detailed and helpful — this is a deep-dive question.
                """.formatted(recsContext, message);

        String reply = chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(prompt)
                .call()
                .content();

        StudentProfile profile = profileService.getProfile(conversationId);

        return ChatResponse.builder()
                .conversationId(conversationId)
                .reply(reply)
                .profileComplete(true)
                .completionPercentage(100.0)
                .profile(convert(profile))
                .recommendations(storedRecs)
                .nextMissingField(null)
                .build();
    }



    private ChatResponse handleProfileCollection(
            String conversationId,
            String message
    ) {
        ProfileExtraction extraction =
                extractionService.extractProfile(message);

        profileService.updateProfile(conversationId, extraction);

        StudentProfile profile = profileService.getProfile(conversationId);

        double completionPercentage =
                profileCompletionService.calculateCompletion(profile);

        boolean profileComplete =
                profileCompletionService.isProfileComplete(profile);

        if (profileComplete) {
            List<RecommendationDto> recommendations =
                    recommendationService.recommend(profile);

            lastRecommendations.put(conversationId, recommendations);

            return ChatResponse.builder()
                    .conversationId(conversationId)
                    .reply("Great. I have enough information to find suitable universities.")
                    .profileComplete(true)
                    .completionPercentage(completionPercentage)
                    .profile(convert(profile))
                    .recommendations(recommendations)
                    .nextMissingField(null)
                    .build();
        }

        String aiReply = chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(message)
                .call()
                .content();

        return ChatResponse.builder()
                .conversationId(conversationId)
                .reply(aiReply)
                .profileComplete(false)
                .completionPercentage(completionPercentage)
                .profile(convert(profile))
                .recommendations(null)
                .nextMissingField(
                        profileCompletionService.getNextMissingField(profile))
                .build();
    }


    private String formatRecommendationsAsContext(List<RecommendationDto> recs) {
        if (recs == null || recs.isEmpty()) return "No recommendations available.";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < recs.size(); i++) {
            RecommendationDto r = recs.get(i);
            sb.append(String.format(
                    """
                    Option %d: %s — %s
                      City: %s | Program: %s | Specialization: %s
                      Min CGPA: %.1f | Min IELTS: %.1f
                      Tuition: €%.0f/year | Language: %s | Intake: %s
                      Match score: %d | Why: %s
                    
                    """,
                    i + 1,
                    r.getUniversityName(),
                    r.getProgramName(),
                    r.getCity(),
                    r.getProgramName(),
                    r.getSpecialization() != null ? r.getSpecialization() : "N/A",
                    r.getMinCgpa() != null ? r.getMinCgpa() : 0.0,
                    r.getMinIelts() != null ? r.getMinIelts() : 0.0,
                    r.getTuitionFee() != null ? r.getTuitionFee() : 0.0,
                    r.getCourseLanguage() != null ? r.getCourseLanguage() : "N/A",
                    r.getIntake() != null ? r.getIntake() : "N/A",
                    r.getMatchScore(),
                    r.getReason()
            ));
        }
        return sb.toString();
    }

    @GetMapping("/profile")
    public StudentProfileDto getProfile(@RequestParam String conversationId) {
        return convert(profileService.getProfile(conversationId));
    }

    private StudentProfileDto convert(StudentProfile profile) {
        if (profile == null) return null;
        StudentProfileDto dto = new StudentProfileDto();
        dto.setTenthMarks(profile.getTenthMarks());
        dto.setTwelfthMarks(profile.getTwelfthMarks());
        dto.setCgpa(profile.getCgpa());
        dto.setIelts(profile.getIeltsScore());
        dto.setPreferredCourse(profile.getPreferredCourse());
        dto.setBudget(profile.getBudget());
        dto.setPreferredCity(profile.getPreferredCity());
        dto.setGermanLevel(profile.getGermanLevel());
        return dto;
    }
}