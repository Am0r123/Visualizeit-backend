package com.example.demo.service;

import com.example.demo.entities.BugChallenge;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DetectiveService {

    @Value("${gemini.api.key}")
    private String API_KEY;

    @Value("${gemini.api.url}")
    private String GEMINI_API_URL;

    private List<BugChallenge> challenges = new ArrayList<>();
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public DetectiveService() {
        createStaticFallback();
    }

    public BugChallenge generateChallenge(String topic, String difficulty, String language) {
        
        // 1. Calculate Random Bugs based on Difficulty
        int minBugs = 1, maxBugs = 1;
        String difficultyInstruction = "";

        switch (difficulty.toLowerCase()) {
            case "hard":
                minBugs = 4; maxBugs = 6;
                difficultyInstruction = "Create a COMPLEX code with intricate logic errors, memory leaks, or recursion depth issues.";
                break;
            case "medium":
                minBugs = 2; maxBugs = 4;
                difficultyInstruction = "Create INTERMEDIATE code with logical errors (e.g. wrong conditions, off-by-one).";
                break;
            default: // Easy
                minBugs = 1; maxBugs = 2;
                difficultyInstruction = "Create SIMPLE code with basic syntax or typo-level logical errors.";
                break;
        }

        // Randomize the bug count
        int targetBugs = ThreadLocalRandom.current().nextInt(minBugs, maxBugs + 1);

        // 2. Construct the "Package" Prompt
        String systemInstruction = "You are a " + language + " Expert. " + difficultyInstruction + 
                " The code MUST contain EXACTLY " + targetBugs + " bugs. " +
                " Return ONLY valid JSON (no markdown). Format: " +
                "{ \"title\": \"...\", \"description\": \"...\", \"brokenCode\": \"...\", \"correctCode\": \"...\" }";

        String userPrompt = "Create a " + difficulty + " " + language + " challenge about '" + (topic.isEmpty() ? "Random Algorithms" : topic) + "' with " + targetBugs + " bugs.";

        try {
            Map<String, Object> payload = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", userPrompt)))),
                "systemInstruction", Map.of("parts", List.of(Map.of("text", systemInstruction)))
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            String url = GEMINI_API_URL.endsWith("=") ? GEMINI_API_URL + API_KEY : GEMINI_API_URL + "?key=" + API_KEY;
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = mapper.readTree(response.getBody());
                String rawText = root.at("/candidates/0/content/parts/0/text").asText();
                rawText = rawText.replace("```json", "").replace("```", "").trim(); 

                JsonNode json = mapper.readTree(rawText);

                BugChallenge newChallenge = new BugChallenge(
                    UUID.randomUUID().toString(),
                    json.path("title").asText("New Mystery"),
                    json.path("description").asText("Find the bugs."),
                    json.path("brokenCode").asText("// Error parsing code"),
                    json.path("correctCode").asText("// Error parsing code"),
                    targetBugs, // 🟢 Save ACTUAL calculated bug count
                    difficulty  // 🟢 Save ACTUAL difficulty selected
                );

                this.challenges.add(0, newChallenge);
                return newChallenge;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<BugChallenge> getAllChallenges() { return challenges; }

    public boolean checkSolution(String id, String userCode) {
        Optional<BugChallenge> c = challenges.stream().filter(ch -> ch.getId().equals(id)).findFirst();
        if (c.isEmpty()) return false;
        String u = userCode.replaceAll("\\s+", "");
        String k = c.get().getCorrectCode().replaceAll("\\s+", "");
        return u.equals(k);
    }

    private void createStaticFallback() {
        String broken = "public void hello() { System.out.println(\"Helo World\"); }"; 
        String correct = "public void hello() { System.out.println(\"Hello World\"); }";
        challenges.add(new BugChallenge("static-1", "Hello World Typo", "Fix the typo.", broken, correct, 1, "Easy"));
    }
}