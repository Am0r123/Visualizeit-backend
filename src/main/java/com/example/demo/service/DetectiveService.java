package com.example.demo.service;

import com.example.demo.entities.BugChallenge;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
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

    // ---------------------------------------------------------
    // 1. HELPER: Random Topic Selector
    // ---------------------------------------------------------
    private String getRandomTopic(String structure) {
        List<String> algorithms;
        switch (structure.toLowerCase()) {
            case "linked list":
                algorithms = List.of("Singly Linked List Traversal", "Reverse a Linked List", "Detect Loop", "Remove N-th Node");
                break;
            case "tree":
                algorithms = List.of("BST Insertion", "Inorder Traversal", "Find Max Depth", "Level Order Traversal");
                break;
            case "array":
            default:
                algorithms = List.of("Binary Search", "Bubble Sort", "Find Max Element", "Reverse Array", "Two Pointer Sum");
                break;
        }
        return algorithms.get(ThreadLocalRandom.current().nextInt(algorithms.size()));
    }

    // ---------------------------------------------------------
    // 2. CORE: Generate Challenge
    // ---------------------------------------------------------
    public BugChallenge generateChallenge(String structure, String difficulty, String language) {
        
        String specificTopic = getRandomTopic(structure);
        int minBugs = 1, maxBugs = 1;
        String diffInst = "";

        switch (difficulty.toLowerCase()) {
            case "hard": 
                minBugs = 3; maxBugs = 5; 
                diffInst = "Complex logic errors or edge cases."; 
                break;
            case "medium": 
                minBugs = 2; maxBugs = 3; 
                diffInst = "Logic errors like off-by-one or null pointer."; 
                break;
            default: 
                minBugs = 1; maxBugs = 1; 
                diffInst = "Simple syntax or typos."; 
                break;
        }

        int targetBugs = ThreadLocalRandom.current().nextInt(minBugs, maxBugs + 1);

        String systemInstruction = "You are a " + language + " Expert. " + diffInst + 
                " The code MUST contain " + targetBugs + " bugs. Return ONLY valid JSON (no markdown). Format: " +
                "{ \"title\": \"...\", \"description\": \"...\", \"brokenCode\": \"...\", \"correctCode\": \"...\" }";

        String userPrompt = "Create a " + difficulty + " " + language + " challenge for '" + specificTopic + "' (" + structure + ") with " + targetBugs + " bugs.";

        try {
            // 🟢 REFACTORED: Uses the helper method now
            String responseText = callGemini(systemInstruction, userPrompt);
            
            if (responseText != null) {
                JsonNode json = mapper.readTree(responseText);

                BugChallenge newChallenge = new BugChallenge(
                    UUID.randomUUID().toString(),
                    json.path("title").asText("Mystery Bug"),
                    json.path("description").asText("Fix the code."),
                    json.path("brokenCode").asText("// Error parsing code"),
                    json.path("correctCode").asText("// Error parsing code"),
                    targetBugs, 
                    difficulty
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

    // ---------------------------------------------------------
    // 3. CORE: Check Solution (Hybrid Logic)
    // ---------------------------------------------------------
    public boolean checkSolution(String id, String userCode) {
        Optional<BugChallenge> c = challenges.stream().filter(ch -> ch.getId().equals(id)).findFirst();
        if (c.isEmpty()) return false;
        
        BugChallenge challenge = c.get();

        // Step A: Fast Check (Exact String Match - Ignoring Spaces)
        String u = userCode.replaceAll("\\s+", "");
        String k = challenge.getCorrectCode().replaceAll("\\s+", "");
        if (u.equals(k)) return true;

        // Step B: Smart Check (Ask AI if logic is correct)
        return verifyWithAI(challenge.getBrokenCode(), userCode, challenge.getCorrectCode());
    }

    // ---------------------------------------------------------
    // 4. HELPER: AI Verification Logic
    // ---------------------------------------------------------
    private boolean verifyWithAI(String broken, String user, String expected) {
        String systemInstruction = "You are a senior code reviewer. Verify if the student fixed the bugs. " +
                                   "Ignore comments, whitespace, and variable renaming if logic is valid. " +
                                   "Reply ONLY with the word 'true' or 'false'.";
        
        String prompt = "1. BROKEN CODE:\n" + broken + "\n\n" +
                        "2. EXPECTED FIX:\n" + expected + "\n\n" +
                        "3. STUDENT SOLUTION:\n" + user + "\n\n" +
                        "TASK: Did the student fix the logic errors?";

        try {
            String response = callGemini(systemInstruction, prompt);
            return response != null && response.toLowerCase().contains("true");
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Fallback to fail if AI is down
        }
    }

    // ---------------------------------------------------------
    // 5. HELPER: Reusable Gemini API Call
    // ---------------------------------------------------------
    private String callGemini(String systemInst, String userMsg) {
        try {
            Map<String, Object> payload = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", userMsg)))),
                "systemInstruction", Map.of("parts", List.of(Map.of("text", systemInst)))
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            String url = GEMINI_API_URL.contains("?") ? GEMINI_API_URL + "&key=" + API_KEY : GEMINI_API_URL + "?key=" + API_KEY;
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = mapper.readTree(response.getBody());
                String text = root.at("/candidates/0/content/parts/0/text").asText();
                return text.replace("```json", "").replace("```", "").trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void createStaticFallback() {
        String broken = "public void test() { System.out.println(\"Eror\"); }"; 
        String correct = "public void test() { System.out.println(\"Error\"); }";
        challenges.add(new BugChallenge("static-1", "Typo Fix", "Fix the typo.", broken, correct, 1, "Easy"));
    }
}